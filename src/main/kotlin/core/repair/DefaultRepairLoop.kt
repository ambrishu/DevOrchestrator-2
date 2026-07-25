package core.repair

import core.agent.AgentAdapter
import core.build.BuildExecutor
import io.github.oshai.kotlinlogging.KotlinLogging
import models.BuildResult
import models.ContextPackage
import models.RepairResult
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/**
 * [RepairLoop] implementing the documented workflow: analyze -> build repair context -> invoke
 * agent -> rebuild -> repeat, until the build succeeds or [RepairLoop.repair]'s retry limit is
 * reached.
 */
class DefaultRepairLoop(
    private val failureAnalyzer: FailureAnalyzer,
    private val repairContextBuilder: RepairContextBuilder,
    private val agentAdapter: AgentAdapter,
    private val buildExecutor: BuildExecutor,
) : RepairLoop {

    override fun repair(context: ContextPackage, repositoryPath: Path, initialFailure: BuildResult, maxRetries: Int): RepairResult {
        var currentFailure = initialFailure
        var attempts = 0

        while (attempts < maxRetries && !currentFailure.isSuccess) {
            attempts++
            logger.info { "Repair attempt $attempts/$maxRetries for ${context.story.id}" }

            val analysis = failureAnalyzer.analyze(currentFailure)
            val repairContext = repairContextBuilder.buildRepairContext(context, analysis)
            agentAdapter.generate(repairContext, repositoryPath)
            currentFailure = buildExecutor.executeBuild(repositoryPath)
        }

        return RepairResult(finalBuildResult = currentFailure, attempts = attempts)
    }
}
