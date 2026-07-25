package models

/** Outcome of executing one story through the Execution Engine. */
data class StoryExecutionResult(
    val storyId: String,
    val status: StoryStatus,
    val message: String,
)
