package core.git

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.Story
import models.StoryStatus

class DefaultCommitMessageFormatterTest : FunSpec({

    val formatter = DefaultCommitMessageFormatter()

    test("formats as feat(story-id): lowercased title") {
        val story = Story(id = "ADO-001", title = "Initialize Gradle Kotlin Project", description = "", status = StoryStatus.TODO)

        formatter.format(story) shouldBe "feat(ADO-001): initialize gradle kotlin project"
    }

    test("uses the story's own ID as scope") {
        val story = Story(id = "ADO-057", title = "Implement Story Planner", description = "", status = StoryStatus.TODO)

        formatter.format(story) shouldBe "feat(ADO-057): implement story planner"
    }
})
