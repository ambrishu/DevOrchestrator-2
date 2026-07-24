package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ProcessResultTest : FunSpec({

    test("isSuccess is true for exit code 0") {
        ProcessResult(exitCode = 0, stdout = "", stderr = "", durationMillis = 5).isSuccess shouldBe true
    }

    test("isSuccess is false for a non-zero exit code") {
        ProcessResult(exitCode = 1, stdout = "", stderr = "boom", durationMillis = 5).isSuccess shouldBe false
    }

    test("carries stdout, stderr, and duration") {
        val result = ProcessResult(exitCode = 0, stdout = "out", stderr = "err", durationMillis = 42)

        result.stdout shouldBe "out"
        result.stderr shouldBe "err"
        result.durationMillis shouldBe 42
    }
})
