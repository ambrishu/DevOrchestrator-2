package core.validation

import core.common.exception.ProcessExecutionException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import models.ProcessResult
import utils.ProcessExecutor
import java.nio.file.Path

class CommandQualityGateTest : FunSpec({

    val repositoryPath = Path.of("/repo")

    test("passes vacuously when the command is blank, without invoking the process executor") {
        val processExecutor = mockk<ProcessExecutor>()

        val result = CommandQualityGate("formatting", "", processExecutor).execute(repositoryPath)

        result.passed shouldBe true
        result.details shouldBe "Gate not configured; skipped."
    }

    test("passes when the configured command exits 0") {
        val processExecutor = mockk<ProcessExecutor> {
            every { execute(listOf("ktlint"), repositoryPath) } returns
                ProcessResult(exitCode = 0, stdout = "clean", stderr = "", durationMillis = 5)
        }

        val result = CommandQualityGate("formatting", "ktlint", processExecutor).execute(repositoryPath)

        result.passed shouldBe true
        result.details shouldBe ""
    }

    test("fails with stderr details when the configured command exits non-zero") {
        val processExecutor = mockk<ProcessExecutor> {
            every { execute(listOf("ktlint"), repositoryPath) } returns
                ProcessResult(exitCode = 1, stdout = "", stderr = "1 violation found", durationMillis = 5)
        }

        val result = CommandQualityGate("formatting", "ktlint", processExecutor).execute(repositoryPath)

        result.passed shouldBe false
        result.details shouldBe "1 violation found"
    }

    test("fails with an explanatory message when the command cannot be started") {
        val processExecutor = mockk<ProcessExecutor> {
            every { execute(any(), repositoryPath) } throws ProcessExecutionException("no such file")
        }

        val result = CommandQualityGate("formatting", "ktlint", processExecutor).execute(repositoryPath)

        result.passed shouldBe false
        result.details shouldBe "no such file"
    }

    test("uses the given name") {
        CommandQualityGate("staticAnalysis", "", mockk()).name shouldBe "staticAnalysis"
    }
})
