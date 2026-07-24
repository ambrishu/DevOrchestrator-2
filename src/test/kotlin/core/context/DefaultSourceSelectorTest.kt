package core.context

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.Story
import models.StoryStatus
import java.nio.file.Path

class DefaultSourceSelectorTest : FunSpec({

    val selector = DefaultSourceSelector()

    fun story(title: String, description: String = "", acceptanceCriteria: List<String> = emptyList()) =
        Story(id = "ADO-001", title = title, description = description, status = StoryStatus.TODO, acceptanceCriteria = acceptanceCriteria)

    test("selects a file whose class name is mentioned verbatim in the story text") {
        val files = listOf(Path.of("src/main/kotlin/core/planner/StoryPlanner.kt"))

        selector.select(story(title = "Fix a bug in StoryPlanner"), files) shouldBe files
    }

    test("selects files whose containing directory is mentioned as a whole word") {
        val files = listOf(
            Path.of("src/main/kotlin/core/planner/StoryPlanner.kt"),
            Path.of("src/main/kotlin/core/planner/DependencyResolver.kt"),
            Path.of("src/main/kotlin/core/configuration/ConfigurationLoader.kt"),
        )

        val selected = selector.select(story(title = "Improve the planner module"), files)

        selected shouldBe listOf(
            Path.of("src/main/kotlin/core/planner/DependencyResolver.kt"),
            Path.of("src/main/kotlin/core/planner/StoryPlanner.kt"),
        )
    }

    test("does not select a directory name that is only a substring, not a whole word") {
        val files = listOf(Path.of("src/main/kotlin/core/planner/StoryPlanner.kt"))

        selector.select(story(title = "Rename the plannerFactory helper"), files) shouldBe emptyList()
    }

    test("considers description and acceptance criteria, not just the title") {
        val files = listOf(Path.of("src/main/kotlin/core/progress/ProgressTracker.kt"))

        selector.select(
            story(title = "Unrelated title", description = "Touches ProgressTracker directly."),
            files,
        ) shouldBe files

        selector.select(
            story(title = "Unrelated title", acceptanceCriteria = listOf("ProgressTracker behaves correctly")),
            files,
        ) shouldBe files
    }

    test("returns an empty list when nothing in the story text matches") {
        val files = listOf(Path.of("src/main/kotlin/core/planner/StoryPlanner.kt"))

        selector.select(story(title = "Write the README"), files) shouldBe emptyList()
    }

    test("returns files in deterministic sorted order") {
        val files = listOf(
            Path.of("src/main/kotlin/core/planner/StoryPlanner.kt"),
            Path.of("src/main/kotlin/core/planner/DependencyResolver.kt"),
        )

        val selected = selector.select(story(title = "Rework the planner"), files)

        selected shouldBe files.sortedBy { it.toString() }
    }
})
