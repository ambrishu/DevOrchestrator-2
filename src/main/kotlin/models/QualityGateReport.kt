package models

/** Result of running every quality gate, in the order they ran. */
data class QualityGateReport(
    val results: List<QualityGateResult>,
) {
    val allPassed: Boolean
        get() = results.all { it.passed }
}
