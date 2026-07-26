package adapters.claude

import core.common.exception.GitOperationException
import core.common.exception.ProcessExecutionException
import core.common.exception.ReviewFailureException
import core.git.GitManager
import core.review.ReviewPromptBuilder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.ContextPackage
import models.ProcessResult
import models.RepositoryStatus
import models.SourceFile
import models.Story
import models.StoryStatus
import utils.ProcessExecutor
import java.nio.file.Files
import java.nio.file.Path

class ClaudeCodeReviewAgentTest : FunSpec({

    val story = Story(id = "ADO-001", title = "Example", description = "d", status = StoryStatus.TODO)
    val context = ContextPackage(story, emptyList())

    fun agentWith(
        gitManager: GitManager,
        processExecutor: ProcessExecutor,
        reviewPromptBuilder: ReviewPromptBuilder = mockk(relaxed = true),
    ) = ClaudeCodeReviewAgent(
        gitManager = gitManager,
        reviewPromptBuilder = reviewPromptBuilder,
        invoker = ClaudeCodeInvoker(processExecutor),
    )

    test("reads changed files' content and invokes claude with the review prompt") {
        val repo = Files.createTempDirectory("ado-review-agent-test")
        Files.writeString(repo.resolve("Foo.kt"), "class Foo")

        val gitManager = mockk<GitManager>()
        every { gitManager.inspectStatus(repo) } returns RepositoryStatus(hasChanges = true, changedFiles = listOf("Foo.kt"))

        val reviewPromptBuilder = mockk<ReviewPromptBuilder>()
        every { reviewPromptBuilder.buildPrompt(context, listOf(SourceFile("Foo.kt", "class Foo"))) } returns "the review prompt"

        val processExecutor = mockk<ProcessExecutor>()
        every {
            processExecutor.execute(listOf("claude", "--print", "--permission-mode", "plan", "the review prompt"), repo)
        } returns ProcessResult(exitCode = 0, stdout = "BLOCKING: bad thing", stderr = "", durationMillis = 5)

        val result = agentWith(gitManager, processExecutor, reviewPromptBuilder).review(context, repo)

        result.blockingIssues shouldBe listOf("bad thing")
    }

    test("invokes claude with plan permission mode, since review must never modify the repository") {
        val repo = Files.createTempDirectory("ado-review-agent-test")
        val gitManager = mockk<GitManager>()
        every { gitManager.inspectStatus(repo) } returns RepositoryStatus(hasChanges = false)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repo) } returns
            ProcessResult(exitCode = 0, stdout = "", stderr = "", durationMillis = 5)

        agentWith(gitManager, processExecutor).review(context, repo)

        verify {
            processExecutor.execute(match { it.contains("--permission-mode") && it.contains("plan") }, repo)
        }
    }

    test("skips a changed file that is not valid UTF-8 text, instead of crashing") {
        val repo = Files.createTempDirectory("ado-review-agent-test")
        // A lone continuation byte is not valid UTF-8 on its own; mimics binary content
        // like a jar file appearing in `git status` after e.g. `gradle wrapper` runs.
        Files.write(repo.resolve("gradle-wrapper.jar"), byteArrayOf(0x80.toByte(), 0x00))
        Files.writeString(repo.resolve("Foo.kt"), "class Foo")

        val gitManager = mockk<GitManager>()
        every { gitManager.inspectStatus(repo) } returns
            RepositoryStatus(hasChanges = true, changedFiles = listOf("gradle-wrapper.jar", "Foo.kt"))

        val reviewPromptBuilder = mockk<ReviewPromptBuilder>()
        every { reviewPromptBuilder.buildPrompt(context, listOf(SourceFile("Foo.kt", "class Foo"))) } returns "prompt"

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repo) } returns
            ProcessResult(exitCode = 0, stdout = "", stderr = "", durationMillis = 5)

        agentWith(gitManager, processExecutor, reviewPromptBuilder).review(context, repo)

        verify { reviewPromptBuilder.buildPrompt(context, listOf(SourceFile("Foo.kt", "class Foo"))) }
    }

    test("skips changed paths that no longer exist as regular files") {
        val repo = Files.createTempDirectory("ado-review-agent-test")

        val gitManager = mockk<GitManager>()
        every { gitManager.inspectStatus(repo) } returns
            RepositoryStatus(hasChanges = true, changedFiles = listOf("deleted-file.kt"))

        val reviewPromptBuilder = mockk<ReviewPromptBuilder>()
        every { reviewPromptBuilder.buildPrompt(context, emptyList()) } returns "prompt"

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repo) } returns
            ProcessResult(exitCode = 0, stdout = "", stderr = "", durationMillis = 5)

        agentWith(gitManager, processExecutor, reviewPromptBuilder).review(context, repo)

        verify { reviewPromptBuilder.buildPrompt(context, emptyList()) }
    }

    test("throws ReviewFailureException when repository status cannot be inspected") {
        val repo = Files.createTempDirectory("ado-review-agent-test")
        val gitManager = mockk<GitManager>()
        every { gitManager.inspectStatus(repo) } throws GitOperationException("not a git repository")

        shouldThrow<ReviewFailureException> {
            agentWith(gitManager, mockk()).review(context, repo)
        }
    }

    test("throws ReviewFailureException when claude cannot be started") {
        val repo = Files.createTempDirectory("ado-review-agent-test")
        val gitManager = mockk<GitManager>()
        every { gitManager.inspectStatus(repo) } returns RepositoryStatus(hasChanges = false)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repo) } throws ProcessExecutionException("claude: command not found")

        shouldThrow<ReviewFailureException> {
            agentWith(gitManager, processExecutor).review(context, repo)
        }
    }

    test("throws ReviewFailureException when claude exits with a non-zero status") {
        val repo = Files.createTempDirectory("ado-review-agent-test")
        val gitManager = mockk<GitManager>()
        every { gitManager.inspectStatus(repo) } returns RepositoryStatus(hasChanges = false)

        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repo) } returns
            ProcessResult(exitCode = 1, stdout = "", stderr = "session error", durationMillis = 5)

        shouldThrow<ReviewFailureException> {
            agentWith(gitManager, processExecutor).review(context, repo)
        }
    }
})
