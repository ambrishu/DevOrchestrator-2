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
        DependencyGraphValidator.validate(stories)
        return stories
    }
}
