package cli

import models.RepositoryStatus

/** Renders a [RepositoryStatus] as human-readable terminal output. */
object RepositoryStatusFormatter {

    fun format(status: RepositoryStatus): String {
        if (!status.hasChanges) return "No changes."

        return buildString {
            appendLine("Changes (${status.changedFiles.size}):")
            append(status.changedFiles.joinToString("\n") { "  $it" })
        }
    }
}
