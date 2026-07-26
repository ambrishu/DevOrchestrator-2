package core.planner

import core.common.exception.StoryLoadException
import models.Story

/** Validates that every story dependency in a backlog refers to a story that actually exists. */
object DependencyGraphValidator {

    /** @throws StoryLoadException if any story depends on an unknown story ID. */
    fun validate(stories: List<Story>) {
        val knownIds = stories.map { it.id }.toSet()
        val missing = stories
            .flatMap { story -> story.dependencies.filterNot { it in knownIds }.map { story.id to it } }

        if (missing.isNotEmpty()) {
            val details = missing.joinToString("\n") { (storyId, dependencyId) ->
                "  - $storyId depends on unknown story $dependencyId"
            }
            throw StoryLoadException("Invalid dependency graph:\n$details")
        }
    }
}
