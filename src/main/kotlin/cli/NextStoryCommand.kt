package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.StoryLoadException
import core.planner.StoryLoader
import core.planner.StoryPlanner
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/** `ado plan next` — prints the next executable story. Read-only; never writes files. */
class NextStoryCommand :
    CliktCommand(name = "next", help = "Print the next executable story from docs/TASKS.md"),
    KoinComponent {

    private val storyLoader: StoryLoader by inject()
    private val storyPlanner: StoryPlanner by inject()

    private val path: String by option("--path", help = "Repository path to read docs/TASKS.md from (defaults to the current directory)")
        .default(".")

    override fun run() {
        val repositoryPath = Paths.get(path).toAbsolutePath().normalize()

        val stories = try {
            storyLoader.loadStories(repositoryPath)
        } catch (e: StoryLoadException) {
            echo(e.message ?: "Failed to load stories", err = true)
            throw ProgramResult(1)
        }

        echo(StorySelectionFormatter.format(storyPlanner.selectNext(stories)))
    }
}
