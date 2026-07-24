package core.context

import core.common.exception.ContextException
import models.ContextPackage
import models.SourceFile
import models.Story
import java.nio.file.Files
import java.nio.file.Path

/**
 * [ContextBuilder] assembling context from repository-relative documentation and source files,
 * following the priority order in `docs/07-context-engine.md` §5: story, then documentation,
 * then source, then tests.
 */
class DefaultContextBuilder(
    private val repositoryScanner: RepositoryScanner = DefaultRepositoryScanner(),
    private val documentationLoader: DocumentationLoader = FileDocumentationLoader(),
    private val sourceSelector: SourceSelector = DefaultSourceSelector(),
) : ContextBuilder {

    override fun buildContext(story: Story, repositoryPath: Path): ContextPackage {
        val repositoryFiles = repositoryScanner.scan(repositoryPath)
        val sourceFiles = repositoryFiles.filter { RepositoryFileClassifier.isSourceFile(it) }
        val testFiles = repositoryFiles.filter { RepositoryFileClassifier.isTestFile(it) }.toSet()

        val selectedSource = sourceSelector.select(story, sourceFiles)
        val selectedTests = selectedSource.mapNotNull { testPathFor(it) }.filter { it in testFiles }.distinct()

        return ContextPackage(
            story = story,
            acceptanceCriteria = story.acceptanceCriteria,
            prdExcerpts = loadExcerpt(repositoryPath, "docs/02-product-requirements.md"),
            architectureRules = loadExcerpt(repositoryPath, "docs/03-system-architecture.md"),
            impactedSourceFiles = selectedSource.map { readSourceFile(repositoryPath, it) },
            relatedTests = selectedTests.map { readSourceFile(repositoryPath, it) },
        )
    }

    private fun loadExcerpt(repositoryPath: Path, relativePath: String): List<String> =
        documentationLoader.load(repositoryPath, relativePath)?.let { listOf(it) } ?: emptyList()

    private fun readSourceFile(repositoryPath: Path, relativePath: Path): SourceFile {
        val absolute = repositoryPath.resolve(relativePath)
        val content = try {
            Files.readString(absolute)
        } catch (e: Exception) {
            throw ContextException("Failed to read source file: $absolute", e)
        }
        return SourceFile(path = relativePath.toString(), content = content)
    }

    private fun testPathFor(sourcePath: Path): Path? {
        val sourceStr = sourcePath.toString()
        if (!sourceStr.startsWith(SOURCE_PREFIX) || !sourceStr.endsWith(".kt")) return null

        val relative = sourceStr.removePrefix(SOURCE_PREFIX).removeSuffix(".kt")
        return Path.of("$TEST_PREFIX$relative" + "Test.kt")
    }

    private companion object {
        const val SOURCE_PREFIX = "src/main/kotlin/"
        const val TEST_PREFIX = "src/test/kotlin/"
    }
}
