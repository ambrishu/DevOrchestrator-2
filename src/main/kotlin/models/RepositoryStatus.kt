package models

/** Snapshot of a git repository's working tree state. */
data class RepositoryStatus(
    val hasChanges: Boolean,
    val changedFiles: List<String> = emptyList(),
)
