package core.validation

import core.build.BuildExecutor
import core.common.exception.BuildExecutionException
import models.QualityGateResult
import java.nio.file.Path

/** [QualityGate] wrapping the [BuildExecutor]: the build must succeed. Always mandatory. */
class BuildQualityGate(
    private val buildExecutor: BuildExecutor,
) : QualityGate {

    override val name: String = "build"

    override fun execute(repositoryPath: Path): QualityGateResult = try {
        val result = buildExecutor.executeBuild(repositoryPath)
        QualityGateResult(name, result.isSuccess, if (result.isSuccess) "" else result.stderr.ifBlank { result.stdout })
    } catch (e: BuildExecutionException) {
        QualityGateResult(name, passed = false, details = e.message ?: "Failed to start the build")
    }
}
