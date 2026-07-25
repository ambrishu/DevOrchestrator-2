package core.execution

import models.ProgressState
import models.Story

/**
 * Merges ADO's own runtime record onto the backlog loaded from `docs/TASKS.md`.
 *
 * Once ADO has recorded a status for a story in [ProgressState], that status is authoritative
 * for planning purposes — it overrides the story's static `Status:` field, without modifying
 * the source document.
 */
object ProgressOverlay {

    fun apply(stories: List<Story>, progress: ProgressState): List<Story> =
        stories.map { story -> progress.statusOf(story.id)?.let { story.copy(status = it) } ?: story }
}
