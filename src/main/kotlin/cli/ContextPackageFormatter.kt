package cli

import models.ContextPackage
import models.SourceFile

/** Renders a [ContextPackage] as human-readable terminal output. */
object ContextPackageFormatter {

    fun format(context: ContextPackage): String = buildString {
        appendLine("Context Package: ${context.story.id} — ${context.story.title}")
        appendLine("=========================================")
        appendLine("Acceptance Criteria:    ${context.acceptanceCriteria.size}")
        appendLine("PRD included:           ${yesNo(context.prdExcerpts.isNotEmpty())}")
        appendLine("Architecture included:  ${yesNo(context.architectureRules.isNotEmpty())}")
        appendLine()
        append(fileSection("Impacted Source Files", context.impactedSourceFiles))
        appendLine()
        appendLine()
        append(fileSection("Related Tests", context.relatedTests))
    }.trimEnd()

    private fun fileSection(title: String, files: List<SourceFile>): String = buildString {
        appendLine("$title (${files.size}):")
        if (files.isEmpty()) {
            append("  (none)")
        } else {
            append(files.joinToString("\n") { "  - ${it.path}" })
        }
    }

    private fun yesNo(value: Boolean): String = if (value) "yes" else "no"
}
