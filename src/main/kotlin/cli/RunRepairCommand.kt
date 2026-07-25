package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.build.BuildExecutor
import core.common.exception.AgentInvocationException
import core.common.exception.BuildExecutionException
import core.common.exception.ContextException
import core.common.exception.StoryLoadException
import core.configuration.ConfigurationLoader
import core.context.ContextBuilder
import core.planner.StoryLoader
import core.planner.StoryPlanner
import core.repair.RepairLoop
import models.AdoConfiguration
import models.Story
import models.StorySelection
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/**
 * `ado repair run [story-id]` — runs the build for a story and, if it fails, repairs it by
 * invoking the agent and rebuilding until it succeeds or the configured retry limit is reached.
 *
 * A real write path: it invokes the agent and re-runs the build repeatedly.
 */
class RunRepairCommand :
    CliktCommand(name = "run", help = "Repair a failing build by invoking the agent until it succeeds or retries are exhausted"),
    KoinComponent {

    private val storyLoader: StoryLoader by inject()
    private val storyPlanner: StoryPlanner by inject()
    private val contextBuilder: ContextBuilder by inject()
    private val buildExecutor: BuildExecutor by inject()
    private val repairLoop: RepairLoop by inject()
    private val configurationLoader: ConfigurationLoader by inject()

    private val storyId: String? by argument(
        name = "story-id",
        help = "Story ID to repair (defaults to the next executable story)",
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

        val initialResult = try {
            buildExecutor.executeBuild(repositoryPath)
        } catch (e: BuildExecutionException) {
            echo(e.message ?: "Failed to start the build", err = true)
            throw ProgramResult(1)
        }

        if (initialResult.isSuccess) {
            echo("Build already succeeds; nothing to repair.")
            return
        }

        val configPath = repositoryPath.resolve(".ado").resolve("config.yaml")
        val config = if (configurationLoader.exists(configPath)) configurationLoader.load(configPath) else AdoConfiguration()

        val repairResult = try {
            repairLoop.repair(context, repositoryPath, initialResult, config.repair.retries)
        } catch (e: AgentInvocationException) {
            echo(e.message ?: "Failed to invoke the agent", err = true)
            throw ProgramResult(1)
        } catch (e: BuildExecutionException) {
            echo(e.message ?: "Failed to start the build", err = true)
            throw ProgramResult(1)
        }

        echo(RepairResultFormatter.format(repairResult))

        if (!repairResult.succeeded) {
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
