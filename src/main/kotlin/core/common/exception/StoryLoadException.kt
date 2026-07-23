package core.common.exception

/** Raised when `docs/TASKS.md` cannot be located, parsed, or forms an invalid dependency graph. */
class StoryLoadException(message: String, cause: Throwable? = null) : AdoException(message, cause)
