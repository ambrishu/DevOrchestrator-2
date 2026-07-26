package adapters.claude

import core.common.exception.GitOperationException
import core.common.exception.ProcessExecutionException
import core.common.exception.ReviewFailureException
import core.git.GitManager
import core.review.CodeReviewAgent
import core.review.DefaultReviewPromptBuilder
import core.review.ReviewPromptBuilder
import models.ContextPackage
import models.ReviewResult
import models.SourceFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * [CodeReviewAgent] implemented by invoking the `claude` CLI in a fresh, independent session.
 * Requires `claude` on `PATH`.
 *
 * Discovers what to review via [GitManager.inspectStatus] rather than [models.GenerationResult],
 * since that model intentionally leaves `modifiedFiles` empty (see [ClaudeResultParser]) —
 * a working-tree diff is the reliable source of "what changed."
 */
class ClaudeCodeReviewAgent(
    private val gitManager: GitManager,
    private val reviewPromptBuilder: ReviewPromptBuilder = DefaultReviewPromptBuilder(),
    private val invoker: ClaudeCodeInvoker = ClaudeCodeInvoker(),
    private val resultParser: ReviewResultParser = ReviewResultParser(),
) : CodeReviewAgent {

    override fun review(context: ContextPackage, repositoryPath: Path): ReviewResult {
        val changedFiles = try {
            readChangedFiles(repositoryPath)
        } catch (e: GitOperationException) {
            throw ReviewFailureException("Failed to inspect repository status for review", e)
        }

        val prompt = reviewPromptBuilder.buildPrompt(context, changedFiles)

        val result = try {
            invoker.invoke(prompt, repositoryPath, permissionMode = "plan")
        } catch (e: ProcessExecutionException) {
            throw ReviewFailureException("Failed to invoke Claude Code for review: ${e.message}", e)
        }

        if (!result.isSuccess) {
            throw ReviewFailureException(
                "Claude Code review exited with status ${result.exitCode}" +
                    if (result.stderr.isNotBlank()) ": ${result.stderr}" else "",
            )
        }

        return resultParser.parse(result)
    }

    private fun readChangedFiles(repositoryPath: Path): List<SourceFile> {
        val status = gitManager.inspectStatus(repositoryPath)
        return status.changedFiles.mapNotNull { relativePath ->
            val file = repositoryPath.resolve(relativePath)
            if (!Files.isRegularFile(file)) return@mapNotNull null

            // Binary changed files (jars, images, compiled output) have nothing useful to show
            // the reviewer as text; skip them rather than letting one crash the whole review.
            try {
                SourceFile(relativePath, Files.readString(file))
            } catch (e: IOException) {
                null
            }
        }
    }
}
