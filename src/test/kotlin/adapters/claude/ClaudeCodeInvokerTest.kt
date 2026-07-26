package adapters.claude

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.ProcessResult
import utils.ProcessExecutor
import java.nio.file.Path

class ClaudeCodeInvokerTest : FunSpec({

    test("invokes claude in print mode with acceptEdits by default") {
        val processExecutor = mockk<ProcessExecutor>()
        val repositoryPath = Path.of("/repo")
        val expected = ProcessResult(exitCode = 0, stdout = "done", stderr = "", durationMillis = 10)
        every {
            processExecutor.execute(
                listOf("claude", "--print", "--permission-mode", "acceptEdits", "do the thing"),
                repositoryPath,
            )
        } returns expected

        val result = ClaudeCodeInvoker(processExecutor).invoke("do the thing", repositoryPath)

        result shouldBe expected
        verify {
            processExecutor.execute(
                listOf("claude", "--print", "--permission-mode", "acceptEdits", "do the thing"),
                repositoryPath,
            )
        }
    }

    test("invokes claude with the given permission mode") {
        val processExecutor = mockk<ProcessExecutor>()
        val repositoryPath = Path.of("/repo")
        every { processExecutor.execute(any(), repositoryPath) } returns
            ProcessResult(exitCode = 0, stdout = "", stderr = "", durationMillis = 5)

        ClaudeCodeInvoker(processExecutor).invoke("review this", repositoryPath, permissionMode = "plan")

        verify {
            processExecutor.execute(
                listOf("claude", "--print", "--permission-mode", "plan", "review this"),
                repositoryPath,
            )
        }
    }
})
