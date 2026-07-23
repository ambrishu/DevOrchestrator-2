package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.ConfigurationException
import core.configuration.ConfigurationLoader
import models.AdoConfiguration
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/** `ado config show` — prints the effective configuration. Read-only; never writes files. */
class ShowConfigCommand :
    CliktCommand(name = "show", help = "Print the effective ADO configuration"),
    KoinComponent {

    private val configurationLoader: ConfigurationLoader by inject()

    private val path: String by option("--path", help = "Repository path to read configuration from (defaults to the current directory)")
        .default(".")

    override fun run() {
        val repositoryPath = Paths.get(path).toAbsolutePath().normalize()
        val configPath = repositoryPath.resolve(".ado").resolve("config.yaml")

        if (!configurationLoader.exists(configPath)) {
            echo(ConfigurationSummaryFormatter.format(AdoConfiguration(), "defaults (no .ado/config.yaml found)"))
            return
        }

        try {
            val config = configurationLoader.load(configPath)
            echo(ConfigurationSummaryFormatter.format(config, ".ado/config.yaml"))
        } catch (e: ConfigurationException) {
            echo(e.message ?: "Configuration is invalid", err = true)
            throw ProgramResult(1)
        }
    }
}
