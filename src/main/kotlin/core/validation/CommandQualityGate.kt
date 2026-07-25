package core.validation

import core.common.exception.ProcessExecutionException
import models.QualityGateResult
import utils.ProcessExecutor
import java.nio.file.Path

/**
 * [QualityGate] running a single configured shell command, interpreting its exit code.
 *
 * A blank [command] means this gate is not configured for the repository; it passes vacuously
 * rather than failing for a tool the repository may not use.
 */
class CommandQualityGate(
    override val name: String,
    private val command: String,
    private val processExecutor: ProcessExecutor,
) : QualityGate {

    override fun execute(repositoryPath: Path): QualityGateResult {
        if (command.isBlank()) {
            return QualityGateResult(name, passed = true, details = "Gate not configured; skipped.")
        }

        return try {
            val result = processExecutor.execute(command.split(" "), repositoryPath)
            QualityGateResult(name, result.isSuccess, if (result.isSuccess) "" else result.stderr.ifBlank { result.stdout })
        } catch (e: ProcessExecutionException) {
            QualityGateResult(name, passed = false, details = e.message ?: "Failed to start command \"$command\"")
        }
    }
}
