package core.common.exception

/** Raised when the repository cannot be scanned or documentation cannot be read while building context. */
class ContextException(message: String, cause: Throwable? = null) : AdoException(message, cause)
