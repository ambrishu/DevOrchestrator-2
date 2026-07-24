package adapters.claude

import models.ProcessResult
import utils.DefaultProcessExecutor
import utils.ProcessExecutor
import java.nio.file.Path

/** Invokes the `claude` CLI in print (non-interactive) mode via the process execution abstraction. */
class ClaudeCodeInvoker(
    private val processExecutor: ProcessExecutor = DefaultProcessExecutor(),
) {

    /**
     * @throws core.common.exception.ProcessExecutionException if the `claude` binary cannot be started.
     */
    fun invoke(prompt: String, repositoryPath: Path): ProcessResult =
        processExecutor.execute(listOf("claude", "--print", prompt), repositoryPath)
}
