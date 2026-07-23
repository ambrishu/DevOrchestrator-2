package models

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

    companion object {
        /** Parses a backlog status token (e.g. `"in_progress"`) case-insensitively. Returns null if unrecognized. */
        fun fromToken(token: String): StoryStatus? =
            entries.firstOrNull { it.name.equals(token.trim(), ignoreCase = true) }
    }
}
