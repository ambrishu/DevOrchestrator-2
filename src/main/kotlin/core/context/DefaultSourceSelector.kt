package core.context

import models.Story
import java.nio.file.Path

/**
 * [SourceSelector] using two deterministic MVP heuristics documented as sufficient for the
 * Context Engine (semantic code search and dependency-graph analysis are explicitly out of
 * MVP scope per `docs/07-context-engine.md` §19):
 *
 * 1. Explicit mention: the file's class name appears verbatim in the story text.
 * 2. Module mention: the file's containing directory name appears as a whole word in the story text.
 */
class DefaultSourceSelector : SourceSelector {

    override fun select(story: Story, sourceFiles: List<Path>): List<Path> {
        val text = listOf(story.title, story.description, story.acceptanceCriteria.joinToString(" ")).joinToString(" ")

        return sourceFiles
            .filter { file -> mentionsFileName(text, file) || mentionsModule(text, file) }
            .distinct()
            .sortedBy { it.toString() }
    }

    private fun mentionsFileName(text: String, file: Path): Boolean {
        val stem = file.fileName.toString().removeSuffix(".kt")
        return text.contains(stem)
    }

    private fun mentionsModule(text: String, file: Path): Boolean {
        val moduleName = file.parent?.fileName?.toString() ?: return false
        return wordBoundary(moduleName).containsMatchIn(text)
    }

    private fun wordBoundary(word: String): Regex = Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE)
}
