package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StoryExecutionResultTest : FunSpec({

    test("carries the story id, resulting status, and a message") {
        val result = StoryExecutionResult(storyId = "ADO-001", status = StoryStatus.REVIEW, message = "Implemented it.")

        result.storyId shouldBe "ADO-001"
        result.status shouldBe StoryStatus.REVIEW
        result.message shouldBe "Implemented it."
    }
})
