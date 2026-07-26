package adapters.claude

import core.common.exception.ContextException
import core.common.exception.ProcessExecutionException
import core.common.exception.StoryLoadException
import core.common.exception.TaskGenerationException
import core.context.DocumentationLoader
import core.planner.DependencyGraphValidator
import core.planner.MarkdownStoryParser
import core.planner.StoryParser
import core.tasks.DefaultTaskGenerationPromptBuilder
import core.tasks.FileTaskGenerationCacheStore
import core.tasks.PlanningDocuments
import core.tasks.TaskGenerationCacheStore
import core.tasks.TaskGenerationPromptBuilder
import core.tasks.TaskGenerator
import models.Story
import models.TaskGenerationCache
import models.TaskGenerationResult
import java.nio.file.Files
import java.nio.file.Path

/**
 * [TaskGenerator] implemented by invoking the `claude` CLI in a fresh, read-only session.
 * Requires `claude` on `PATH`.
 *
 * Claude never writes `docs/TASKS.md` directly: it runs in `"plan"` mode and returns markdown
 * text, which is parsed and validated against the same grammar `core.planner.MarkdownStoryParser`
 * enforces before it is ever written to disk. This guarantees the backlog ADO's execution engine
 * reads is always loadable — an agent-written file that skipped this validation could silently
 * break every other ADO command.
 *
 * Generation is cached by a hash of the three input documents ([TaskGenerationCacheStore]): an LLM
 * call is inherently non-deterministic, so without caching, two runs over the same documents could
 * produce two different backlogs. Unchanged documents always reproduce the exact same content, and
 * a `docs/TASKS.md` that already matches it is left alone without requiring `force`.
 */
class ClaudeCodeTaskGenerator(
    private val documentationLoader: DocumentationLoader,
    private val promptBuilder: TaskGenerationPromptBuilder = DefaultTaskGenerationPromptBuilder(),
    private val invoker: ClaudeCodeInvoker = ClaudeCodeInvoker(),
    private val resultParser: TaskListResultParser = TaskListResultParser(),
    private val storyParser: StoryParser = MarkdownStoryParser(),
    private val cacheStore: TaskGenerationCacheStore = FileTaskGenerationCacheStore(),
) : TaskGenerator {

    override fun generate(repositoryPath: Path, force: Boolean, regenerate: Boolean): TaskGenerationResult {
        val outputFile = repositoryPath.resolve("docs").resolve("TASKS.md")
        val existingContent = readIfExists(outputFile)

        val documents = loadDocuments(repositoryPath)
        val inputHash = documents.contentHash()
        val cached = if (regenerate) null else cacheStore.load(repositoryPath)?.takeIf { it.inputHash == inputHash }

        val content = if (cached != null) {
            cached.content
        } else {
            requireOverwriteAllowed(outputFile, existingContent, force)
            generateFresh(repositoryPath, documents, inputHash)
        }

        val stories = validate(content)
        writeIfNeeded(outputFile, existingContent, content, force)

        return TaskGenerationResult(outputPath = outputFile.toString(), storyCount = stories.size, fromCache = cached != null)
    }

    private fun generateFresh(repositoryPath: Path, documents: PlanningDocuments, inputHash: String): String {
        val prompt = promptBuilder.buildPrompt(documents)

        val result = try {
            invoker.invoke(prompt, repositoryPath, permissionMode = "plan")
        } catch (e: ProcessExecutionException) {
            throw TaskGenerationException("Failed to invoke Claude Code for task generation: ${e.message}", e)
        }

        if (!result.isSuccess) {
            throw TaskGenerationException(
                "Claude Code task generation exited with status ${result.exitCode}" +
                    if (result.stderr.isNotBlank()) ": ${result.stderr}" else "",
            )
        }

        val content = resultParser.parse(result)
        validate(content)
        cacheStore.save(repositoryPath, TaskGenerationCache(inputHash = inputHash, content = content))
        return content
    }

    private fun readIfExists(outputFile: Path): String? {
        if (!Files.isRegularFile(outputFile)) return null
        return try {
            Files.readString(outputFile)
        } catch (e: Exception) {
            throw TaskGenerationException("Failed to read existing $outputFile", e)
        }
    }

    /** Fails fast, before spending an agent call, when we already know the result can't be written. */
    private fun requireOverwriteAllowed(outputFile: Path, existingContent: String?, force: Boolean) {
        if (force) return
        if (!existingContent.isNullOrBlank()) {
            throw TaskGenerationException("$outputFile already exists; pass --force to overwrite it")
        }
    }

    private fun writeIfNeeded(outputFile: Path, existingContent: String?, content: String, force: Boolean) {
        if (existingContent == content) return

        if (!existingContent.isNullOrBlank() && !force) {
            throw TaskGenerationException("$outputFile already exists with different content; pass --force to overwrite it")
        }

        try {
            Files.createDirectories(outputFile.parent)
            Files.writeString(outputFile, content)
        } catch (e: Exception) {
            throw TaskGenerationException("Failed to write $outputFile", e)
        }
    }

    private fun validate(content: String): List<Story> = try {
        storyParser.parse(content).also { stories ->
            DependencyGraphValidator.validate(stories)
            if (stories.isEmpty()) {
                throw TaskGenerationException("Generated backlog contained no tasks. Raw output:\n${preview(content)}")
            }
        }
    } catch (e: StoryLoadException) {
        throw TaskGenerationException(
            "Generated backlog is not a valid ADO task list: ${e.message}. Raw output:\n${preview(content)}",
            e,
        )
    }

    private fun preview(content: String): String =
        if (content.length <= 2000) content else content.take(2000) + "\n... (${content.length - 2000} more characters)"

    private fun loadDocuments(repositoryPath: Path) = PlanningDocuments(
        engineeringSpec = requireDocument(repositoryPath, "docs/AI_ENGINEERING_SPEC.md"),
        productRequirements = requireDocument(repositoryPath, "docs/02-product-requirements.md"),
        systemArchitecture = requireDocument(repositoryPath, "docs/03-system-architecture.md"),
    )

    private fun requireDocument(repositoryPath: Path, relativePath: String): String = try {
        documentationLoader.load(repositoryPath, relativePath)
            ?: throw TaskGenerationException("Required planning document not found: $relativePath")
    } catch (e: ContextException) {
        throw TaskGenerationException("Failed to read required planning document: $relativePath", e)
    }
}
