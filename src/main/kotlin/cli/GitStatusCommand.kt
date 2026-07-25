package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.GitOperationException
import core.git.GitManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/** `ado git status` — prints the repository's working tree status. Read-only. */
class GitStatusCommand :
    CliktCommand(name = "status", help = "Print the repository's working tree status"),
    KoinComponent {

    private val gitManager: GitManager by inject()

    private val path: String by option("--path", help = "Repository path to inspect (defaults to the current directory)")
        .default(".")

    override fun run() {
        val repositoryPath = Paths.get(path).toAbsolutePath().normalize()

        val status = try {
            gitManager.inspectStatus(repositoryPath)
        } catch (e: GitOperationException) {
            echo(e.message ?: "Failed to read repository status", err = true)
            throw ProgramResult(1)
        }

        echo(RepositoryStatusFormatter.format(status))
    }
}
