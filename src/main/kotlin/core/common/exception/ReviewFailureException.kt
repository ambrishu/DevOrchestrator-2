package core.common.exception

/** Raised when the code review agent cannot be invoked, or its invocation exits with a failure status. */
class ReviewFailureException(message: String, cause: Throwable? = null) : AdoException(message, cause)
