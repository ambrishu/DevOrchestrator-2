package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.StoryLoadException
import core.execution.ExecutionEngine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/**
 * `ado run` — runs the Execution Engine over every executable story.
 *
 * This is a write path: it invokes the configured AI agent, which may modify the repository,
 * and persists progress to `.ado/progress.yaml` as it goes.
 */
class RunCommand :
    CliktCommand(name = "run", help = "Run the execution engine over every executable story"),
    KoinComponent {

    private val executionEngine: ExecutionEngine by inject()

    private val path: String by option("--path", help = "Repository path to operate on (defaults to the current directory)")
        .default(".")

    override fun run() {
        val repositoryPath = Paths.get(path).toAbsolutePath().normalize()

        val summary = try {
            executionEngine.run(repositoryPath)
        } catch (e: StoryLoadException) {
            echo(e.message ?: "Failed to load stories", err = true)
            throw ProgramResult(1)
        }

        echo(ExecutionSummaryFormatter.format(summary))

        if (summary.hasBlockedStory) {
            throw ProgramResult(1)
        }
    }
}
