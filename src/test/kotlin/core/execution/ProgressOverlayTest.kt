package core.execution

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.ProgressState
import models.Story
import models.StoryStatus

class ProgressOverlayTest : FunSpec({

    fun story(id: String, status: StoryStatus) = Story(id = id, title = id, description = "", status = status)

    test("keeps the story's own status when progress has no entry for it") {
        val story = story("ADO-001", StoryStatus.TODO)

        ProgressOverlay.apply(listOf(story), ProgressState()) shouldBe listOf(story)
    }

    test("overrides the status with the recorded progress entry") {
        val story = story("ADO-001", StoryStatus.TODO)
        val progress = ProgressState().withStatus("ADO-001", StoryStatus.REVIEW)

        ProgressOverlay.apply(listOf(story), progress) shouldBe listOf(story.copy(status = StoryStatus.REVIEW))
    }

    test("applies overrides independently across multiple stories") {
        val storyA = story("ADO-001", StoryStatus.TODO)
        val storyB = story("ADO-002", StoryStatus.TODO)
        val progress = ProgressState().withStatus("ADO-001", StoryStatus.DONE)

        ProgressOverlay.apply(listOf(storyA, storyB), progress) shouldBe listOf(
            storyA.copy(status = StoryStatus.DONE),
            storyB,
        )
    }

    test("does not modify the original story list") {
        val story = story("ADO-001", StoryStatus.TODO)
        val stories = listOf(story)
        ProgressOverlay.apply(stories, ProgressState().withStatus("ADO-001", StoryStatus.DONE))

        stories shouldBe listOf(story)
    }
})
