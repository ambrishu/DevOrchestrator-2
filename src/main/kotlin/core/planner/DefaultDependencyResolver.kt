package core.planner

import models.Story
import models.StoryStatus

/** [DependencyResolver] backed by an in-memory lookup over the loaded story list. */
class DefaultDependencyResolver : DependencyResolver {

    override fun isSatisfied(story: Story, stories: List<Story>): Boolean {
        if (story.dependencies.isEmpty()) return true

        val statusById = stories.associate { it.id to it.status }
        return story.dependencies.all { dependencyId -> statusById[dependencyId] == StoryStatus.DONE }
    }
}
