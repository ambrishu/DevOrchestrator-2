package core.repair

import core.agent.AgentAdapter
import core.build.BuildExecutor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.BuildResult
import models.BuildStatus
import models.ContextPackage
import models.FailureAnalysis
import models.FailureCategory
import models.GenerationResult
import models.Story
import models.StoryStatus
import java.nio.file.Path

class DefaultRepairLoopTest : FunSpec({

    val repositoryPath = Path.of("/repo")
    val story = Story(id = "ADO-001", title = "Example", description = "d", status = StoryStatus.TODO)
    val context = ContextPackage(story, emptyList())

    fun failure(message: String) = BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = message, durationMillis = 5)
    fun success() = BuildResult(status = BuildStatus.SUCCESS, stdout = "built", stderr = "", durationMillis = 5)

    test("succeeds on the first repair attempt") {
        val initialFailure = failure("boom")
        val analysis = FailureAnalysis(FailureCategory.COMPILATION, listOf("boom"))
        val repairContext = context.copy(failureAnalysis = analysis)

        val failureAnalyzer = mockk<FailureAnalyzer> { every { analyze(initialFailure) } returns analysis }
        val repairContextBuilder = mockk<RepairContextBuilder> {
            every { buildRepairContext(context, analysis) } returns repairContext
        }
        val agentAdapter = mockk<AgentAdapter> {
            every { generate(repairContext, repositoryPath) } returns GenerationResult(summary = "fixed")
        }
        val buildExecutor = mockk<BuildExecutor> { every { executeBuild(repositoryPath) } returns success() }

        val loop = DefaultRepairLoop(failureAnalyzer, repairContextBuilder, agentAdapter, buildExecutor)
        val result = loop.repair(context, repositoryPath, initialFailure, maxRetries = 5)

        result.succeeded shouldBe true
        result.attempts shouldBe 1
        verify(exactly = 1) { agentAdapter.generate(repairContext, repositoryPath) }
        verify(exactly = 1) { buildExecutor.executeBuild(repositoryPath) }
    }

    test("makes no attempt and returns the initial failure when maxRetries is 0") {
        val initialFailure = failure("boom")
        val failureAnalyzer = mockk<FailureAnalyzer>()
        val repairContextBuilder = mockk<RepairContextBuilder>()
        val agentAdapter = mockk<AgentAdapter>()
        val buildExecutor = mockk<BuildExecutor>()

        val loop = DefaultRepairLoop(failureAnalyzer, repairContextBuilder, agentAdapter, buildExecutor)
        val result = loop.repair(context, repositoryPath, initialFailure, maxRetries = 0)

        result.attempts shouldBe 0
        result.finalBuildResult shouldBe initialFailure
        verify(exactly = 0) { agentAdapter.generate(any(), any()) }
        verify(exactly = 0) { buildExecutor.executeBuild(any()) }
    }

    test("stops as soon as the build succeeds, without using the remaining retry budget") {
        val initialFailure = failure("boom")
        val analysis = FailureAnalysis(FailureCategory.COMPILATION, listOf("boom"))
        val repairContext = context.copy(failureAnalysis = analysis)

        val failureAnalyzer = mockk<FailureAnalyzer> { every { analyze(initialFailure) } returns analysis }
        val repairContextBuilder = mockk<RepairContextBuilder> {
            every { buildRepairContext(context, analysis) } returns repairContext
        }
        val agentAdapter = mockk<AgentAdapter> {
            every { generate(repairContext, repositoryPath) } returns GenerationResult(summary = "fixed")
        }
        val buildExecutor = mockk<BuildExecutor> { every { executeBuild(repositoryPath) } returns success() }

        val loop = DefaultRepairLoop(failureAnalyzer, repairContextBuilder, agentAdapter, buildExecutor)
        val result = loop.repair(context, repositoryPath, initialFailure, maxRetries = 5)

        result.attempts shouldBe 1
        verify(exactly = 1) { failureAnalyzer.analyze(any()) }
    }

    test("exhausts the retry limit when every rebuild still fails") {
        val initialFailure = failure("boom")
        val analysis = FailureAnalysis(FailureCategory.COMPILATION, listOf("boom"))
        val repairContext = context.copy(failureAnalysis = analysis)

        val failureAnalyzer = mockk<FailureAnalyzer> { every { analyze(any()) } returns analysis }
        val repairContextBuilder = mockk<RepairContextBuilder> {
            every { buildRepairContext(context, analysis) } returns repairContext
        }
        val agentAdapter = mockk<AgentAdapter> {
            every { generate(repairContext, repositoryPath) } returns GenerationResult(summary = "attempted fix")
        }
        val buildExecutor = mockk<BuildExecutor> { every { executeBuild(repositoryPath) } returns failure("still broken") }

        val loop = DefaultRepairLoop(failureAnalyzer, repairContextBuilder, agentAdapter, buildExecutor)
        val result = loop.repair(context, repositoryPath, initialFailure, maxRetries = 3)

        result.succeeded shouldBe false
        result.attempts shouldBe 3
        verify(exactly = 3) { agentAdapter.generate(repairContext, repositoryPath) }
        verify(exactly = 3) { buildExecutor.executeBuild(repositoryPath) }
    }

    test("analyzes the most recent failure on each attempt, not the stale initial one") {
        val initialFailure = failure("first problem")
        val secondFailure = failure("second problem")
        val analysisOne = FailureAnalysis(FailureCategory.COMPILATION, listOf("first problem"))
        val analysisTwo = FailureAnalysis(FailureCategory.TESTING, listOf("second problem"))

        val failureAnalyzer = mockk<FailureAnalyzer>()
        every { failureAnalyzer.analyze(initialFailure) } returns analysisOne
        every { failureAnalyzer.analyze(secondFailure) } returns analysisTwo

        val repairContextBuilder = mockk<RepairContextBuilder>()
        every { repairContextBuilder.buildRepairContext(context, analysisOne) } returns context.copy(failureAnalysis = analysisOne)
        every { repairContextBuilder.buildRepairContext(context, analysisTwo) } returns context.copy(failureAnalysis = analysisTwo)

        val agentAdapter = mockk<AgentAdapter>()
        every { agentAdapter.generate(any(), repositoryPath) } returns GenerationResult(summary = "attempted fix")

        val buildExecutor = mockk<BuildExecutor>()
        every { buildExecutor.executeBuild(repositoryPath) } returnsMany listOf(secondFailure, success())

        val loop = DefaultRepairLoop(failureAnalyzer, repairContextBuilder, agentAdapter, buildExecutor)
        val result = loop.repair(context, repositoryPath, initialFailure, maxRetries = 5)

        result.succeeded shouldBe true
        result.attempts shouldBe 2
        verify(exactly = 1) { failureAnalyzer.analyze(initialFailure) }
        verify(exactly = 1) { failureAnalyzer.analyze(secondFailure) }
    }
})
