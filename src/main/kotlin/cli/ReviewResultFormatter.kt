package cli

import models.ReviewResult

/** Renders a [ReviewResult] as human-readable terminal output. */
object ReviewResultFormatter {

    fun format(result: ReviewResult): String = buildString {
        appendLine("Review Result")
        appendLine("=============")
        appendLine("Blocking Issues (${result.blockingIssues.size}):")
        if (result.blockingIssues.isEmpty()) {
            appendLine("  (none)")
        } else {
            result.blockingIssues.forEach { appendLine("  - $it") }
        }

        appendLine()
        appendLine("Recommendations (${result.recommendations.size}):")
        if (result.recommendations.isEmpty()) {
            append("  (none)")
        } else {
            append(result.recommendations.joinToString("\n") { "  - $it" })
        }
    }.trimEnd()
}
