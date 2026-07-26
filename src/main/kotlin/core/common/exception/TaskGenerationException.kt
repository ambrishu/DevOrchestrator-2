package core.common.exception

/** Raised when the task generator cannot be invoked, produces unloadable output, or is blocked by an existing backlog. */
class TaskGenerationException(message: String, cause: Throwable? = null) : AdoException(message, cause)
