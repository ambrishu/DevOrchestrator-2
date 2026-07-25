package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import models.ExecutionSummary
import models.StoryExecutionResult
import models.StoryStatus

class ExecutionSummaryFormatterTest : FunSpec({

    test("an empty summary formats as a clear no-op message") {
        ExecutionSummaryFormatter.format(ExecutionSummary()) shouldBe "No executable story found. Nothing to do."
    }

    test("lists each story's id, resulting status, and message") {
        val summary = ExecutionSummary(
            listOf(
                StoryExecutionResult("ADO-001", StoryStatus.REVIEW, "Implemented it."),
                StoryExecutionResult("ADO-002", StoryStatus.BLOCKED, "claude not found"),
            ),
        )

        val output = ExecutionSummaryFormatter.format(summary)

        output shouldContain "ADO-001: review — Implemented it."
        output shouldContain "ADO-002: blocked — claude not found"
    }
})
