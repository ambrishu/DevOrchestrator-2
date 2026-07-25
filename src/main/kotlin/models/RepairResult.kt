package models

/** Outcome of running the Repair Loop: the final build result and how many attempts it took. */
data class RepairResult(
    val finalBuildResult: BuildResult,
    val attempts: Int,
) {
    val succeeded: Boolean
        get() = finalBuildResult.isSuccess
}
