package models

/**
 * Result of validating a repository path as an ADO target.
 *
 * This is a read-only snapshot: producing it must never modify the
 * repository being inspected.
 */
data class RepositoryReadiness(
    val repositoryPath: String,
    val exists: Boolean,
    val isDirectory: Boolean,
    val isGitRepository: Boolean,
    val isWritable: Boolean,
    val hasDocumentation: Boolean,
    val hasConfiguration: Boolean,
    val issues: List<String> = emptyList(),
) {
    val isReady: Boolean
        get() = exists && isDirectory && isGitRepository && isWritable && issues.isEmpty()
}
