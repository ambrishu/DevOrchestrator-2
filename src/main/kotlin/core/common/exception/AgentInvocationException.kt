package core.common.exception

/** Raised when an AI coding agent cannot be invoked, or exits with a failure status. */
class AgentInvocationException(message: String, cause: Throwable? = null) : AdoException(message, cause)
