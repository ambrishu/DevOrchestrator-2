package core.execution

import core.agent.AgentAdapter
import core.build.BuildExecutor
import core.common.exception.AgentInvocationException
import core.common.exception.ContextException
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
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.AdoConfiguration
import models.BuildResult
import models.BuildStatus
import models.CommitResult
import models.ContextPackage
import models.GenerationResult
import models.ProgressState
import models.QualityGateReport
import models.QualityGateResult
import models.RepairConfig
import models.RepairResult
import models.ReviewConfig
import models.ReviewResult
import models.Story
import models.StoryExecutionResult
import models.StorySelection
import models.StoryStatus
import java.nio.file.Path

class DefaultExecutionEngineTest : FunSpec({

    val repositoryPath = Path.of("/repo")
    val configPath = repositoryPath.resolve(".ado").resolve("config.yaml")

    fun story(id: String) = Story(id = id, title = id, description = "", status = StoryStatus.TODO)

    fun successfulBuild() = BuildResult(status = BuildStatus.SUCCESS, stdout = "built", stderr = "", durationMillis = 5)
    fun failedBuild(message: String = "boom") = BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = message, durationMillis = 5)
    fun passingGates() = QualityGateReport(listOf(QualityGateResult("build", true)))

    /** Real in-memory fake: progress must actually accumulate across loop iterations for these tests to mean anything. */
    class FakeProgressTracker : ProgressTracker {
        var state = ProgressState()
        override fun loadProgress(repositoryPath: Path) = state
        override fun saveProgress(repositoryPath: Path, state: ProgressState) {
            this.state = state
        }

        override fun updateStatus(repositoryPath: Path, storyId: String, status: StoryStatus): ProgressState {
            state = state.withStatus(storyId, status)
            return state
        }
    }

    class Fixture {
        val storyLoader = mockk<StoryLoader>()
        val storyPlanner = mockk<StoryPlanner>()
        val progressTracker = FakeProgressTracker()
        val contextBuilder = mockk<ContextBuilder>()
        val agentAdapter = mockk<AgentAdapter>()
        val buildExecutor = mockk<BuildExecutor>()
        val repairLoop = mockk<RepairLoop>()
        val repairContextBuilder = mockk<RepairContextBuilder>()
        val qualityGateEngine = mockk<QualityGateEngine>()
        val codeReviewAgent = mockk<CodeReviewAgent>()
        val gitManager = mockk<GitManager>()
        val commitMessageFormatter = mockk<CommitMessageFormatter>()
        val configurationLoader = mockk<ConfigurationLoader>()

        init {
            every { configurationLoader.exists(configPath) } returns false
            every { codeReviewAgent.review(any(), any()) } returns ReviewResult()
        }

        fun engine() = DefaultExecutionEngine(
            storyLoader, storyPlanner, progressTracker, contextBuilder, agentAdapter,
            buildExecutor, repairLoop, repairContextBuilder, qualityGateEngine, codeReviewAgent,
            gitManager, commitMessageFormatter, configurationLoader,
        )
    }

    test("returns an empty summary when no story is executable") {
        val fixture = Fixture()
        every { fixture.storyLoader.loadStories(repositoryPath) } returns emptyList()
        every { fixture.storyPlanner.selectNext(emptyList()) } returns StorySelection.None

        fixture.engine().run(repositoryPath).results shouldBe emptyList()
    }

    test("a story that builds, passes quality gates, and commits successfully reaches DONE") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val context = ContextPackage(storyA, emptyList())

        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA)
        every { fixture.storyPlanner.selectNext(listOf(storyA)) } returns StorySelection.Selected(storyA)
        every { fixture.storyPlanner.selectNext(listOf(storyA.copy(status = StoryStatus.DONE))) } returns StorySelection.None
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns context
        every { fixture.agentAdapter.generate(context, repositoryPath) } returns GenerationResult(summary = "done")
        every { fixture.buildExecutor.executeBuild(repositoryPath) } returns successfulBuild()
        every { fixture.qualityGateEngine.runQualityGates(repositoryPath) } returns passingGates()
        every { fixture.commitMessageFormatter.format(storyA) } returns "feat(ADO-001): example"
        every { fixture.gitManager.createCommit(repositoryPath, "feat(ADO-001): example") } returns
            CommitResult(success = true, commitSha = "abc123")

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(StoryExecutionResult("ADO-001", StoryStatus.DONE, "Committed abc123"))
        verify(exactly = 0) { fixture.repairLoop.repair(any(), any(), any(), any()) }
    }

    test("a build that fails and then succeeds after repair still reaches DONE") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val context = ContextPackage(storyA, emptyList())
        val initialFailure = failedBuild()

        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA)
        every { fixture.storyPlanner.selectNext(listOf(storyA)) } returns StorySelection.Selected(storyA)
        every { fixture.storyPlanner.selectNext(listOf(storyA.copy(status = StoryStatus.DONE))) } returns StorySelection.None
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns context
        every { fixture.agentAdapter.generate(context, repositoryPath) } returns GenerationResult(summary = "done")
        every { fixture.buildExecutor.executeBuild(repositoryPath) } returns initialFailure
        every { fixture.repairLoop.repair(context, repositoryPath, initialFailure, 5) } returns
            RepairResult(successfulBuild(), attempts = 1)
        every { fixture.qualityGateEngine.runQualityGates(repositoryPath) } returns passingGates()
        every { fixture.commitMessageFormatter.format(storyA) } returns "feat(ADO-001): example"
        every { fixture.gitManager.createCommit(repositoryPath, "feat(ADO-001): example") } returns
            CommitResult(success = true, commitSha = "abc123")

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(StoryExecutionResult("ADO-001", StoryStatus.DONE, "Committed abc123"))
    }

    test("reads the repair retry limit from configuration when a config file exists") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val context = ContextPackage(storyA, emptyList())
        val initialFailure = failedBuild()

        every { fixture.configurationLoader.exists(configPath) } returns true
        every { fixture.configurationLoader.load(configPath) } returns AdoConfiguration(repair = RepairConfig(retries = 2))
        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA)
        every { fixture.storyPlanner.selectNext(any()) } returns StorySelection.Selected(storyA)
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns context
        every { fixture.agentAdapter.generate(context, repositoryPath) } returns GenerationResult(summary = "done")
        every { fixture.buildExecutor.executeBuild(repositoryPath) } returns initialFailure
        every { fixture.repairLoop.repair(context, repositoryPath, initialFailure, 2) } returns
            RepairResult(initialFailure, attempts = 2)

        fixture.engine().run(repositoryPath)

        verify { fixture.repairLoop.repair(context, repositoryPath, initialFailure, 2) }
    }

    test("blocks the story and stops the run when repair exhausts its retries") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val storyB = story("ADO-002")
        val context = ContextPackage(storyA, emptyList())
        val initialFailure = failedBuild()

        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA, storyB)
        every { fixture.storyPlanner.selectNext(any()) } returns StorySelection.Selected(storyA)
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns context
        every { fixture.agentAdapter.generate(context, repositoryPath) } returns GenerationResult(summary = "done")
        every { fixture.buildExecutor.executeBuild(repositoryPath) } returns initialFailure
        every { fixture.repairLoop.repair(context, repositoryPath, initialFailure, 5) } returns
            RepairResult(initialFailure, attempts = 5)

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(
            StoryExecutionResult("ADO-001", StoryStatus.BLOCKED, "Build failed after 5 repair attempt(s)"),
        )
        verify(exactly = 1) { fixture.storyPlanner.selectNext(any()) }
        verify(exactly = 0) { fixture.qualityGateEngine.runQualityGates(any()) }
    }

    test("blocks the story when a quality gate fails, naming the failed gate") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val context = ContextPackage(storyA, emptyList())

        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA)
        every { fixture.storyPlanner.selectNext(any()) } returns StorySelection.Selected(storyA)
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns context
        every { fixture.agentAdapter.generate(context, repositoryPath) } returns GenerationResult(summary = "done")
        every { fixture.buildExecutor.executeBuild(repositoryPath) } returns successfulBuild()
        every { fixture.qualityGateEngine.runQualityGates(repositoryPath) } returns
            QualityGateReport(listOf(QualityGateResult("build", true), QualityGateResult("unitTest", false)))

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(
            StoryExecutionResult("ADO-001", StoryStatus.BLOCKED, "Quality gates failed: unitTest"),
        )
        verify(exactly = 0) { fixture.codeReviewAgent.review(any(), any()) }
        verify(exactly = 0) { fixture.gitManager.createCommit(any(), any()) }
    }

    test("blocks the story when code review keeps finding a blocking issue with no retries configured") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val context = ContextPackage(storyA, emptyList())

        every { fixture.configurationLoader.exists(configPath) } returns true
        every { fixture.configurationLoader.load(configPath) } returns AdoConfiguration(repair = RepairConfig(retries = 0))
        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA)
        every { fixture.storyPlanner.selectNext(any()) } returns StorySelection.Selected(storyA)
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns context
        every { fixture.agentAdapter.generate(context, repositoryPath) } returns GenerationResult(summary = "done")
        every { fixture.buildExecutor.executeBuild(repositoryPath) } returns successfulBuild()
        every { fixture.qualityGateEngine.runQualityGates(repositoryPath) } returns passingGates()
        every { fixture.codeReviewAgent.review(context, repositoryPath) } returns
            ReviewResult(blockingIssues = listOf("Thread-unsafe access to shared state"))

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(
            StoryExecutionResult(
                "ADO-001",
                StoryStatus.BLOCKED,
                "Code review blocked after 0 repair attempt(s): Thread-unsafe access to shared state",
            ),
        )
        verify(exactly = 0) { fixture.gitManager.createCommit(any(), any()) }
    }

    test("repairs a review-blocking issue and reaches DONE once the second review passes") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val context = ContextPackage(storyA, emptyList())
        val analysis = ReviewResult(blockingIssues = listOf("Missing .gitignore"))
        val repairedContext = context.copy(reviewFeedback = analysis.blockingIssues)

        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA)
        every { fixture.storyPlanner.selectNext(listOf(storyA)) } returns StorySelection.Selected(storyA)
        every { fixture.storyPlanner.selectNext(listOf(storyA.copy(status = StoryStatus.DONE))) } returns StorySelection.None
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns context
        every { fixture.buildExecutor.executeBuild(repositoryPath) } returns successfulBuild()
        every { fixture.qualityGateEngine.runQualityGates(repositoryPath) } returns passingGates()
        every { fixture.agentAdapter.generate(context, repositoryPath) } returns GenerationResult(summary = "done")
        every { fixture.agentAdapter.generate(repairedContext, repositoryPath) } returns GenerationResult(summary = "fixed")
        every { fixture.codeReviewAgent.review(context, repositoryPath) } returns analysis
        every { fixture.codeReviewAgent.review(repairedContext, repositoryPath) } returns ReviewResult()
        every { fixture.repairContextBuilder.buildReviewRepairContext(context, analysis) } returns repairedContext
        every { fixture.commitMessageFormatter.format(storyA) } returns "feat(ADO-001): example"
        every { fixture.gitManager.createCommit(repositoryPath, "feat(ADO-001): example") } returns
            CommitResult(success = true, commitSha = "abc123")

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(StoryExecutionResult("ADO-001", StoryStatus.DONE, "Committed abc123"))
        verify(exactly = 1) { fixture.agentAdapter.generate(repairedContext, repositoryPath) }
        verify(exactly = 2) { fixture.codeReviewAgent.review(any(), repositoryPath) }
    }

    test("blocks after exhausting review-repair retries, without ever committing") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val context = ContextPackage(storyA, emptyList())
        val analysis = ReviewResult(blockingIssues = listOf("Still not fixed"))
        val repairedContext = context.copy(reviewFeedback = analysis.blockingIssues)

        every { fixture.configurationLoader.exists(configPath) } returns true
        every { fixture.configurationLoader.load(configPath) } returns AdoConfiguration(repair = RepairConfig(retries = 2))
        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA)
        every { fixture.storyPlanner.selectNext(any()) } returns StorySelection.Selected(storyA)
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns context
        every { fixture.buildExecutor.executeBuild(repositoryPath) } returns successfulBuild()
        every { fixture.qualityGateEngine.runQualityGates(repositoryPath) } returns passingGates()
        every { fixture.agentAdapter.generate(any(), repositoryPath) } returns GenerationResult(summary = "attempt")
        every { fixture.codeReviewAgent.review(any(), repositoryPath) } returns analysis
        every { fixture.repairContextBuilder.buildReviewRepairContext(any(), analysis) } returns repairedContext

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(
            StoryExecutionResult("ADO-001", StoryStatus.BLOCKED, "Code review blocked after 2 repair attempt(s): Still not fixed"),
        )
        verify(exactly = 3) { fixture.codeReviewAgent.review(any(), repositoryPath) }
        verify(exactly = 0) { fixture.gitManager.createCommit(any(), any()) }
    }

    test("skips code review entirely when review.enabled is false") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val context = ContextPackage(storyA, emptyList())

        every { fixture.configurationLoader.exists(configPath) } returns true
        every { fixture.configurationLoader.load(configPath) } returns AdoConfiguration(review = ReviewConfig(enabled = false))
        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA)
        every { fixture.storyPlanner.selectNext(listOf(storyA)) } returns StorySelection.Selected(storyA)
        every { fixture.storyPlanner.selectNext(listOf(storyA.copy(status = StoryStatus.DONE))) } returns StorySelection.None
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns context
        every { fixture.agentAdapter.generate(context, repositoryPath) } returns GenerationResult(summary = "done")
        every { fixture.buildExecutor.executeBuild(repositoryPath) } returns successfulBuild()
        every { fixture.qualityGateEngine.runQualityGates(repositoryPath) } returns passingGates()
        every { fixture.commitMessageFormatter.format(storyA) } returns "feat(ADO-001): example"
        every { fixture.gitManager.createCommit(repositoryPath, "feat(ADO-001): example") } returns
            CommitResult(success = true, commitSha = "abc123")

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(StoryExecutionResult("ADO-001", StoryStatus.DONE, "Committed abc123"))
        verify(exactly = 0) { fixture.codeReviewAgent.review(any(), any()) }
    }

    test("blocks the story when the commit fails") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val context = ContextPackage(storyA, emptyList())

        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA)
        every { fixture.storyPlanner.selectNext(any()) } returns StorySelection.Selected(storyA)
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns context
        every { fixture.agentAdapter.generate(context, repositoryPath) } returns GenerationResult(summary = "done")
        every { fixture.buildExecutor.executeBuild(repositoryPath) } returns successfulBuild()
        every { fixture.qualityGateEngine.runQualityGates(repositoryPath) } returns passingGates()
        every { fixture.commitMessageFormatter.format(storyA) } returns "feat(ADO-001): example"
        every { fixture.gitManager.createCommit(repositoryPath, "feat(ADO-001): example") } returns
            CommitResult(success = false, failureReason = "nothing to commit")

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(
            StoryExecutionResult("ADO-001", StoryStatus.BLOCKED, "Commit failed: nothing to commit"),
        )
    }

    test("blocks the story and never reaches the build when context building fails") {
        val fixture = Fixture()
        val storyA = story("ADO-001")

        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA)
        every { fixture.storyPlanner.selectNext(any()) } returns StorySelection.Selected(storyA)
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } throws ContextException("repository unreadable")

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(
            StoryExecutionResult("ADO-001", StoryStatus.BLOCKED, "repository unreadable"),
        )
        verify(exactly = 0) { fixture.buildExecutor.executeBuild(any()) }
    }

    test("blocks the story when the agent invocation fails") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val context = ContextPackage(storyA, emptyList())

        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA)
        every { fixture.storyPlanner.selectNext(any()) } returns StorySelection.Selected(storyA)
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns context
        every { fixture.agentAdapter.generate(context, repositoryPath) } throws AgentInvocationException("claude not found")

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(
            StoryExecutionResult("ADO-001", StoryStatus.BLOCKED, "claude not found"),
        )
        verify(exactly = 0) { fixture.buildExecutor.executeBuild(any()) }
    }

    test("processes two independent stories to DONE in a single run") {
        val fixture = Fixture()
        val storyA = story("ADO-001")
        val storyB = story("ADO-002")
        val contextA = ContextPackage(storyA, emptyList())
        val contextB = ContextPackage(storyB, emptyList())

        every { fixture.storyLoader.loadStories(repositoryPath) } returns listOf(storyA, storyB)
        every { fixture.storyPlanner.selectNext(any()) } returnsMany listOf(
            StorySelection.Selected(storyA),
            StorySelection.Selected(storyB),
            StorySelection.None,
        )
        every { fixture.contextBuilder.buildContext(storyA, repositoryPath) } returns contextA
        every { fixture.contextBuilder.buildContext(storyB, repositoryPath) } returns contextB
        every { fixture.agentAdapter.generate(any(), repositoryPath) } returns GenerationResult(summary = "done")
        every { fixture.buildExecutor.executeBuild(repositoryPath) } returns successfulBuild()
        every { fixture.qualityGateEngine.runQualityGates(repositoryPath) } returns passingGates()
        every { fixture.commitMessageFormatter.format(storyA) } returns "feat(ADO-001): a"
        every { fixture.commitMessageFormatter.format(storyB) } returns "feat(ADO-002): b"
        every { fixture.gitManager.createCommit(repositoryPath, "feat(ADO-001): a") } returns
            CommitResult(success = true, commitSha = "sha-a")
        every { fixture.gitManager.createCommit(repositoryPath, "feat(ADO-002): b") } returns
            CommitResult(success = true, commitSha = "sha-b")

        val summary = fixture.engine().run(repositoryPath)

        summary.results shouldBe listOf(
            StoryExecutionResult("ADO-001", StoryStatus.DONE, "Committed sha-a"),
            StoryExecutionResult("ADO-002", StoryStatus.DONE, "Committed sha-b"),
        )
    }
})
