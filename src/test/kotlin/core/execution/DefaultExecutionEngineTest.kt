package core.execution

import core.agent.AgentAdapter
import core.common.exception.AgentInvocationException
import core.common.exception.ContextException
import core.context.ContextBuilder
import core.planner.StoryLoader
import core.planner.StoryPlanner
import core.progress.ProgressTracker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.ContextPackage
import models.GenerationResult
import models.ProgressState
import models.Story
import models.StoryExecutionResult
import models.StorySelection
import models.StoryStatus
import java.nio.file.Path

class DefaultExecutionEngineTest : FunSpec({

    val repositoryPath = Path.of("/repo")

    fun story(id: String) = Story(id = id, title = id, description = "", status = StoryStatus.TODO)

    fun engineWith(
        storyLoader: StoryLoader,
        storyPlanner: StoryPlanner,
        progressTracker: ProgressTracker,
        contextBuilder: ContextBuilder,
        agentAdapter: AgentAdapter,
    ) = DefaultExecutionEngine(storyLoader, storyPlanner, progressTracker, contextBuilder, agentAdapter)

    test("returns an empty summary when no story is executable") {
        val storyLoader = mockk<StoryLoader> { every { loadStories(repositoryPath) } returns emptyList() }
        val progressTracker = mockk<ProgressTracker> { every { loadProgress(repositoryPath) } returns ProgressState() }
        val storyPlanner = mockk<StoryPlanner> { every { selectNext(emptyList()) } returns StorySelection.None }

        val summary = engineWith(storyLoader, storyPlanner, progressTracker, mockk(), mockk()).run(repositoryPath)

        summary.results shouldBe emptyList()
    }

    test("executes a single story: marks it in_progress, then review, and records the summary") {
        val storyA = story("ADO-001")
        val storyLoader = mockk<StoryLoader> { every { loadStories(repositoryPath) } returns listOf(storyA) }
        val progressTracker = mockk<ProgressTracker>()
        every { progressTracker.loadProgress(repositoryPath) } returns ProgressState()
        every { progressTracker.updateStatus(repositoryPath, "ADO-001", StoryStatus.IN_PROGRESS) } returns
            ProgressState().withStatus("ADO-001", StoryStatus.IN_PROGRESS)
        every { progressTracker.updateStatus(repositoryPath, "ADO-001", StoryStatus.REVIEW) } returns
            ProgressState().withStatus("ADO-001", StoryStatus.REVIEW)

        val storyPlanner = mockk<StoryPlanner>()
        every { storyPlanner.selectNext(listOf(storyA)) } returns StorySelection.Selected(storyA)
        every { storyPlanner.selectNext(listOf(storyA.copy(status = StoryStatus.REVIEW))) } returns StorySelection.None

        val context = ContextPackage(storyA, emptyList())
        val contextBuilder = mockk<ContextBuilder> { every { buildContext(storyA, repositoryPath) } returns context }
        val agentAdapter = mockk<AgentAdapter> {
            every { generate(context, repositoryPath) } returns GenerationResult(summary = "Implemented ADO-001.")
        }

        val summary = engineWith(storyLoader, storyPlanner, progressTracker, contextBuilder, agentAdapter).run(repositoryPath)

        summary.results shouldBe listOf(
            StoryExecutionResult("ADO-001", StoryStatus.REVIEW, "Implemented ADO-001."),
        )
        verify { progressTracker.updateStatus(repositoryPath, "ADO-001", StoryStatus.IN_PROGRESS) }
        verify { progressTracker.updateStatus(repositoryPath, "ADO-001", StoryStatus.REVIEW) }
    }

    test("processes multiple stories sequentially until none remain") {
        val storyA = story("ADO-001")
        val storyB = story("ADO-002")
        val storyLoader = mockk<StoryLoader> { every { loadStories(repositoryPath) } returns listOf(storyA, storyB) }

        val progressTracker = mockk<ProgressTracker>()
        every { progressTracker.loadProgress(repositoryPath) } returns ProgressState()
        every { progressTracker.updateStatus(repositoryPath, "ADO-001", StoryStatus.IN_PROGRESS) } returns
            ProgressState().withStatus("ADO-001", StoryStatus.IN_PROGRESS)
        every { progressTracker.updateStatus(repositoryPath, "ADO-001", StoryStatus.REVIEW) } returns
            ProgressState().withStatus("ADO-001", StoryStatus.REVIEW)
        every { progressTracker.updateStatus(repositoryPath, "ADO-002", StoryStatus.IN_PROGRESS) } returns
            ProgressState().withStatus("ADO-001", StoryStatus.REVIEW).withStatus("ADO-002", StoryStatus.IN_PROGRESS)
        every { progressTracker.updateStatus(repositoryPath, "ADO-002", StoryStatus.REVIEW) } returns
            ProgressState().withStatus("ADO-001", StoryStatus.REVIEW).withStatus("ADO-002", StoryStatus.REVIEW)

        val storyPlanner = mockk<StoryPlanner>()
        every { storyPlanner.selectNext(any()) } returnsMany listOf(
            StorySelection.Selected(storyA),
            StorySelection.Selected(storyB),
            StorySelection.None,
        )

        val contextA = ContextPackage(storyA, emptyList())
        val contextB = ContextPackage(storyB, emptyList())
        val contextBuilder = mockk<ContextBuilder>()
        every { contextBuilder.buildContext(storyA, repositoryPath) } returns contextA
        every { contextBuilder.buildContext(storyB, repositoryPath) } returns contextB

        val agentAdapter = mockk<AgentAdapter>()
        every { agentAdapter.generate(contextA, repositoryPath) } returns GenerationResult(summary = "done A")
        every { agentAdapter.generate(contextB, repositoryPath) } returns GenerationResult(summary = "done B")

        val summary = engineWith(storyLoader, storyPlanner, progressTracker, contextBuilder, agentAdapter).run(repositoryPath)

        summary.results.map { it.storyId } shouldBe listOf("ADO-001", "ADO-002")
        summary.results.map { it.status } shouldBe listOf(StoryStatus.REVIEW, StoryStatus.REVIEW)
    }

    test("blocks the story and stops the run when context building fails") {
        val storyA = story("ADO-001")
        val storyB = story("ADO-002")
        val storyLoader = mockk<StoryLoader> { every { loadStories(repositoryPath) } returns listOf(storyA, storyB) }
        val progressTracker = mockk<ProgressTracker>()
        every { progressTracker.loadProgress(repositoryPath) } returns ProgressState()
        every { progressTracker.updateStatus(repositoryPath, "ADO-001", StoryStatus.IN_PROGRESS) } returns ProgressState()
        every { progressTracker.updateStatus(repositoryPath, "ADO-001", StoryStatus.BLOCKED) } returns
            ProgressState().withStatus("ADO-001", StoryStatus.BLOCKED)

        val storyPlanner = mockk<StoryPlanner>()
        every { storyPlanner.selectNext(any()) } returns StorySelection.Selected(storyA)

        val contextBuilder = mockk<ContextBuilder> {
            every { buildContext(storyA, repositoryPath) } throws ContextException("repository unreadable")
        }
        val agentAdapter = mockk<AgentAdapter>()

        val summary = engineWith(storyLoader, storyPlanner, progressTracker, contextBuilder, agentAdapter).run(repositoryPath)

        summary.results shouldBe listOf(
            StoryExecutionResult("ADO-001", StoryStatus.BLOCKED, "repository unreadable"),
        )
        verify(exactly = 1) { storyPlanner.selectNext(any()) }
        verify(exactly = 0) { agentAdapter.generate(any(), any()) }
    }

    test("blocks the story and stops the run when the agent invocation fails") {
        val storyA = story("ADO-001")
        val storyLoader = mockk<StoryLoader> { every { loadStories(repositoryPath) } returns listOf(storyA) }
        val progressTracker = mockk<ProgressTracker>()
        every { progressTracker.loadProgress(repositoryPath) } returns ProgressState()
        every { progressTracker.updateStatus(repositoryPath, "ADO-001", StoryStatus.IN_PROGRESS) } returns ProgressState()
        every { progressTracker.updateStatus(repositoryPath, "ADO-001", StoryStatus.BLOCKED) } returns
            ProgressState().withStatus("ADO-001", StoryStatus.BLOCKED)

        val storyPlanner = mockk<StoryPlanner> { every { selectNext(any()) } returns StorySelection.Selected(storyA) }

        val context = ContextPackage(storyA, emptyList())
        val contextBuilder = mockk<ContextBuilder> { every { buildContext(storyA, repositoryPath) } returns context }
        val agentAdapter = mockk<AgentAdapter> {
            every { generate(context, repositoryPath) } throws AgentInvocationException("claude not found")
        }

        val summary = engineWith(storyLoader, storyPlanner, progressTracker, contextBuilder, agentAdapter).run(repositoryPath)

        summary.results shouldBe listOf(
            StoryExecutionResult("ADO-001", StoryStatus.BLOCKED, "claude not found"),
        )
        summary.hasBlockedStory shouldBe true
    }
})
