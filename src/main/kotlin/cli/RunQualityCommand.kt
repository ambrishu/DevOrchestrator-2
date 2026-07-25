package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.ConfigurationException
import core.validation.QualityGateEngine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/** `ado quality run` — runs every mandatory quality gate and prints the report. */
class RunQualityCommand :
    CliktCommand(name = "run", help = "Run every mandatory quality gate"),
    KoinComponent {

    private val qualityGateEngine: QualityGateEngine by inject()

    private val path: String by option("--path", help = "Repository path to validate (defaults to the current directory)")
        .default(".")

    override fun run() {
        val repositoryPath = Paths.get(path).toAbsolutePath().normalize()

        val report = try {
            qualityGateEngine.runQualityGates(repositoryPath)
        } catch (e: ConfigurationException) {
            echo(e.message ?: "Failed to load configuration", err = true)
            throw ProgramResult(1)
        }

        echo(QualityGateReportFormatter.format(report))

        if (!report.allPassed) {
            throw ProgramResult(1)
        }
    }
}
