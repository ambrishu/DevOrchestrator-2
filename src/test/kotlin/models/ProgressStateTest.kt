package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ProgressStateTest : FunSpec({

    test("a new ProgressState has no recorded stories") {
        ProgressState().stories shouldBe emptyMap()
    }

    test("statusOf returns null for an unrecorded story") {
        ProgressState().statusOf("ADO-001").shouldBeNull()
    }

    test("withStatus records a new story") {
        val state = ProgressState().withStatus("ADO-001", StoryStatus.TODO)

        state.statusOf("ADO-001") shouldBe StoryStatus.TODO
    }

    test("withStatus overwrites an existing story's status") {
        val state = ProgressState()
            .withStatus("ADO-001", StoryStatus.TODO)
            .withStatus("ADO-001", StoryStatus.DONE)

        state.statusOf("ADO-001") shouldBe StoryStatus.DONE
    }

    test("withStatus preserves other recorded stories") {
        val state = ProgressState()
            .withStatus("ADO-001", StoryStatus.DONE)
            .withStatus("ADO-002", StoryStatus.IN_PROGRESS)

        state.statusOf("ADO-001") shouldBe StoryStatus.DONE
        state.statusOf("ADO-002") shouldBe StoryStatus.IN_PROGRESS
    }

    test("withStatus does not mutate the original state") {
        val original = ProgressState()
        original.withStatus("ADO-001", StoryStatus.DONE)

        original.stories shouldBe emptyMap()
    }
})
