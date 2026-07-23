package cli

import models.AdoConfiguration

/** Renders an [AdoConfiguration] as human-readable terminal output. */
object ConfigurationSummaryFormatter {

    fun format(config: AdoConfiguration, source: String): String = buildString {
        appendLine("Effective Configuration")
        appendLine("========================")
        appendLine("Source:          $source")
        appendLine("agent.provider:  ${config.agent.provider}")
        appendLine("build.command:   ${config.build.command}")
        appendLine("test.command:    ${config.test.command}")
        appendLine("review.enabled:  ${config.review.enabled}")
        append("repair.retries:  ${config.repair.retries}")
    }
}
