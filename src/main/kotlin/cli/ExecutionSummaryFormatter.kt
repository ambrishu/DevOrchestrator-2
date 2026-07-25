package cli

import models.ExecutionSummary

/** Renders an [ExecutionSummary] as human-readable terminal output. */
object ExecutionSummaryFormatter {

    fun format(summary: ExecutionSummary): String {
        if (summary.results.isEmpty()) return "No executable story found. Nothing to do."

        return buildString {
            appendLine("Execution Summary")
            appendLine("==================")
            val lines = summary.results.map { result ->
                "${result.storyId}: ${result.status.toToken()} — ${result.message.ifBlank { "(no message)" }}"
            }
            append(lines.joinToString("\n"))
        }
    }
}
