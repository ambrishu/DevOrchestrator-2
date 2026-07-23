package core.planner

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import models.Story
import models.StorySelection
import models.StoryStatus

class DefaultStoryPlannerTest : FunSpec({

    val planner = DefaultStoryPlanner()

    fun story(id: String, status: StoryStatus, dependencies: List<String> = emptyList()) =
        Story(id = id, title = id, description = "", status = status, dependencies = dependencies)

    test("selects the first todo story in document order") {
        val stories = listOf(
            story("ADO-001", StoryStatus.DONE),
            story("ADO-002", StoryStatus.TODO),
            story("ADO-003", StoryStatus.TODO),
        )

        val selection = planner.selectNext(stories)

        selection.shouldBeInstanceOf<StorySelection.Selected>()
        (selection as StorySelection.Selected).story.id shouldBe "ADO-002"
    }

    test("skips a todo story whose dependencies are incomplete") {
        val stories = listOf(
            story("ADO-001", StoryStatus.TODO),
            story("ADO-002", StoryStatus.TODO, dependencies = listOf("ADO-001")),
        )

        val selection = planner.selectNext(stories)

        (selection as StorySelection.Selected).story.id shouldBe "ADO-001"
    }

    test("selects a todo story once its dependency is done") {
        val stories = listOf(
            story("ADO-001", StoryStatus.DONE),
            story("ADO-002", StoryStatus.TODO, dependencies = listOf("ADO-001")),
        )

        val selection = planner.selectNext(stories)

        (selection as StorySelection.Selected).story.id shouldBe "ADO-002"
    }

    test("excludes blocked, in_progress, and done stories from selection") {
        val stories = listOf(
            story("ADO-001", StoryStatus.DONE),
            story("ADO-002", StoryStatus.BLOCKED),
            story("ADO-003", StoryStatus.IN_PROGRESS),
        )

        planner.selectNext(stories) shouldBe StorySelection.None
    }

    test("returns None when there are no stories") {
        planner.selectNext(emptyList()) shouldBe StorySelection.None
    }

    test("returns None when no story is both todo and unblocked") {
        val stories = listOf(story("ADO-001", StoryStatus.TODO, dependencies = listOf("ADO-999")))

        planner.selectNext(stories) shouldBe StorySelection.None
    }

    test("exactly one story is selected even when several are executable") {
        val stories = listOf(
            story("ADO-001", StoryStatus.TODO),
            story("ADO-002", StoryStatus.TODO),
        )

        val selection = planner.selectNext(stories) as StorySelection.Selected
        selection.story.id shouldBe "ADO-001"
    }
})
