package models

/** Outcome of running an external process to completion. */
data class ProcessResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val durationMillis: Long,
) {
    val isSuccess: Boolean
        get() = exitCode == 0
}
