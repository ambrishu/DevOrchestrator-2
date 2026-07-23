package core.planner

import core.common.exception.StoryLoadException
import io.github.oshai.kotlinlogging.KotlinLogging
import models.Story
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/** [StoryLoader] reading `docs/TASKS.md` from a repository. */
class TasksFileLoader(
    private val parser: StoryParser = MarkdownStoryParser(),
) : StoryLoader {

    override fun loadStories(repositoryPath: Path): List<Story> {
        val tasksFile = repositoryPath.resolve("docs").resolve("TASKS.md")
        if (!Files.isRegularFile(tasksFile)) {
            throw StoryLoadException("Tasks file not found: $tasksFile")
        }

        logger.debug { "Loading stories from $tasksFile" }

        val content = try {
            Files.readString(tasksFile)
        } catch (e: Exception) {
            throw StoryLoadException("Failed to read tasks file: $tasksFile", e)
        }

        val stories = parser.parse(content)
        validateDependencyGraph(stories)
        return stories
    }

    private fun validateDependencyGraph(stories: List<Story>) {
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
