package core.git

import core.common.exception.GitOperationException
import core.common.exception.ProcessExecutionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import models.ProcessResult
import utils.ProcessExecutor
import java.nio.file.Files
import java.nio.file.Path

/** Runs a real git command for test fixture setup only; production code goes through [DefaultGitManager]. */
private fun runGitForFixture(repo: Path, vararg args: String) {
    val process = ProcessBuilder(listOf("git") + args).directory(repo.toFile()).start()
    process.waitFor()
}

private fun initRepo(): Path {
    val repo = Files.createTempDirectory("ado-git-manager-test")
    runGitForFixture(repo, "init", "-q")
    runGitForFixture(repo, "config", "user.email", "test@example.com")
    runGitForFixture(repo, "config", "user.name", "Test User")
    Files.writeString(repo.resolve("README.md"), "initial\n")
    runGitForFixture(repo, "add", "-A")
    runGitForFixture(repo, "commit", "-q", "-m", "initial commit")
    return repo
}

class DefaultGitManagerTest : FunSpec({

    val gitManager = DefaultGitManager()

    test("inspectStatus reports no changes in a clean repository") {
        val repo = initRepo()

        gitManager.inspectStatus(repo).hasChanges shouldBe false
    }

    test("inspectStatus reports an untracked file") {
        val repo = initRepo()
        Files.writeString(repo.resolve("new-file.txt"), "content")

        val status = gitManager.inspectStatus(repo)

        status.hasChanges shouldBe true
        status.changedFiles shouldBe listOf("new-file.txt")
    }

    test("inspectStatus reports a modified tracked file") {
        val repo = initRepo()
        Files.writeString(repo.resolve("README.md"), "changed\n")

        val status = gitManager.inspectStatus(repo)

        status.hasChanges shouldBe true
        status.changedFiles shouldBe listOf("README.md")
    }

    test("inspectStatus throws GitOperationException for a path that is not a git repository") {
        val notARepo = Files.createTempDirectory("ado-git-manager-test-not-a-repo")

        shouldThrow<GitOperationException> { gitManager.inspectStatus(notARepo) }
    }

    test("createCommit stages and commits changes, returning the new commit SHA") {
        val repo = initRepo()
        Files.writeString(repo.resolve("new-file.txt"), "content")

        val result = gitManager.createCommit(repo, "feat(ADO-001): add new file")

        result.success shouldBe true
        result.commitSha!!.isNotBlank() shouldBe true
        gitManager.inspectStatus(repo).hasChanges shouldBe false
    }

    test("the commit message and content are actually recorded in git history") {
        val repo = initRepo()
        Files.writeString(repo.resolve("new-file.txt"), "content")

        gitManager.createCommit(repo, "feat(ADO-001): add new file")

        val log = ProcessBuilder("git", "log", "-1", "--pretty=%s").directory(repo.toFile()).start()
        val subject = log.inputStream.bufferedReader().readText().trim()
        log.waitFor()

        subject shouldBe "feat(ADO-001): add new file"
    }

    test("createCommit returns a failed result, not an exception, when there is nothing to commit") {
        val repo = initRepo()

        val result = gitManager.createCommit(repo, "feat(ADO-001): nothing changed")

        result.success shouldBe false
        result.failureReason!!.isNotBlank() shouldBe true
    }

    test("throws GitOperationException when the git binary cannot be started") {
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), any()) } throws ProcessExecutionException("git: command not found")

        shouldThrow<GitOperationException> {
            DefaultGitManager(processExecutor).inspectStatus(Path.of("/repo"))
        }
    }

    test("propagates GitOperationException when staging fails during a commit attempt") {
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(match { it.first() == "git" && it.getOrNull(1) == "add" }, any()) } returns
            ProcessResult(exitCode = 128, stdout = "", stderr = "fatal: corrupted repository", durationMillis = 1)

        shouldThrow<GitOperationException> {
            DefaultGitManager(processExecutor).createCommit(Path.of("/repo"), "feat(ADO-001): x")
        }
    }
})
