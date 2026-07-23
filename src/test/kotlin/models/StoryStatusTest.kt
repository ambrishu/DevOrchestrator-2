package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class StoryStatusTest : FunSpec({

    test("all nine documented statuses exist") {
        StoryStatus.entries.map { it.name } shouldBe listOf(
            "TODO", "READY", "IN_PROGRESS", "REVIEW", "RETRYING", "BLOCKED", "FAILED", "PASSED", "DONE",
        )
    }

    test("fromToken parses snake_case tokens case-insensitively") {
        StoryStatus.fromToken("todo") shouldBe StoryStatus.TODO
        StoryStatus.fromToken("in_progress") shouldBe StoryStatus.IN_PROGRESS
        StoryStatus.fromToken("DONE") shouldBe StoryStatus.DONE
        StoryStatus.fromToken(" done ") shouldBe StoryStatus.DONE
    }

    test("fromToken returns null for an unrecognized token") {
        StoryStatus.fromToken("archived").shouldBeNull()
    }
})
