package core.configuration

import models.AdoConfiguration
import models.ConfigurationValidationResult

/** [ConfigurationValidator] enforcing the semantic rules documented for `.ado/config.yaml`. */
class DefaultConfigurationValidator : ConfigurationValidator {

    override fun validate(config: AdoConfiguration): ConfigurationValidationResult {
        val errors = buildList {
            if (config.agent.provider.isBlank()) {
                add("agent.provider must not be blank")
            }
            if (config.build.command.isBlank()) {
                add("build.command must not be blank")
            }
            if (config.test.command.isBlank()) {
                add("test.command must not be blank")
            }
            if (config.repair.retries < 0) {
                add("repair.retries must not be negative, got ${config.repair.retries}")
            }
            if (config.repair.retries > MAX_RETRIES) {
                add("repair.retries must not exceed $MAX_RETRIES, got ${config.repair.retries}")
            }
        }

        return ConfigurationValidationResult(errors)
    }

    private companion object {
        const val MAX_RETRIES = 20
    }
}
