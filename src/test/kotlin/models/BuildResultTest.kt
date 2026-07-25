package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BuildResultTest : FunSpec({

    test("isSuccess is true for a SUCCESS status") {
        BuildResult(status = BuildStatus.SUCCESS, stdout = "", stderr = "", durationMillis = 10).isSuccess shouldBe true
    }

    test("isSuccess is false for a FAILURE status") {
        BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = "boom", durationMillis = 10).isSuccess shouldBe false
    }

    test("warnings default to empty") {
        BuildResult(status = BuildStatus.SUCCESS, stdout = "", stderr = "", durationMillis = 10).warnings shouldBe emptyList()
    }

    test("carries stdout, stderr, warnings, and duration") {
        val result = BuildResult(
            status = BuildStatus.FAILURE,
            stdout = "out",
            stderr = "err",
            warnings = listOf("warning: unused import"),
            durationMillis = 42,
        )

        result.stdout shouldBe "out"
        result.stderr shouldBe "err"
        result.warnings shouldBe listOf("warning: unused import")
        result.durationMillis shouldBe 42
    }
})
