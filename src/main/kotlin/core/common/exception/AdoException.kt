package core.common.exception

/**
 * Base type for all exceptions raised by ADO.
 *
 * Business modules throw a specific subclass rather than this type directly,
 * so callers can distinguish failure sources without inspecting messages.
 */
sealed class AdoException(message: String, cause: Throwable? = null) : Exception(message, cause)
