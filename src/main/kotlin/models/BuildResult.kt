package models

/**
 * Raw output of executing the configured build command.
 *
 * The Build Executor never interprets this result; categorizing failures is the Failure
 * Analyzer's job (a later milestone).
 */
data class BuildResult(
    val status: BuildStatus,
    val stdout: String,
    val stderr: String,
    val warnings: List<String> = emptyList(),
    val durationMillis: Long,
) {
    val isSuccess: Boolean
        get() = status == BuildStatus.SUCCESS
}
