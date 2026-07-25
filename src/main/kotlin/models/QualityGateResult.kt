package models

/** Outcome of a single quality gate. */
data class QualityGateResult(
    val name: String,
    val passed: Boolean,
    val details: String = "",
)
