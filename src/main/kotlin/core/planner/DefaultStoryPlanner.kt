package core.planner

import models.Story
import models.StorySelection
import models.StoryStatus

/**
 * [StoryPlanner] implementing the task selection rules documented in `docs/TASKS.md`:
 * the next executable story is the first, in document order, that is `todo` with every
 * dependency `done`.
 */
class DefaultStoryPlanner(
    private val dependencyResolver: DependencyResolver = DefaultDependencyResolver(),
) : StoryPlanner {

    override fun selectNext(stories: List<Story>): StorySelection {
        val next = stories.firstOrNull { story ->
            story.status == StoryStatus.TODO && dependencyResolver.isSatisfied(story, stories)
        }

        return if (next != null) StorySelection.Selected(next) else StorySelection.None
    }
}
