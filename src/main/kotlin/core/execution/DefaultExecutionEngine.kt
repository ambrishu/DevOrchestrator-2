package core.execution

import core.agent.AgentAdapter
import core.build.BuildExecutor
import core.common.exception.AdoException
import core.configuration.ConfigurationLoader
import core.context.ContextBuilder
import core.git.CommitMessageFormatter
import core.git.GitManager
import core.planner.StoryLoader
import core.planner.StoryPlanner
import core.progress.ProgressTracker
import core.repair.RepairContextBuilder
import core.repair.RepairLoop
import core.review.CodeReviewAgent
import core.validation.QualityGateEngine
import io.github.oshai.kotlinlogging.KotlinLogging
import models.AdoConfiguration
import models.ContextPackage
import models.ExecutionSummary
import models.Story
import models.StoryExecutionResult
import models.StorySelection
import models.StoryStatus
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/**
 * [ExecutionEngine] implementing the full documented lifecycle (`docs/06-execution-engine.md`
 * §7): select -> build context -> invoke agent -> execute build -> repair until successful ->
 * quality gates -> AI review -> commit -> update progress, looping until no executable story
 * remains.
 *
 * A blocking code review finding is itself repaired the same way a build failure is: the finding
 * is folded back into context, the agent is invoked again, and the build/gates/review sequence
 * re-runs, up to `repair.retries` attempts — a separate budget from build-failure repair, since
 * the two are independent concerns and a story could need both in the same run.
 *
 * Code review runs only when `review.enabled` is true (the default). Quality gate failures are
 * not retried (no documented repair path for them); any other failure — context assembly, agent
 * invocation, exhausted repair retries of either kind, or a failed commit — blocks the story and
 * stops the run.
 */
class DefaultExecutionEngine(
    private val storyLoader: StoryLoader,
    private val storyPlanner: StoryPlanner,
    private val progressTracker: ProgressTracker,
    private val contextBuilder: ContextBuilder,
    private val agentAdapter: AgentAdapter,
    private val buildExecutor: BuildExecutor,
    private val repairLoop: RepairLoop,
    private val repairContextBuilder: RepairContextBuilder,
    private val qualityGateEngine: QualityGateEngine,
    private val codeReviewAgent: CodeReviewAgent,
    private val gitManager: GitManager,
    private val commitMessageFormatter: CommitMessageFormatter,
    private val configurationLoader: ConfigurationLoader,
) : ExecutionEngine {

    override fun run(repositoryPath: Path): ExecutionSummary {
        val stories = storyLoader.loadStories(repositoryPath)
        var progress = progressTracker.loadProgress(repositoryPath)
        val results = mutableListOf<StoryExecutionResult>()

        while (true) {
            val effectiveStories = ProgressOverlay.apply(stories, progress)
            val story = when (val selection = storyPlanner.selectNext(effectiveStories)) {
                is StorySelection.Selected -> selection.story
                StorySelection.None -> break
            }

            logger.info { "Executing ${story.id} — ${story.title}" }
            progressTracker.updateStatus(repositoryPath, story.id, StoryStatus.IN_PROGRESS)

            val outcome = try {
                executeStory(story, repositoryPath)
            } catch (e: AdoException) {
                logger.warn(e) { "${story.id} failed and will be blocked" }
                StoryExecutionResult(story.id, StoryStatus.BLOCKED, e.message ?: "Execution failed")
            }

            progress = progressTracker.updateStatus(repositoryPath, story.id, outcome.status)
            results.add(outcome)
            if (outcome.status == StoryStatus.BLOCKED) break
        }

        return ExecutionSummary(results)
    }

    private fun executeStory(story: Story, repositoryPath: Path): StoryExecutionResult {
        val config = loadConfig(repositoryPath)

        var context = contextBuilder.buildContext(story, repositoryPath)
        agentAdapter.generate(context, repositoryPath)

        ensureBuildSucceeds(context, repositoryPath, config)?.let { return it }

        var reviewAttempts = 0
        while (true) {
            val qualityReport = qualityGateEngine.runQualityGates(repositoryPath)
            if (!qualityReport.allPassed) {
                val failedGates = qualityReport.results.filter { !it.passed }.joinToString(", ") { it.name }
                return StoryExecutionResult(story.id, StoryStatus.BLOCKED, "Quality gates failed: $failedGates")
            }

            if (!config.review.enabled) break

            val reviewResult = codeReviewAgent.review(context, repositoryPath)
            if (!reviewResult.hasBlockingIssues) break

            if (reviewAttempts >= config.repair.retries) {
                return StoryExecutionResult(
                    story.id,
                    StoryStatus.BLOCKED,
                    "Code review blocked after $reviewAttempts repair attempt(s): " +
                        reviewResult.blockingIssues.joinToString("; "),
                )
            }

            reviewAttempts++
            logger.info { "Review repair attempt $reviewAttempts/${config.repair.retries} for ${story.id}" }
            context = repairContextBuilder.buildReviewRepairContext(context, reviewResult)
            agentAdapter.generate(context, repositoryPath)

            ensureBuildSucceeds(context, repositoryPath, config)?.let { return it }
        }

        val commitResult = gitManager.createCommit(repositoryPath, commitMessageFormatter.format(story))
        if (!commitResult.success) {
            return StoryExecutionResult(story.id, StoryStatus.BLOCKED, "Commit failed: ${commitResult.failureReason}")
        }

        return StoryExecutionResult(story.id, StoryStatus.DONE, "Committed ${commitResult.commitSha}")
    }

    /** Runs the build, repairing via [repairLoop] if it fails. Null means the build now succeeds. */
    private fun ensureBuildSucceeds(context: ContextPackage, repositoryPath: Path, config: AdoConfiguration): StoryExecutionResult? {
        val buildResult = buildExecutor.executeBuild(repositoryPath)
        if (buildResult.isSuccess) return null

        val repairResult = repairLoop.repair(context, repositoryPath, buildResult, config.repair.retries)
        if (!repairResult.succeeded) {
            return StoryExecutionResult(
                context.story.id,
                StoryStatus.BLOCKED,
                "Build failed after ${repairResult.attempts} repair attempt(s)",
            )
        }
        return null
    }

    private fun loadConfig(repositoryPath: Path): AdoConfiguration {
        val configPath = repositoryPath.resolve(".ado").resolve("config.yaml")
        return if (configurationLoader.exists(configPath)) configurationLoader.load(configPath) else AdoConfiguration()
    }
}
