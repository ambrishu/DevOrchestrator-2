package adapters.claude

import core.common.exception.ContextException
import core.common.exception.ProcessExecutionException
import core.common.exception.TaskGenerationException
import core.context.DocumentationLoader
import core.tasks.PlanningDocuments
import core.tasks.TaskGenerationPromptBuilder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.ProcessResult
import utils.ProcessExecutor
import java.nio.file.Files
import java.nio.file.Path

private const val DIVIDER = "⸻"

class ClaudeCodeTaskGeneratorTest : FunSpec({

    val validBacklog = """
        ADO-001 — First Task

        Status: todo

        Depends On: None

        Description

        Does the first thing.

        Acceptance Criteria

        * It works

        $DIVIDER
    """.trimIndent()

    val alternateBacklog = """
        ADO-001 — Alternate First Task

        Status: todo

        Depends On: None

        Description

        Does a different first thing.

        Acceptance Criteria

        * It also works

        $DIVIDER

        ADO-002 — Alternate Second Task

        Status: todo

        Depends On: ADO-001

        Description

        Does a second thing.

        Acceptance Criteria

        * It works too

        $DIVIDER
    """.trimIndent()

    fun repo(): Path = Files.createTempDirectory("ado-task-generator-test")

    fun stubDocs(documentationLoader: DocumentationLoader, repo: Path) {
        every { documentationLoader.load(repo, "docs/AI_ENGINEERING_SPEC.md") } returns "spec"
        every { documentationLoader.load(repo, "docs/02-product-requirements.md") } returns "prd"
        every { documentationLoader.load(repo, "docs/03-system-architecture.md") } returns "architecture"
    }

    fun generatorWith(
        documentationLoader: DocumentationLoader,
        processExecutor: ProcessExecutor,
        promptBuilder: TaskGenerationPromptBuilder = mockk<TaskGenerationPromptBuilder>().also {
            every { it.buildPrompt(any<PlanningDocuments>()) } returns "the generation prompt"
        },
    ) = ClaudeCodeTaskGenerator(
        documentationLoader = documentationLoader,
        promptBuilder = promptBuilder,
        invoker = ClaudeCodeInvoker(processExecutor),
    )

    test("generates, validates, and writes a valid backlog") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } returns
            ProcessResult(exitCode = 0, stdout = validBacklog, stderr = "", durationMillis = 5)

        val result = generatorWith(documentationLoader, processExecutor).generate(repository)

        result.storyCount shouldBe 1
        result.fromCache shouldBe false
        result.outputPath shouldBe repository.resolve("docs").resolve("TASKS.md").toString()
        Files.readString(repository.resolve("docs").resolve("TASKS.md")) shouldBe validBacklog
    }

    test("invokes claude with plan permission mode, since generation must never modify the repository") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } returns
            ProcessResult(exitCode = 0, stdout = validBacklog, stderr = "", durationMillis = 5)

        generatorWith(documentationLoader, processExecutor).generate(repository)

        verify {
            processExecutor.execute(match { it.contains("--permission-mode") && it.contains("plan") }, repository)
        }
    }

    test("throws when a required planning document is missing") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        every { documentationLoader.load(repository, any()) } returns "content"
        every { documentationLoader.load(repository, "docs/AI_ENGINEERING_SPEC.md") } returns null

        shouldThrow<TaskGenerationException> {
            generatorWith(documentationLoader, mockk()).generate(repository)
        }
    }

    test("throws when a required planning document cannot be read") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        every { documentationLoader.load(repository, "docs/AI_ENGINEERING_SPEC.md") } throws
            ContextException("permission denied")

        shouldThrow<TaskGenerationException> {
            generatorWith(documentationLoader, mockk()).generate(repository)
        }
    }

    test("throws and writes nothing when claude cannot be started") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } throws ProcessExecutionException("claude: command not found")

        shouldThrow<TaskGenerationException> {
            generatorWith(documentationLoader, processExecutor).generate(repository)
        }
        Files.exists(repository.resolve("docs").resolve("TASKS.md")) shouldBe false
    }

    test("throws when claude exits with a non-zero status") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } returns
            ProcessResult(exitCode = 1, stdout = "", stderr = "session error", durationMillis = 5)

        shouldThrow<TaskGenerationException> {
            generatorWith(documentationLoader, processExecutor).generate(repository)
        }
    }

    test("throws and writes nothing when the generated backlog fails to parse") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } returns
            ProcessResult(exitCode = 0, stdout = "not a valid backlog", stderr = "", durationMillis = 5)

        shouldThrow<TaskGenerationException> {
            generatorWith(documentationLoader, processExecutor).generate(repository)
        }
        Files.exists(repository.resolve("docs").resolve("TASKS.md")) shouldBe false
    }

    test("throws when the generated backlog has an invalid dependency graph") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val invalidBacklog = """
            ADO-001 — Only Task

            Status: todo

            Depends On: ADO-999

            Description

            References a task that does not exist.

            $DIVIDER
        """.trimIndent()

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } returns
            ProcessResult(exitCode = 0, stdout = invalidBacklog, stderr = "", durationMillis = 5)

        shouldThrow<TaskGenerationException> {
            generatorWith(documentationLoader, processExecutor).generate(repository)
        }
    }

    test("refuses to overwrite an existing non-blank backlog without force, without calling claude") {
        val repository = repo()
        val docsDir = Files.createDirectory(repository.resolve("docs"))
        Files.writeString(docsDir.resolve("TASKS.md"), "existing backlog")

        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        shouldThrow<TaskGenerationException> {
            generatorWith(documentationLoader, mockk()).generate(repository, force = false)
        }
        Files.readString(docsDir.resolve("TASKS.md")) shouldBe "existing backlog"
    }

    test("overwrites an existing backlog when force is true") {
        val repository = repo()
        val docsDir = Files.createDirectory(repository.resolve("docs"))
        Files.writeString(docsDir.resolve("TASKS.md"), "existing backlog")

        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } returns
            ProcessResult(exitCode = 0, stdout = validBacklog, stderr = "", durationMillis = 5)

        generatorWith(documentationLoader, processExecutor).generate(repository, force = true)

        Files.readString(docsDir.resolve("TASKS.md")) shouldBe validBacklog
    }

    test("proceeds without a force flag when no backlog exists yet") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } returns
            ProcessResult(exitCode = 0, stdout = validBacklog, stderr = "", durationMillis = 5)

        val result = generatorWith(documentationLoader, processExecutor).generate(repository, force = false)

        result.storyCount shouldBe 1
    }

    test("reuses cached content on a second call with unchanged documents, without invoking claude again") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } returns
            ProcessResult(exitCode = 0, stdout = validBacklog, stderr = "", durationMillis = 5)

        val generator = generatorWith(documentationLoader, processExecutor)
        generator.generate(repository)
        val second = generator.generate(repository)

        second.fromCache shouldBe true
        second.storyCount shouldBe 1
        Files.readString(repository.resolve("docs").resolve("TASKS.md")) shouldBe validBacklog
        verify(exactly = 1) { processExecutor.execute(any(), repository) }
    }

    test("invokes claude again and refreshes the cache when a planning document changes") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } returnsMany listOf(
            ProcessResult(exitCode = 0, stdout = validBacklog, stderr = "", durationMillis = 5),
            ProcessResult(exitCode = 0, stdout = alternateBacklog, stderr = "", durationMillis = 5),
        )

        val generator = generatorWith(documentationLoader, processExecutor)
        generator.generate(repository)

        every { documentationLoader.load(repository, "docs/AI_ENGINEERING_SPEC.md") } returns "a changed spec"
        val second = generator.generate(repository, force = true)

        second.fromCache shouldBe false
        second.storyCount shouldBe 2
        Files.readString(repository.resolve("docs").resolve("TASKS.md")) shouldBe alternateBacklog
        verify(exactly = 2) { processExecutor.execute(any(), repository) }
    }

    test("regenerate bypasses the cache even when documents are unchanged") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } returnsMany listOf(
            ProcessResult(exitCode = 0, stdout = validBacklog, stderr = "", durationMillis = 5),
            ProcessResult(exitCode = 0, stdout = alternateBacklog, stderr = "", durationMillis = 5),
        )

        val generator = generatorWith(documentationLoader, processExecutor)
        generator.generate(repository)
        val second = generator.generate(repository, force = true, regenerate = true)

        second.fromCache shouldBe false
        second.storyCount shouldBe 2
        verify(exactly = 2) { processExecutor.execute(any(), repository) }
    }

    test("throws without calling claude when an on-disk backlog differs from cached content and force is not passed") {
        val repository = repo()
        val documentationLoader = mockk<DocumentationLoader>()
        stubDocs(documentationLoader, repository)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repository) } returns
            ProcessResult(exitCode = 0, stdout = validBacklog, stderr = "", durationMillis = 5)

        val generator = generatorWith(documentationLoader, processExecutor)
        generator.generate(repository)
        Files.writeString(repository.resolve("docs").resolve("TASKS.md"), "hand edited backlog")

        shouldThrow<TaskGenerationException> {
            generator.generate(repository, force = false)
        }
        verify(exactly = 1) { processExecutor.execute(any(), repository) }
    }
})
