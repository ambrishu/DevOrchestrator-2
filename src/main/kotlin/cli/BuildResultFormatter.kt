package cli

import models.BuildResult

/** Renders a [BuildResult] as human-readable terminal output. */
object BuildResultFormatter {

    fun format(result: BuildResult): String = buildString {
        appendLine("Build Result")
        appendLine("============")
        appendLine("Status:    ${if (result.isSuccess) "SUCCESS" else "FAILURE"}")
        appendLine("Duration:  ${result.durationMillis}ms")
        appendLine("Warnings:  ${result.warnings.size}")
        result.warnings.forEach { appendLine("  - $it") }

        if (result.stdout.isNotBlank()) {
            appendLine()
            appendLine("stdout:")
            appendLine(result.stdout)
        }

        if (result.stderr.isNotBlank()) {
            appendLine()
            appendLine("stderr:")
            appendLine(result.stderr)
        }
    }.trimEnd()
}
