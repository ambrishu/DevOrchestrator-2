package core.build

import core.common.exception.BuildExecutionException
import core.common.exception.ProcessExecutionException
import core.configuration.ConfigurationLoader
import core.configuration.YamlConfigurationLoader
import models.AdoConfiguration
import models.BuildResult
import models.BuildStatus
import utils.DefaultProcessExecutor
import utils.ProcessExecutor
import java.nio.file.Path

/** [BuildExecutor] running the configured build command via the process execution abstraction. */
class DefaultBuildExecutor(
    private val configurationLoader: ConfigurationLoader = YamlConfigurationLoader(),
    private val processExecutor: ProcessExecutor = DefaultProcessExecutor(),
) : BuildExecutor {

    override fun executeBuild(repositoryPath: Path): BuildResult {
        val configPath = repositoryPath.resolve(".ado").resolve("config.yaml")
        val config = if (configurationLoader.exists(configPath)) configurationLoader.load(configPath) else AdoConfiguration()

        val result = try {
            processExecutor.execute(config.build.command.split(" "), repositoryPath)
        } catch (e: ProcessExecutionException) {
            throw BuildExecutionException("Failed to start build command \"${config.build.command}\"", e)
        }

        val warnings = (result.stdout.lineSequence() + result.stderr.lineSequence())
            .filter { it.contains("warning", ignoreCase = true) }
            .toList()

        return BuildResult(
            status = if (result.isSuccess) BuildStatus.SUCCESS else BuildStatus.FAILURE,
            stdout = result.stdout,
            stderr = result.stderr,
            warnings = warnings,
            durationMillis = result.durationMillis,
        )
    }
}
