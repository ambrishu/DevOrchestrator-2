package core.validation

import core.build.BuildExecutor
import core.configuration.ConfigurationLoader
import models.AdoConfiguration
import models.QualityGateReport
import utils.ProcessExecutor
import java.nio.file.Path

/**
 * [QualityGateEngine] running the six documented mandatory gates in order: build, formatting,
 * static analysis, unit tests, integration tests, architecture validation.
 *
 * Build and unit tests always run (their commands always have a default). The other four run
 * only when configured, per the documented schema gap around per-gate commands.
 */
class DefaultQualityGateEngine(
    private val configurationLoader: ConfigurationLoader,
    private val buildExecutor: BuildExecutor,
    private val processExecutor: ProcessExecutor,
) : QualityGateEngine {

    override fun runQualityGates(repositoryPath: Path): QualityGateReport {
        val configPath = repositoryPath.resolve(".ado").resolve("config.yaml")
        val config = if (configurationLoader.exists(configPath)) configurationLoader.load(configPath) else AdoConfiguration()

        val gates: List<QualityGate> = listOf(
            BuildQualityGate(buildExecutor),
            CommandQualityGate("formatting", config.formatting.command, processExecutor),
            CommandQualityGate("staticAnalysis", config.staticAnalysis.command, processExecutor),
            CommandQualityGate("unitTest", config.test.command, processExecutor),
            CommandQualityGate("integrationTest", config.integrationTest.command, processExecutor),
            CommandQualityGate("architectureValidation", config.architectureValidation.command, processExecutor),
        )

        return QualityGateReport(gates.map { it.execute(repositoryPath) })
    }
}
