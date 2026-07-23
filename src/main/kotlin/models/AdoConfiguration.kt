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
)
