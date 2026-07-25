package cli

import models.FailureAnalysis

/** Renders a [FailureAnalysis] as human-readable terminal output. */
object FailureAnalysisFormatter {

    fun format(analysis: FailureAnalysis): String = buildString {
        appendLine("Failure Analysis")
        appendLine("================")
        appendLine("Category: ${analysis.category}")
        appendLine("Details (${analysis.details.size}):")
        if (analysis.details.isEmpty()) {
            append("  (none)")
        } else {
            append(analysis.details.joinToString("\n") { "  - $it" })
        }
    }.trimEnd()
}
