package models

/** Output of a [core.tasks.TaskGenerator] invocation. */
data class TaskGenerationResult(
    val outputPath: String,
    val storyCount: Int,
    val fromCache: Boolean = false,
)
