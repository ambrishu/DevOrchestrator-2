package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class StorySelectionTest : FunSpec({

    val story = Story(
        id = "ADO-001",
        title = "Example",
        description = "",
        status = StoryStatus.TODO,
    )

    test("Selected carries the chosen story") {
        val selection = StorySelection.Selected(story)
        selection.shouldBeInstanceOf<StorySelection.Selected>()
        selection.story shouldBe story
    }

    test("None represents the absence of an executable story") {
        StorySelection.None.shouldBeInstanceOf<StorySelection.None>()
    }
})
