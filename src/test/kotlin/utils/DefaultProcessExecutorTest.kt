package utils

import core.common.exception.ProcessExecutionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import java.nio.file.Files

class DefaultProcessExecutorTest : FunSpec({

    val executor = DefaultProcessExecutor()

    test("captures stdout and a zero exit code for a successful command") {
        val workingDirectory = Files.createTempDirectory("ado-process-executor-test")

        val result = executor.execute(listOf("sh", "-c", "echo hello"), workingDirectory)

        result.exitCode shouldBe 0
        result.isSuccess shouldBe true
        result.stdout shouldBe "hello"
    }

    test("captures stderr and a non-zero exit code") {
        val workingDirectory = Files.createTempDirectory("ado-process-executor-test")

        val result = executor.execute(listOf("sh", "-c", "echo oops 1>&2; exit 3"), workingDirectory)

        result.exitCode shouldBe 3
        result.isSuccess shouldBe false
        result.stderr shouldBe "oops"
    }

    test("records a non-negative duration") {
        val workingDirectory = Files.createTempDirectory("ado-process-executor-test")

        val result = executor.execute(listOf("sh", "-c", "exit 0"), workingDirectory)

        result.durationMillis shouldBeGreaterThanOrEqualTo 0L
    }

    test("throws ProcessExecutionException when the binary does not exist") {
        val workingDirectory = Files.createTempDirectory("ado-process-executor-test")

        shouldThrow<ProcessExecutionException> {
            executor.execute(listOf("ado-nonexistent-binary-xyz"), workingDirectory)
        }
    }

    test("runs the command in the given working directory") {
        val workingDirectory = Files.createTempDirectory("ado-process-executor-test")
        Files.writeString(workingDirectory.resolve("marker.txt"), "present")

        val result = executor.execute(listOf("sh", "-c", "cat marker.txt"), workingDirectory)

        result.stdout shouldBe "present"
    }
})
