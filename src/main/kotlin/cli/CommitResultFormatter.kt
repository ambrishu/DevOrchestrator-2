package cli

import models.CommitResult

/** Renders a [CommitResult] as human-readable terminal output. */
object CommitResultFormatter {

    fun format(result: CommitResult): String = if (result.success) {
        "Committed ${result.commitSha}"
    } else {
        "Commit failed: ${result.failureReason ?: "unknown reason"}"
    }
}
