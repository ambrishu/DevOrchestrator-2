package core.planner

import models.Story
import java.nio.file.Path

/** Loads the backlog of [Story] items for a repository. */
interface StoryLoader {

    /**
     * Loads and validates every story defined in `docs/TASKS.md` under [repositoryPath].
     *
     * @throws core.common.exception.StoryLoadException if the file is missing, unparsable, or
     * references a dependency ID that does not exist.
     */
    fun loadStories(repositoryPath: Path): List<Story>
}
