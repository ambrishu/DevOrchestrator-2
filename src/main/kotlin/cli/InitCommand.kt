package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.ConfigurationException
import core.configuration.ConfigurationLoader
import core.repository.RepositoryLoader
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Path
import java.nio.file.Paths

/**
 * `ado init` — validates the repository and prints a readiness summary.
 *
 * This command is read-only: it never generates or modifies project files.
 */
class InitCommand :
    CliktCommand(name = "init", help = "Validate the repository and print a readiness summary"),
    KoinComponent {

    private val repositoryLoader: RepositoryLoader by inject()
    private val configurationLoader: ConfigurationLoader by inject()

    private val path: String by option("--path", help = "Repository path to validate (defaults to the current directory)")
        .default(".")

    override fun run() {
        val repositoryPath: Path = Paths.get(path).toAbsolutePath().normalize()
        val readiness = repositoryLoader.validate(repositoryPath)
        val configurationStatus = describeConfiguration(readiness.hasConfiguration, repositoryPath)

        echo(ReadinessSummaryFormatter.format(readiness, configurationStatus))

        if (!readiness.isReady) {
            throw ProgramResult(1)
        }
    }

    private fun describeConfiguration(hasConfiguration: Boolean, repositoryPath: Path): String {
        if (!hasConfiguration) {
            return "not found (.ado/config.yaml)"
        }

        val configPath = repositoryPath.resolve(".ado").resolve("config.yaml")
        return try {
            configurationLoader.load(configPath)
            "found and valid (.ado/config.yaml)"
        } catch (e: ConfigurationException) {
            "found but invalid (.ado/config.yaml): ${e.message}"
        }
    }
}
