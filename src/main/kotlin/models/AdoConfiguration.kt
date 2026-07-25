package models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** Agent invocation settings. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AgentConfig(
    val provider: String = "claude-code",
)

/** Build command settings. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class BuildConfig(
    val command: String = "./gradlew build",
)

/** Test command settings. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TestConfig(
    val command: String = "./gradlew test",
)

/** AI code review settings. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ReviewConfig(
    val enabled: Boolean = true,
)

/** Repair loop settings. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RepairConfig(
    val retries: Int = 5,
)

/**
 * Formatting quality gate command. Blank means the gate is not configured for this repository
 * and is skipped, since no formatting tool can be safely assumed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class FormattingConfig(
    val command: String = "",
)

/** Static analysis quality gate command. Blank means the gate is skipped, as with [FormattingConfig]. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class StaticAnalysisConfig(
    val command: String = "",
)

/** Integration test quality gate command. Blank means the gate is skipped, as with [FormattingConfig]. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class IntegrationTestConfig(
    val command: String = "",
)

/** Architecture validation quality gate command. Blank means the gate is skipped, as with [FormattingConfig]. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ArchitectureValidationConfig(
    val command: String = "",
)

/**
 * Top-level ADO configuration loaded from `.ado/config.yaml`.
 *
 * Every section has a default, so a partial or empty document parses to a
 * usable configuration.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AdoConfiguration(
    val agent: AgentConfig = AgentConfig(),
    val build: BuildConfig = BuildConfig(),
    val test: TestConfig = TestConfig(),
    val review: ReviewConfig = ReviewConfig(),
    val repair: RepairConfig = RepairConfig(),
    val formatting: FormattingConfig = FormattingConfig(),
    val staticAnalysis: StaticAnalysisConfig = StaticAnalysisConfig(),
    val integrationTest: IntegrationTestConfig = IntegrationTestConfig(),
    val architectureValidation: ArchitectureValidationConfig = ArchitectureValidationConfig(),
)
