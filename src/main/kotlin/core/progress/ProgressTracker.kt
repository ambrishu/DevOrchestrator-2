package core.progress

import models.ProgressState
import models.StoryStatus
import java.nio.file.Path

/** Loads, updates, and persists ADO's runtime execution state. */
interface ProgressTracker {

    /**
     * Loads the progress snapshot for the repository at [repositoryPath].
     *
     * Returns an empty [ProgressState] if `.ado/progress.yaml` does not exist.
     *
     * @throws core.common.exception.ProgressException if the file exists but cannot be parsed.
     */
    fun loadProgress(repositoryPath: Path): ProgressState

    /** Writes [state] to `.ado/progress.yaml` under [repositoryPath], creating the file if needed. */
    fun saveProgress(repositoryPath: Path, state: ProgressState)

    /**
     * Loads the current progress, records [status] for [storyId], persists the result
     * immediately, and returns the updated snapshot.
     */
    fun updateStatus(repositoryPath: Path, storyId: String, status: StoryStatus): ProgressState
}
