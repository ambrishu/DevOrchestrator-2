package models

/** Outcome of attempting to commit a story's changes. */
data class CommitResult(
    val success: Boolean,
    val commitSha: String? = null,
    val failureReason: String? = null,
)
