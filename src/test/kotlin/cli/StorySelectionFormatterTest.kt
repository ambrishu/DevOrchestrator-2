package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import models.Story
import models.StorySelection
import models.StoryStatus

class StorySelectionFormatterTest : FunSpec({

    test("None formats as a clear no-op message") {
        StorySelectionFormatter.format(StorySelection.None) shouldBe "No executable story found."
    }

    test("Selected formats the story's id, title, status, and dependencies") {
        val story = Story(
            id = "ADO-002",
            title = "Second Story",
            description = "Does the second thing.",
            status = StoryStatus.TODO,
            dependencies = listOf("ADO-001"),
        )

        val output = StorySelectionFormatter.format(StorySelection.Selected(story))

        output shouldContain "ADO-002 — Second Story"
        output shouldContain "Status:      TODO"
        output shouldContain "Depends On:  ADO-001"
        output shouldContain "Does the second thing."
    }

    test("Selected shows \"none\" for a story without dependencies") {
        val story = Story(id = "ADO-001", title = "First", description = "d", status = StoryStatus.TODO)

        StorySelectionFormatter.format(StorySelection.Selected(story)) shouldContain "Depends On:  none"
    }
})
