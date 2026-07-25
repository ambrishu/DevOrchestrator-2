package models

/** Structured interpretation of a failed [BuildResult], produced by the Failure Analyzer. */
data class FailureAnalysis(
    val category: FailureCategory,
    val details: List<String> = emptyList(),
)
