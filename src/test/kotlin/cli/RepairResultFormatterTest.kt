package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import models.BuildResult
import models.BuildStatus
import models.RepairResult

class RepairResultFormatterTest : FunSpec({

    test("shows SUCCESS and the attempt count when repair succeeded") {
        val result = RepairResult(
            finalBuildResult = BuildResult(status = BuildStatus.SUCCESS, stdout = "", stderr = "", durationMillis = 5),
            attempts = 2,
        )

        val output = RepairResultFormatter.format(result)

        output shouldContain "Attempts: 2"
        output shouldContain "Outcome:  SUCCESS"
    }

    test("shows RETRIES EXHAUSTED when repair failed") {
        val result = RepairResult(
            finalBuildResult = BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = "", durationMillis = 5),
            attempts = 5,
        )

        val output = RepairResultFormatter.format(result)

        output shouldContain "Attempts: 5"
        output shouldContain "Outcome:  RETRIES EXHAUSTED"
    }
})
