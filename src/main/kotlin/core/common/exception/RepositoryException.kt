package core.common.exception

/** Raised when the target repository path cannot be accessed or inspected. */
class RepositoryException(message: String, cause: Throwable? = null) : AdoException(message, cause)
