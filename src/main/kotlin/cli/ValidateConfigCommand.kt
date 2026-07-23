package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.ConfigurationException
import core.configuration.ConfigurationLoader
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/** `ado config validate` — validates `.ado/config.yaml`. Read-only; never writes files. */
class ValidateConfigCommand :
    CliktCommand(name = "validate", help = "Validate the ADO configuration file"),
    KoinComponent {

    private val configurationLoader: ConfigurationLoader by inject()

    private val path: String by option("--path", help = "Repository path to validate configuration for (defaults to the current directory)")
        .default(".")

    override fun run() {
        val repositoryPath = Paths.get(path).toAbsolutePath().normalize()
        val configPath = repositoryPath.resolve(".ado").resolve("config.yaml")

        if (!configurationLoader.exists(configPath)) {
            echo("Configuration file not found: $configPath", err = true)
            throw ProgramResult(1)
        }

        try {
            configurationLoader.load(configPath)
            echo("Configuration is valid: $configPath")
        } catch (e: ConfigurationException) {
            echo(e.message ?: "Configuration is invalid", err = true)
            throw ProgramResult(1)
        }
    }
}
