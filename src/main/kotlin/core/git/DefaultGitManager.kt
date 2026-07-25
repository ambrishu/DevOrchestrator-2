package core.git

import core.common.exception.GitOperationException
import core.common.exception.ProcessExecutionException
import models.CommitResult
import models.ProcessResult
import models.RepositoryStatus
import utils.DefaultProcessExecutor
import utils.ProcessExecutor
import java.nio.file.Path

/** [GitManager] running the real `git` CLI via the process execution abstraction. */
class DefaultGitManager(
    private val processExecutor: ProcessExecutor = DefaultProcessExecutor(),
) : GitManager {

    override fun inspectStatus(repositoryPath: Path): RepositoryStatus {
        val result = runGit(repositoryPath, "status", "--porcelain")
        val changedFiles = result.stdout.lineSequence()
            .filter { it.isNotBlank() }
            .map { it.substring(minOf(3, it.length)) }
            .toList()

        return RepositoryStatus(hasChanges = changedFiles.isNotEmpty(), changedFiles = changedFiles)
    }

    override fun createCommit(repositoryPath: Path, message: String): CommitResult {
        runGit(repositoryPath, "add", "-A")

        val commitResult = runGitAllowingFailure(repositoryPath, "commit", "-m", message)
        if (!commitResult.isSuccess) {
            return CommitResult(success = false, failureReason = commitResult.stderr.ifBlank { commitResult.stdout })
        }

        val sha = runGit(repositoryPath, "rev-parse", "HEAD").stdout.trim()
        return CommitResult(success = true, commitSha = sha)
    }

    /** Runs a git command expected to always succeed in a valid repository; throws otherwise. */
    private fun runGit(repositoryPath: Path, vararg args: String): ProcessResult {
        val result = runGitAllowingFailure(repositoryPath, *args)
        if (!result.isSuccess) {
            throw GitOperationException(
                "git ${args.joinToString(" ")} failed: ${result.stderr.ifBlank { result.stdout }}",
            )
        }
        return result
    }

    /** Runs a git command that may legitimately exit non-zero (e.g. commit with nothing staged). */
    private fun runGitAllowingFailure(repositoryPath: Path, vararg args: String): ProcessResult = try {
        processExecutor.execute(listOf("git") + args, repositoryPath)
    } catch (e: ProcessExecutionException) {
        throw GitOperationException("Failed to run git ${args.joinToString(" ")}", e)
    }
}
