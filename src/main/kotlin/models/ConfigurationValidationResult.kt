package models

/** Result of validating an [AdoConfiguration] against ADO's semantic rules. */
data class ConfigurationValidationResult(
    val errors: List<String> = emptyList(),
) {
    val isValid: Boolean
        get() = errors.isEmpty()
}
