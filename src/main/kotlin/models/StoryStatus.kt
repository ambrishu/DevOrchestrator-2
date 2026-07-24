package models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/** Lifecycle states a [Story] can occupy, as documented in the ADO backlog format. */
enum class StoryStatus {
    TODO,
    READY,
    IN_PROGRESS,
    REVIEW,
    RETRYING,
    BLOCKED,
    FAILED,
    PASSED,
    DONE,
    ;

    /** Renders as the snake_case token used in YAML and markdown (e.g. `"in_progress"`). */
    @JsonValue
    fun toToken(): String = name.lowercase()

    companion object {
        /** Parses a backlog status token (e.g. `"in_progress"`) case-insensitively. Returns null if unrecognized. */
        fun fromToken(token: String): StoryStatus? =
            entries.firstOrNull { it.name.equals(token.trim(), ignoreCase = true) }

        @JsonCreator
        @JvmStatic
        private fun fromJson(token: String): StoryStatus =
            fromToken(token) ?: throw IllegalArgumentException("Unsupported story status: \"$token\"")
    }
}
