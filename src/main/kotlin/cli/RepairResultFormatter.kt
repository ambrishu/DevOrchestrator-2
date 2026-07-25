package cli

import models.RepairResult

/** Renders a [RepairResult] as human-readable terminal output. */
object RepairResultFormatter {

    fun format(result: RepairResult): String = buildString {
        appendLine("Repair Result")
        appendLine("=============")
        appendLine("Attempts: ${result.attempts}")
        append("Outcome:  ${if (result.succeeded) "SUCCESS" else "RETRIES EXHAUSTED"}")
    }.trimEnd()
}
