package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.build.BuildExecutor
import core.common.exception.BuildExecutionException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/**
 * `ado build run` — executes the configured build command and prints its raw result.
 *
 * This runs the project's real build command as a subprocess; it does not interpret failures.
 */
class RunBuildCommand :
    CliktCommand(name = "run", help = "Execute the configured build command"),
    KoinComponent {

    private val buildExecutor: BuildExecutor by inject()

    private val path: String by option("--path", help = "Repository path to build (defaults to the current directory)")
        .default(".")

    override fun run() {
        val repositoryPath = Paths.get(path).toAbsolutePath().normalize()

        val result = try {
            buildExecutor.executeBuild(repositoryPath)
        } catch (e: BuildExecutionException) {
            echo(e.message ?: "Failed to start the build", err = true)
            throw ProgramResult(1)
        }

        echo(BuildResultFormatter.format(result))

        if (!result.isSuccess) {
            throw ProgramResult(1)
        }
    }
}
