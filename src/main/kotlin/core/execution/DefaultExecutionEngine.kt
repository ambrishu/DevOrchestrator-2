package core.execution

import core.agent.AgentAdapter
import core.common.exception.AdoException
import core.context.ContextBuilder
import core.planner.StoryLoader
import core.planner.StoryPlanner
import core.progress.ProgressTracker
import io.github.oshai.kotlinlogging.KotlinLogging
import models.ExecutionSummary
import models.StoryExecutionResult
import models.StorySelection
import models.StoryStatus
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/**
 * [ExecutionEngine] coordinating the Story Planner, Context Builder, Agent Adapter, and
 * Progress Tracker built in earlier milestones.
 *
 * Any failure while building context or invoking the agent blocks that story and stops the run:
 * without a Repair Loop (a later milestone), such failures are non-recoverable here.
 */
class DefaultExecutionEngine(
    private val storyLoader: StoryLoader,
    private val storyPlanner: StoryPlanner,
    private val progressTracker: ProgressTracker,
    private val contextBuilder: ContextBuilder,
    private val agentAdapter: AgentAdapter,
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
            progress = progressTracker.updateStatus(repositoryPath, story.id, StoryStatus.IN_PROGRESS)

            val outcome = try {
                val context = contextBuilder.buildContext(story, repositoryPath)
                val generation = agentAdapter.generate(context, repositoryPath)
                progress = progressTracker.updateStatus(repositoryPath, story.id, StoryStatus.REVIEW)
                StoryExecutionResult(story.id, StoryStatus.REVIEW, generation.summary)
            } catch (e: AdoException) {
                logger.warn(e) { "${story.id} failed and will be blocked" }
                progress = progressTracker.updateStatus(repositoryPath, story.id, StoryStatus.BLOCKED)
                StoryExecutionResult(story.id, StoryStatus.BLOCKED, e.message ?: "Execution failed")
            }

            results.add(outcome)
            if (outcome.status == StoryStatus.BLOCKED) break
        }

        return ExecutionSummary(results)
    }
}
