package core.common.exception

/**
 * Raised when a git precondition operation (status, add, or reading the new commit SHA) fails,
 * or git cannot be started at all.
 *
 * A commit that fails for an ordinary reason (nothing staged, a rejecting hook) is not this —
 * it is a normal [models.CommitResult] with `success = false`.
 */
class GitOperationException(message: String, cause: Throwable? = null) : AdoException(message, cause)
