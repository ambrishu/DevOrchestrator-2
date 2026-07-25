package core.execution

import core.agent.AgentAdapter
import core.common.exception.AgentInvocationException
import core.context.DefaultContextBuilder
import core.planner.DefaultDependencyResolver
import core.planner.DefaultStoryPlanner
import core.planner.TasksFileLoader
import core.progress.FileProgressTracker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import models.GenerationResult
import models.StoryStatus
import java.nio.file.Files

private const val DIVIDER = "⸻"

class DefaultExecutionEngineIntegrationTest : FunSpec({

    test("runs end to end against a real repository using every real component except the agent") {
        val repo = Files.createTempDirectory("ado-execution-engine-integration-test")
        val docsDir = Files.createDirectory(repo.resolve("docs"))
        Files.writeString(
            docsDir.resolve("TASKS.md"),
            """
            ADO-001 — First Story

            Status: todo

            Depends On: None

            Description

            The first story, no dependencies.

            Acceptance Criteria

            * It works.

            $DIVIDER

            ADO-002 — Second Story

            Status: todo

            Depends On: None

            Description

            An independent second story.

            Acceptance Criteria

            * It also works.

            $DIVIDER
            """.trimIndent(),
        )

        val agentAdapter = mockk<AgentAdapter>()
        every { agentAdapter.generate(any(), any()) } returns GenerationResult(summary = "Implemented it.")

        val engine = DefaultExecutionEngine(
            storyLoader = TasksFileLoader(),
            storyPlanner = DefaultStoryPlanner(DefaultDependencyResolver()),
            progressTracker = FileProgressTracker(),
            contextBuilder = DefaultContextBuilder(),
            agentAdapter = agentAdapter,
        )

        val summary = engine.run(repo)

        summary.results.shouldHaveSize(2)
        summary.results.map { it.storyId } shouldBe listOf("ADO-001", "ADO-002")
        summary.results.map { it.status } shouldBe listOf(StoryStatus.REVIEW, StoryStatus.REVIEW)
        summary.hasBlockedStory shouldBe false

        val progressFile = repo.resolve(".ado").resolve("progress.yaml")
        Files.isRegularFile(progressFile) shouldBe true

        val persisted = FileProgressTracker().loadProgress(repo)
        persisted.statusOf("ADO-001") shouldBe StoryStatus.REVIEW
        persisted.statusOf("ADO-002") shouldBe StoryStatus.REVIEW
    }

    test("stops after the first story when the agent fails, leaving the second untouched") {
        val repo = Files.createTempDirectory("ado-execution-engine-integration-test")
        val docsDir = Files.createDirectory(repo.resolve("docs"))
        Files.writeString(
            docsDir.resolve("TASKS.md"),
            """
            ADO-001 — First Story

            Status: todo

            Depends On: None

            Description

            The first story.

            $DIVIDER

            ADO-002 — Second Story

            Status: todo

            Depends On: None

            Description

            An independent second story.

            $DIVIDER
            """.trimIndent(),
        )

        val agentAdapter = mockk<AgentAdapter>()
        every { agentAdapter.generate(any(), any()) } throws AgentInvocationException("claude not found")

        val engine = DefaultExecutionEngine(
            storyLoader = TasksFileLoader(),
            storyPlanner = DefaultStoryPlanner(DefaultDependencyResolver()),
            progressTracker = FileProgressTracker(),
            contextBuilder = DefaultContextBuilder(),
            agentAdapter = agentAdapter,
        )

        val summary = engine.run(repo)

        summary.results.shouldHaveSize(1)
        summary.results.first().storyId shouldBe "ADO-001"
        summary.results.first().status shouldBe StoryStatus.BLOCKED
        summary.hasBlockedStory shouldBe true

        val persisted = FileProgressTracker().loadProgress(repo)
        persisted.statusOf("ADO-001") shouldBe StoryStatus.BLOCKED
        persisted.statusOf("ADO-002").shouldBeNull()
    }
})
