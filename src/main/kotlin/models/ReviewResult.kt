package models

/** Structured outcome of an independent AI code review. Any blocking issue prevents completion. */
data class ReviewResult(
    val blockingIssues: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
) {
    val hasBlockingIssues: Boolean
        get() = blockingIssues.isNotEmpty()
}
