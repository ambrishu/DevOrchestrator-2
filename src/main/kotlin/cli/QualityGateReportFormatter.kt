package cli

import models.QualityGateReport

/** Renders a [QualityGateReport] as human-readable terminal output. */
object QualityGateReportFormatter {

    fun format(report: QualityGateReport): String = buildString {
        appendLine("Quality Gate Report")
        appendLine("====================")
        report.results.forEach { result ->
            val status = if (result.passed) "PASS" else "FAIL"
            val suffix = result.details.takeIf { it.isNotBlank() }
                ?.lineSequence()?.firstOrNull { it.isNotBlank() }
                ?.let { " — $it" }
                .orEmpty()
            appendLine("${result.name}: $status$suffix")
        }
        appendLine()
        append("Overall: ${if (report.allPassed) "PASS" else "FAIL"}")
    }.trimEnd()
}
