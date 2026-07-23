package core.configuration

import models.AdoConfiguration
import models.ConfigurationValidationResult

/** Validates a parsed [AdoConfiguration] against ADO's semantic rules. */
interface ConfigurationValidator {

    /** Returns every rule violation found in [config]. An empty result means the configuration is valid. */
    fun validate(config: AdoConfiguration): ConfigurationValidationResult
}
