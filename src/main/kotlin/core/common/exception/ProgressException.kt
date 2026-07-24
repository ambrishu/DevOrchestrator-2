package core.common.exception

/** Raised when `.ado/progress.yaml` cannot be read, parsed, or written. */
class ProgressException(message: String, cause: Throwable? = null) : AdoException(message, cause)
