package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.ProgressException
import core.progress.ProgressTracker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/** `ado progress show` — prints the recorded progress state. Read-only; never writes files. */
class ShowProgressCommand :
    CliktCommand(name = "show", help = "Print the recorded progress state"),
    KoinComponent {

    private val progressTracker: ProgressTracker by inject()

    private val path: String by option("--path", help = "Repository path to read .ado/progress.yaml from (defaults to the current directory)")
        .default(".")

    override fun run() {
        val repositoryPath = Paths.get(path).toAbsolutePath().normalize()

        val state = try {
            progressTracker.loadProgress(repositoryPath)
        } catch (e: ProgressException) {
            echo(e.message ?: "Failed to load progress", err = true)
            throw ProgramResult(1)
        }

        echo(ProgressStateFormatter.format(state))
    }
}
