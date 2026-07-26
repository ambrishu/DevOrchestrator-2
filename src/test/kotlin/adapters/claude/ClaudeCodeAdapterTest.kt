package adapters.claude

import core.agent.PromptBuilder
import core.common.exception.AgentInvocationException
import core.common.exception.ProcessExecutionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.ContextPackage
import models.ProcessResult
import models.Story
import models.StoryStatus
import utils.ProcessExecutor
import java.nio.file.Path

class ClaudeCodeAdapterTest : FunSpec({

    val story = Story(id = "ADO-001", title = "Example", description = "d", status = StoryStatus.TODO)
    val context = ContextPackage(story, emptyList())
    val repositoryPath = Path.of("/repo")

    fun adapterWith(processExecutor: ProcessExecutor): ClaudeCodeAdapter {
        val promptBuilder = mockk<PromptBuilder>()
        every { promptBuilder.buildPrompt(context) } returns "the prompt"
        return ClaudeCodeAdapter(
            promptBuilder = promptBuilder,
            invoker = ClaudeCodeInvoker(processExecutor),
            resultParser = ClaudeResultParser(),
        )
    }

    test("returns a GenerationResult when claude exits successfully") {
        val processExecutor = mockk<ProcessExecutor>()
        every {
            processExecutor.execute(listOf("claude", "--print", "--permission-mode", "acceptEdits", "the prompt"), repositoryPath)
        } returns ProcessResult(exitCode = 0, stdout = "Implemented it.", stderr = "", durationMillis = 5)

        val result = adapterWith(processExecutor).generate(context, repositoryPath)

        result.summary shouldBe "Implemented it."
    }

    test("invokes claude with acceptEdits permission mode, since generation must write files") {
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), any()) } returns
            ProcessResult(exitCode = 0, stdout = "done", stderr = "", durationMillis = 5)

        adapterWith(processExecutor).generate(context, repositoryPath)

        verify {
            processExecutor.execute(listOf("claude", "--print", "--permission-mode", "acceptEdits", "the prompt"), repositoryPath)
        }
    }

    test("throws AgentInvocationException when claude exits with a non-zero status") {
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), any()) } returns
            ProcessResult(exitCode = 1, stdout = "", stderr = "something broke", durationMillis = 5)

        val exception = shouldThrow<AgentInvocationException> {
            adapterWith(processExecutor).generate(context, repositoryPath)
        }
        exception.message.shouldNotBeNull().shouldContain("something broke")
    }

    test("throws AgentInvocationException when the claude process cannot be started") {
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), any()) } throws ProcessExecutionException("claude: command not found")

        shouldThrow<AgentInvocationException> {
            adapterWith(processExecutor).generate(context, repositoryPath)
        }
    }
})
