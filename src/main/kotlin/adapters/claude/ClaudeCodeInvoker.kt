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
     * @param permissionMode Passed as `claude --permission-mode <mode>`. Non-interactive `--print`
     * sessions cannot answer a permission prompt, so this must be set explicitly to get useful
     * behavior: `"acceptEdits"` for a session that should write files (code generation, repair),
     * or `"plan"` for a read-only session that must never modify the repository (code review).
     * @throws core.common.exception.ProcessExecutionException if the `claude` binary cannot be started.
     */
    fun invoke(prompt: String, repositoryPath: Path, permissionMode: String = "acceptEdits"): ProcessResult =
        processExecutor.execute(
            listOf("claude", "--print", "--permission-mode", permissionMode, prompt),
            repositoryPath,
        )
}
