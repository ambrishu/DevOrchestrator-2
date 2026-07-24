package models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Immutable snapshot of every story's execution status, persisted to `.ado/progress.yaml`.
 *
 * This is ADO's own runtime record, independent of the `Status:` field embedded in
 * `docs/TASKS.md`.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProgressState(
    val stories: Map<String, StoryProgressEntry> = emptyMap(),
) {
    fun statusOf(storyId: String): StoryStatus? = stories[storyId]?.status

    /** Returns a new [ProgressState] with [storyId] recorded at [status]; all other entries are preserved. */
    fun withStatus(storyId: String, status: StoryStatus): ProgressState =
        copy(stories = stories + (storyId to StoryProgressEntry(status)))
}
