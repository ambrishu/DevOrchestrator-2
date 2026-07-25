package models

/** Result of one Execution Engine run: every story it processed, in the order they ran. */
data class ExecutionSummary(
    val results: List<StoryExecutionResult> = emptyList(),
) {
    val hasBlockedStory: Boolean
        get() = results.any { it.status == StoryStatus.BLOCKED }
}
