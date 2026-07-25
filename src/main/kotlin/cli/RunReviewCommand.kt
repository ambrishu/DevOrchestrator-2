package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.ContextException
import core.common.exception.ReviewFailureException
import core.common.exception.StoryLoadException
import core.context.ContextBuilder
import core.planner.StoryLoader
import core.planner.StoryPlanner
import core.review.CodeReviewAgent
import models.Story
import models.StorySelection
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/**
 * `ado review run [story-id]` — performs an independent AI code review of a story's changes.
 *
 * Discovers what to review via `git status`; requires the `claude` binary on `PATH`.
 */
class RunReviewCommand :
    CliktCommand(name = "run", help = "Perform an independent AI code review of a story's changes"),
    KoinComponent {

    private val storyLoader: StoryLoader by inject()
    private val storyPlanner: StoryPlanner by inject()
    private val contextBuilder: ContextBuilder by inject()
    private val codeReviewAgent: CodeReviewAgent by inject()

    private val storyId: String? by argument(
        name = "story-id",
        help = "Story ID to review (defaults to the next executable story)",
    ).optional()

    private val path: String by option("--path", help = "Repository path to operate on (defaults to the current directory)")
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

        val context = try {
            contextBuilder.buildContext(story, repositoryPath)
        } catch (e: ContextException) {
            echo(e.message ?: "Failed to build context", err = true)
            throw ProgramResult(1)
        }

        val result = try {
            codeReviewAgent.review(context, repositoryPath)
        } catch (e: ReviewFailureException) {
            echo(e.message ?: "Failed to perform the review", err = true)
            throw ProgramResult(1)
        }

        echo(ReviewResultFormatter.format(result))

        if (result.hasBlockingIssues) {
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
