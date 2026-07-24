package core.common.exception

/** Raised when an external process cannot be started. */
class ProcessExecutionException(message: String, cause: Throwable? = null) : AdoException(message, cause)
