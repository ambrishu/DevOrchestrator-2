package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RepairResultTest : FunSpec({

    test("succeeded is true when the final build result is SUCCESS") {
        val result = RepairResult(
            finalBuildResult = BuildResult(status = BuildStatus.SUCCESS, stdout = "", stderr = "", durationMillis = 5),
            attempts = 2,
        )

        result.succeeded shouldBe true
    }

    test("succeeded is false when the final build result is FAILURE") {
        val result = RepairResult(
            finalBuildResult = BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = "", durationMillis = 5),
            attempts = 5,
        )

        result.succeeded shouldBe false
    }

    test("carries the number of attempts made") {
        val result = RepairResult(
            finalBuildResult = BuildResult(status = BuildStatus.SUCCESS, stdout = "", stderr = "", durationMillis = 5),
            attempts = 3,
        )

        result.attempts shouldBe 3
    }
})
