package core.planner

import models.Story

/** Evaluates whether a story's dependencies are complete. */
interface DependencyResolver {

    /**
     * Returns true if [story] has no dependencies, or every dependency ID resolves to a
     * [models.StoryStatus.DONE] story within [stories]. A dependency that does not resolve to any
     * known story is treated as unsatisfied.
     */
    fun isSatisfied(story: Story, stories: List<Story>): Boolean
}
