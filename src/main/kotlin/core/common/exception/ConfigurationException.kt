package core.common.exception

/** Raised when ADO configuration cannot be located, read, or parsed. */
class ConfigurationException(message: String, cause: Throwable? = null) : AdoException(message, cause)
