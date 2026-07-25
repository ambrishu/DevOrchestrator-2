package core.review

import models.ContextPackage
import models.SourceFile

/**
 * [ReviewPromptBuilder] rendering the documented review checks (`docs/05-component-design.md`
 * §11 and `docs/TASKS.md` ADO-119) and a machine-parseable output protocol: one finding per
 * line, prefixed with `BLOCKING:` for issues that must block completion or `RECOMMENDATION:`
 * for everything else.
 */
class DefaultReviewPromptBuilder : ReviewPromptBuilder {

    override fun buildPrompt(context: ContextPackage, changedFiles: List<SourceFile>): String = buildString {
        appendLine("You are performing an independent code review for the AI Development Orchestrator (ADO).")
        appendLine("This is a separate review session; do not assume any memory of the implementation work.")

        appendLine()
        appendLine("Story: ${context.story.id} — ${context.story.title}")
        if (context.story.description.isNotBlank()) {
            appendLine(context.story.description)
        }

        appendLine()
        appendLine("Acceptance Criteria:")
        if (context.acceptanceCriteria.isEmpty()) {
            appendLine("(none specified)")
        } else {
            context.acceptanceCriteria.forEach { appendLine("- $it") }
        }

        appendLine()
        appendLine("Review the following changed files against these checks:")
        REVIEW_CHECKS.forEach { appendLine("- $it") }

        appendLine()
        appendLine("Changed Files:")
        if (changedFiles.isEmpty()) {
            appendLine("(no changed files detected)")
        } else {
            changedFiles.forEach { file ->
                appendLine("--- ${file.path} ---")
                appendLine(file.content)
            }
        }

        appendLine()
        appendLine("Report one finding per line, in one of these two forms:")
        appendLine("BLOCKING: <issue that must be fixed before this story can be completed>")
        appendLine("RECOMMENDATION: <non-blocking suggestion>")
        append("If there are no issues, report nothing.")
    }.trimEnd()

    private companion object {
        val REVIEW_CHECKS = listOf(
            "Architecture compliance",
            "SOLID principles",
            "Naming",
            "Readability",
            "Maintainability",
            "Performance",
            "Thread safety",
            "Test coverage",
        )
    }
}
