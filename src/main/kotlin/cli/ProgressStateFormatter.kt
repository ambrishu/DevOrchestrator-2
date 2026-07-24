package cli

import models.ProgressState

/** Renders a [ProgressState] as human-readable terminal output. */
object ProgressStateFormatter {

    fun format(state: ProgressState): String {
        if (state.stories.isEmpty()) return "No progress recorded yet."

        return buildString {
            appendLine("Progress")
            appendLine("========")
            val lines = state.stories.toSortedMap().map { (id, entry) -> "$id: ${entry.status.toToken()}" }
            append(lines.joinToString("\n"))
        }
    }
}
