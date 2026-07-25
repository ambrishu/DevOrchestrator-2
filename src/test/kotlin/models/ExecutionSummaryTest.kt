package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ExecutionSummaryTest : FunSpec({

    test("an empty summary has no blocked story") {
        ExecutionSummary().hasBlockedStory shouldBe false
    }

    test("hasBlockedStory is false when every result succeeded") {
        val summary = ExecutionSummary(
            listOf(StoryExecutionResult("ADO-001", StoryStatus.REVIEW, "ok")),
        )

        summary.hasBlockedStory shouldBe false
    }

    test("hasBlockedStory is true when any result is blocked") {
        val summary = ExecutionSummary(
            listOf(
                StoryExecutionResult("ADO-001", StoryStatus.REVIEW, "ok"),
                StoryExecutionResult("ADO-002", StoryStatus.BLOCKED, "failed"),
            ),
        )

        summary.hasBlockedStory shouldBe true
    }
})
