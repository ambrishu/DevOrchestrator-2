package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import models.BuildResult
import models.BuildStatus

class BuildResultFormatterTest : FunSpec({

    test("shows SUCCESS, duration, and zero warnings") {
        val output = BuildResultFormatter.format(
            BuildResult(status = BuildStatus.SUCCESS, stdout = "built", stderr = "", durationMillis = 123),
        )

        output shouldContain "Status:    SUCCESS"
        output shouldContain "Duration:  123ms"
        output shouldContain "Warnings:  0"
        output shouldContain "built"
    }

    test("shows FAILURE and lists warnings and stderr") {
        val result = BuildResult(
            status = BuildStatus.FAILURE,
            stdout = "",
            stderr = "compile error",
            warnings = listOf("warning: unused import"),
            durationMillis = 5,
        )

        val output = BuildResultFormatter.format(result)

        output shouldContain "Status:    FAILURE"
        output shouldContain "warning: unused import"
        output shouldContain "compile error"
    }
})
