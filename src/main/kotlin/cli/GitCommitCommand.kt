package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.GitOperationException
import core.common.exception.StoryLoadException
import core.git.CommitMessageFormatter
import core.git.GitManager
import core.planner.StoryLoader
import core.planner.StoryPlanner
import models.Story
import models.StorySelection
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/**
 * `ado git commit [story-id]` — stages every change and commits it with a message generated
 * for the story.
 *
 * A real write path: it runs `git add -A` and `git commit` against the repository.
 */
class GitCommitCommand :
    CliktCommand(name = "commit", help = "Stage and commit a story's changes"),
    KoinComponent {

    private val storyLoader: StoryLoader by inject()
    private val storyPlanner: StoryPlanner by inject()
    private val commitMessageFormatter: CommitMessageFormatter by inject()
    private val gitManager: GitManager by inject()

    private val storyId: String? by argument(
        name = "story-id",
        help = "Story ID to commit changes for (defaults to the next executable story)",
    ).optional()

    private val path: String by option("--path", help = "Repository path to commit in (defaults to the current directory)")
        .default(".")

    override fun run() {
        val repositoryPath = Paths.get(path).toAbsolutePath().normalize()

        val stories = try {
            storyLoader.loadStories(repositoryPath)
        } catch (e: StoryLoadException) {
            echo(e.message ?: "Failed to load stories", err = true)
            throw ProgramResult(1)
        }

        val story = resolveStory(stories) ?: return

        val message = commitMessageFormatter.format(story)

        val result = try {
            gitManager.createCommit(repositoryPath, message)
        } catch (e: GitOperationException) {
            echo(e.message ?: "Failed to commit", err = true)
            throw ProgramResult(1)
        }

        echo(CommitResultFormatter.format(result))

        if (!result.success) {
            throw ProgramResult(1)
        }
    }

    private fun resolveStory(stories: List<Story>): Story? {
        val requestedId = storyId
        if (requestedId != null) {
            return stories.firstOrNull { it.id == requestedId } ?: run {
                echo("Story not found: $requestedId", err = true)
                throw ProgramResult(1)
            }
        }

        return when (val selection = storyPlanner.selectNext(stories)) {
            is StorySelection.Selected -> selection.story
            StorySelection.None -> {
                echo("No executable story found.")
                null
            }
        }
    }
}
