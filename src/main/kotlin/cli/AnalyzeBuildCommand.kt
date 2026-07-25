package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.build.BuildExecutor
import core.common.exception.BuildExecutionException
import core.repair.FailureAnalyzer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/** `ado build analyze` — runs the build and, if it fails, prints a structured failure analysis. */
class AnalyzeBuildCommand :
    CliktCommand(name = "analyze", help = "Run the build and analyze any failure"),
    KoinComponent {

    private val buildExecutor: BuildExecutor by inject()
    private val failureAnalyzer: FailureAnalyzer by inject()

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

        if (result.isSuccess) {
            echo("Build succeeded. Nothing to analyze.")
            return
        }

        echo(FailureAnalysisFormatter.format(failureAnalyzer.analyze(result)))
        throw ProgramResult(1)
    }
}
