package core.common.exception

/** Raised when the configured build command cannot even be started (not for a build that runs and fails). */
class BuildExecutionException(message: String, cause: Throwable? = null) : AdoException(message, cause)
