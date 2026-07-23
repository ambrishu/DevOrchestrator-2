package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StoryTest : FunSpec({

    test("dependencies and acceptance criteria default to empty") {
        val story = Story(id = "ADO-001", title = "Example", description = "desc", status = StoryStatus.TODO)

        story.dependencies shouldBe emptyList()
        story.acceptanceCriteria shouldBe emptyList()
    }

    test("dependencies and acceptance criteria can be represented") {
        val story = Story(
            id = "ADO-002",
            title = "Example",
            description = "desc",
            status = StoryStatus.DONE,
            dependencies = listOf("ADO-001"),
            acceptanceCriteria = listOf("It works", "It is tested"),
        )

        story.dependencies shouldBe listOf("ADO-001")
        story.acceptanceCriteria shouldBe listOf("It works", "It is tested")
    }

    test("stories support structural equality") {
        val a = Story(id = "ADO-001", title = "Example", description = "desc", status = StoryStatus.TODO)
        val b = Story(id = "ADO-001", title = "Example", description = "desc", status = StoryStatus.TODO)

        a shouldBe b
    }
})
