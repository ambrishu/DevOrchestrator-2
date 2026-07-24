package cli

import models.GenerationResult

/** Renders a [GenerationResult] as human-readable terminal output. */
object GenerationResultFormatter {

    fun format(result: GenerationResult): String = buildString {
        appendLine("Generation Result")
        appendLine("==================")
        appendLine("Modified Files (${result.modifiedFiles.size}):")
        if (result.modifiedFiles.isEmpty()) {
            appendLine("  (none)")
        } else {
            result.modifiedFiles.forEach { appendLine("  - $it") }
        }

        appendLine()
        appendLine("Summary:")
        append(result.summary.ifBlank { "(empty)" })

        if (result.implementationNotes.isNotBlank()) {
            appendLine()
            appendLine()
            appendLine("Implementation Notes:")
            append(result.implementationNotes)
        }
    }.trimEnd()
}
