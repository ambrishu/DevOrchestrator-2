package core.execution

import core.agent.AgentAdapter
import core.build.DefaultBuildExecutor
import core.configuration.YamlConfigurationLoader
import core.context.DefaultContextBuilder
import core.git.DefaultCommitMessageFormatter
import core.git.DefaultGitManager
import core.planner.DefaultDependencyResolver
import core.planner.DefaultStoryPlanner
import core.planner.TasksFileLoader
import core.progress.FileProgressTracker
import core.repair.DefaultFailureAnalyzer
import core.repair.DefaultRepairContextBuilder
import core.repair.DefaultRepairLoop
import core.review.CodeReviewAgent
import core.validation.DefaultQualityGateEngine
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.ContextPackage
import models.GenerationResult
import models.ReviewResult
import models.StoryExecutionResult
import models.StoryStatus
import utils.DefaultProcessExecutor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

private const val DIVIDER = "⸻"

/** Runs a real git command for fixture setup/verification only; production code goes through GitManager. */
private fun runGitForFixture(repo: Path, vararg args: String) {
    ProcessBuilder(listOf("git") + args).directory(repo.toFile()).start().waitFor()
}

private fun gitLogSubjects(repo: Path): List<String> {
    val process = ProcessBuilder("git", "log", "--pretty=%s").directory(repo.toFile()).start()
    val output = process.inputStream.bufferedReader().readText()
    process.waitFor()
    return output.lineSequence().filter { it.isNotBlank() }.toList()
}

private fun initGitRepo(): Path {
    val repo = Files.createTempDirectory("ado-e2e-test")
    runGitForFixture(repo, "init", "-q")
    runGitForFixture(repo, "config", "user.email", "test@example.com")
    runGitForFixture(repo, "config", "user.name", "Test User")
    return repo
}

private fun writeTasksFile(repo: Path, content: String) {
    val docsDir = Files.createDirectories(repo.resolve("docs"))
    Files.writeString(docsDir.resolve("TASKS.md"), content)
}

private fun writeConfig(repo: Path, buildCommand: Path, extra: String = "") {
    val adoDir = Files.createDirectories(repo.resolve(".ado"))
    Files.writeString(adoDir.resolve("config.yaml"), "build:\n  command: $buildCommand\ntest:\n  command: $buildCommand\n$extra")
}

private fun createExecutableScript(repo: Path, name: String, script: String): Path {
    val binDir = Files.createDirectories(repo.resolve("bin"))
    val file = binDir.resolve(name)
    Files.writeString(file, script)
    Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rwxr-xr-x"))
    return file
}

private fun commitFixtureSetup(repo: Path) {
    runGitForFixture(repo, "add", "-A")
    runGitForFixture(repo, "commit", "-q", "-m", "fixture setup")
}

/** [AgentAdapter] test double that actually writes a file per story, so there is always something to commit. */
private fun writingAgentAdapter(): AgentAdapter {
    val adapter = mockk<AgentAdapter>()
    every { adapter.generate(any(), any()) } answers {
        val context = firstArg<ContextPackage>()
        val repositoryPath = secondArg<Path>()
        Files.writeString(repositoryPath.resolve("IMPLEMENTED_${context.story.id}.txt"), "done")
        GenerationResult(summary = "Implemented ${context.story.id}")
    }
    return adapter
}

/** [CodeReviewAgent] test double for the integration suite; a real one would invoke live `claude`. */
private fun nonBlockingReviewAgent(): CodeReviewAgent {
    val agent = mockk<CodeReviewAgent>()
    every { agent.review(any(), any()) } returns ReviewResult()
    return agent
}

class DefaultExecutionEngineIntegrationTest : FunSpec({

    fun realEngine(agentAdapter: AgentAdapter, codeReviewAgent: CodeReviewAgent = nonBlockingReviewAgent()) = DefaultExecutionEngine(
        storyLoader = TasksFileLoader(),
        storyPlanner = DefaultStoryPlanner(DefaultDependencyResolver()),
        progressTracker = FileProgressTracker(),
        contextBuilder = DefaultContextBuilder(),
        agentAdapter = agentAdapter,
        buildExecutor = DefaultBuildExecutor(),
        repairLoop = DefaultRepairLoop(
            DefaultFailureAnalyzer(),
            DefaultRepairContextBuilder(),
            agentAdapter,
            DefaultBuildExecutor(),
        ),
        repairContextBuilder = DefaultRepairContextBuilder(),
        qualityGateEngine = DefaultQualityGateEngine(YamlConfigurationLoader(), DefaultBuildExecutor(), DefaultProcessExecutor()),
        codeReviewAgent = codeReviewAgent,
        gitManager = DefaultGitManager(),
        commitMessageFormatter = DefaultCommitMessageFormatter(),
        configurationLoader = YamlConfigurationLoader(),
    )

    test("a story that builds and passes quality gates on the first try reaches DONE with a real commit") {
        val repo = initGitRepo()
        writeTasksFile(
            repo,
            """
            ADO-001 — First Story

            Status: todo

            Depends On: None

            Description

            The first story.

            $DIVIDER
            """.trimIndent(),
        )
        val alwaysOk = createExecutableScript(repo, "always-ok", "#!/bin/sh\necho ok\nexit 0\n")
        writeConfig(repo, alwaysOk)
        commitFixtureSetup(repo)

        val summary = realEngine(writingAgentAdapter()).run(repo)

        summary.results.map { it.status } shouldBe listOf(StoryStatus.DONE)
        gitLogSubjects(repo).first() shouldBe "feat(ADO-001): first story"

        val progress = FileProgressTracker().loadProgress(repo)
        progress.statusOf("ADO-001") shouldBe StoryStatus.DONE
    }

    test("a build that fails is repaired by the agent and the story still reaches DONE") {
        val repo = initGitRepo()
        writeTasksFile(
            repo,
            """
            ADO-001 — First Story

            Status: todo

            Depends On: None

            Description

            The first story.

            $DIVIDER
            """.trimIndent(),
        )
        val readyGatedBuild = createExecutableScript(
            repo,
            "ready-gated-build",
            "#!/bin/sh\nif [ -f READY ]; then echo ok; exit 0; else echo 'not ready' 1>&2; exit 1; fi\n",
        )
        writeConfig(repo, readyGatedBuild)
        commitFixtureSetup(repo)

        val agentAdapter = mockk<AgentAdapter>()
        every { agentAdapter.generate(any(), any()) } answers {
            val context = firstArg<ContextPackage>()
            val repositoryPath = secondArg<Path>()
            if (context.failureAnalysis != null) {
                Files.writeString(repositoryPath.resolve("READY"), "fixed")
            } else {
                Files.writeString(repositoryPath.resolve("IMPLEMENTED_${context.story.id}.txt"), "attempt 1")
            }
            GenerationResult(summary = "attempted ${context.story.id}")
        }

        val summary = realEngine(agentAdapter).run(repo)

        summary.results.shouldHaveSize(1)
        summary.results.first().storyId shouldBe "ADO-001"
        summary.results.first().status shouldBe StoryStatus.DONE
        gitLogSubjects(repo).first() shouldBe "feat(ADO-001): first story"
    }

    test("a failing quality gate blocks the story and no commit is made") {
        val repo = initGitRepo()
        writeTasksFile(
            repo,
            """
            ADO-001 — First Story

            Status: todo

            Depends On: None

            Description

            The first story.

            $DIVIDER
            """.trimIndent(),
        )
        val alwaysOk = createExecutableScript(repo, "always-ok", "#!/bin/sh\necho ok\nexit 0\n")
        val alwaysFail = createExecutableScript(repo, "always-fail", "#!/bin/sh\necho 'style violation' 1>&2\nexit 1\n")
        writeConfig(repo, alwaysOk, extra = "staticAnalysis:\n  command: $alwaysFail\n")
        commitFixtureSetup(repo)
        val commitsBefore = gitLogSubjects(repo).size

        val summary = realEngine(writingAgentAdapter()).run(repo)

        summary.results shouldBe listOf(
            StoryExecutionResult("ADO-001", StoryStatus.BLOCKED, "Quality gates failed: staticAnalysis"),
        )
        gitLogSubjects(repo).size shouldBe commitsBefore
    }

    test("a dependent story becomes executable and reaches DONE only after its dependency is committed, in one run") {
        val repo = initGitRepo()
        writeTasksFile(
            repo,
            """
            ADO-001 — First Story

            Status: todo

            Depends On: None

            Description

            The first story.

            $DIVIDER

            ADO-002 — Second Story

            Status: todo

            Depends On: ADO-001

            Description

            Depends on the first story.

            $DIVIDER
            """.trimIndent(),
        )
        val alwaysOk = createExecutableScript(repo, "always-ok", "#!/bin/sh\necho ok\nexit 0\n")
        writeConfig(repo, alwaysOk)
        commitFixtureSetup(repo)

        val summary = realEngine(writingAgentAdapter()).run(repo)

        summary.results.map { it.storyId } shouldBe listOf("ADO-001", "ADO-002")
        summary.results.map { it.status } shouldBe listOf(StoryStatus.DONE, StoryStatus.DONE)

        val progress = FileProgressTracker().loadProgress(repo)
        progress.statusOf("ADO-001") shouldBe StoryStatus.DONE
        progress.statusOf("ADO-002") shouldBe StoryStatus.DONE

        val subjects = gitLogSubjects(repo)
        subjects.shouldHaveSize(3)
        subjects[0] shouldBe "feat(ADO-002): second story"
        subjects[1] shouldBe "feat(ADO-001): first story"
    }

    test("a blocking code review issue blocks the story and no commit is made") {
        val repo = initGitRepo()
        writeTasksFile(
            repo,
            """
            ADO-001 — First Story

            Status: todo

            Depends On: None

            Description

            The first story.

            $DIVIDER
            """.trimIndent(),
        )
        val alwaysOk = createExecutableScript(repo, "always-ok", "#!/bin/sh\necho ok\nexit 0\n")
        writeConfig(repo, alwaysOk, extra = "repair:\n  retries: 0\n")
        commitFixtureSetup(repo)
        val commitsBefore = gitLogSubjects(repo).size

        val blockingReviewAgent = mockk<CodeReviewAgent>()
        every { blockingReviewAgent.review(any(), any()) } returns
            ReviewResult(blockingIssues = listOf("Thread-unsafe access to shared state"))

        val summary = realEngine(writingAgentAdapter(), blockingReviewAgent).run(repo)

        summary.results shouldBe listOf(
            StoryExecutionResult(
                "ADO-001",
                StoryStatus.BLOCKED,
                "Code review blocked after 0 repair attempt(s): Thread-unsafe access to shared state",
            ),
        )
        gitLogSubjects(repo).size shouldBe commitsBefore
    }

    test("a blocking code review issue is repaired by the agent and the story reaches DONE") {
        val repo = initGitRepo()
        writeTasksFile(
            repo,
            """
            ADO-001 — First Story

            Status: todo

            Depends On: None

            Description

            The first story.

            $DIVIDER
            """.trimIndent(),
        )
        val alwaysOk = createExecutableScript(repo, "always-ok", "#!/bin/sh\necho ok\nexit 0\n")
        writeConfig(repo, alwaysOk)
        commitFixtureSetup(repo)

        val reviewAgent = mockk<CodeReviewAgent>()
        every { reviewAgent.review(any(), any()) } returnsMany listOf(
            ReviewResult(blockingIssues = listOf("Missing .gitignore")),
            ReviewResult(),
        )

        val summary = realEngine(writingAgentAdapter(), reviewAgent).run(repo)

        summary.results.shouldHaveSize(1)
        summary.results.first().status shouldBe StoryStatus.DONE
        verify(exactly = 2) { reviewAgent.review(any(), any()) }
        gitLogSubjects(repo).first() shouldBe "feat(ADO-001): first story"
    }

    test("skips code review when review.enabled is false in real configuration") {
        val repo = initGitRepo()
        writeTasksFile(
            repo,
            """
            ADO-001 — First Story

            Status: todo

            Depends On: None

            Description

            The first story.

            $DIVIDER
            """.trimIndent(),
        )
        val alwaysOk = createExecutableScript(repo, "always-ok", "#!/bin/sh\necho ok\nexit 0\n")
        writeConfig(repo, alwaysOk, extra = "review:\n  enabled: false\n")
        commitFixtureSetup(repo)

        val reviewAgent = mockk<CodeReviewAgent>()

        val summary = realEngine(writingAgentAdapter(), reviewAgent).run(repo)

        summary.results.map { it.status } shouldBe listOf(StoryStatus.DONE)
        verify(exactly = 0) { reviewAgent.review(any(), any()) }
    }
})
