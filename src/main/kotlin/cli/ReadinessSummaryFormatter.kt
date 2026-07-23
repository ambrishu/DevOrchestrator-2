package cli

import models.RepositoryReadiness

/** Renders a [RepositoryReadiness] snapshot as human-readable terminal output. */
object ReadinessSummaryFormatter {

    fun format(readiness: RepositoryReadiness, configurationStatus: String): String = buildString {
        appendLine("Repository Readiness Summary")
        appendLine("=============================")
        appendLine("Path:            ${readiness.repositoryPath}")
        appendLine("Exists:          ${yesNo(readiness.exists)}")
        appendLine("Directory:       ${yesNo(readiness.isDirectory)}")
        appendLine("Git repository:  ${yesNo(readiness.isGitRepository)}")
        appendLine("Writable:        ${yesNo(readiness.isWritable)}")
        appendLine("Documentation:   ${yesNo(readiness.hasDocumentation)} (docs/)")
        appendLine("Configuration:   $configurationStatus")

        if (readiness.issues.isNotEmpty()) {
            appendLine()
            appendLine("Issues:")
            readiness.issues.forEach { appendLine("  - $it") }
        }

        appendLine()
        append("Status: ${if (readiness.isReady) "READY" else "NOT READY"}")
    }

    private fun yesNo(value: Boolean): String = if (value) "yes" else "no"
}
