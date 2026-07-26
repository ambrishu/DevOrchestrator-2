package adapters.claude

import core.agent.AgentAdapter
import core.agent.DefaultPromptBuilder
import core.agent.PromptBuilder
import core.common.exception.AgentInvocationException
import core.common.exception.ProcessExecutionException
import models.ContextPackage
import models.GenerationResult
import java.nio.file.Path

/** [AgentAdapter] implemented by invoking the `claude` CLI. Requires `claude` on `PATH`. */
class ClaudeCodeAdapter(
    private val promptBuilder: PromptBuilder = DefaultPromptBuilder(),
    private val invoker: ClaudeCodeInvoker = ClaudeCodeInvoker(),
    private val resultParser: ClaudeResultParser = ClaudeResultParser(),
) : AgentAdapter {

    override fun generate(context: ContextPackage, repositoryPath: Path): GenerationResult {
        val prompt = promptBuilder.buildPrompt(context)

        val result = try {
            invoker.invoke(prompt, repositoryPath, permissionMode = "acceptEdits")
        } catch (e: ProcessExecutionException) {
            throw AgentInvocationException("Failed to invoke Claude Code: ${e.message}", e)
        }

        if (!result.isSuccess) {
            throw AgentInvocationException(
                "Claude Code exited with status ${result.exitCode}" +
                    if (result.stderr.isNotBlank()) ": ${result.stderr}" else "",
            )
        }

        return resultParser.parse(result)
    }
}
