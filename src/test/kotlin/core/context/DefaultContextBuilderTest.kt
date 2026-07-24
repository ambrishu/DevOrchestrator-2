package core.context

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import models.Story
import models.StoryStatus
import java.nio.file.Files
import java.nio.file.Path

class DefaultContextBuilderTest : FunSpec({

    val builder = DefaultContextBuilder()

    fun createFile(root: Path, relative: String, content: String = "content") {
        val file = root.resolve(relative)
        Files.createDirectories(file.parent)
        Files.writeString(file, content)
    }

    fun fixtureRepo(): Path {
        val repo = Files.createTempDirectory("ado-context-builder-test")
        createFile(repo, "docs/02-product-requirements.md", "PRD content")
        createFile(repo, "docs/03-system-architecture.md", "Architecture content")
        createFile(repo, "src/main/kotlin/core/planner/StoryPlanner.kt", "interface StoryPlanner")
        createFile(repo, "src/test/kotlin/core/planner/StoryPlannerTest.kt", "class StoryPlannerTest")
        createFile(repo, "src/main/kotlin/core/configuration/ConfigurationLoader.kt", "interface ConfigurationLoader")
        return repo
    }

    fun story(title: String, acceptanceCriteria: List<String> = listOf("It works")) =
        Story(id = "ADO-001", title = title, description = "", status = StoryStatus.TODO, acceptanceCriteria = acceptanceCriteria)

    test("includes the story and its acceptance criteria") {
        val repo = fixtureRepo()
        val context = builder.buildContext(story("Rework the planner", listOf("A", "B")), repo)

        context.story.id shouldBe "ADO-001"
        context.acceptanceCriteria shouldBe listOf("A", "B")
    }

    test("loads PRD and architecture excerpts when present") {
        val repo = fixtureRepo()
        val context = builder.buildContext(story("Rework the planner"), repo)

        context.prdExcerpts shouldBe listOf("PRD content")
        context.architectureRules shouldBe listOf("Architecture content")
    }

    test("leaves documentation excerpts empty when the files are missing") {
        val repo = Files.createTempDirectory("ado-context-builder-test")
        createFile(repo, "src/main/kotlin/core/planner/StoryPlanner.kt")

        val context = builder.buildContext(story("Rework the planner"), repo)

        context.prdExcerpts shouldBe emptyList()
        context.architectureRules shouldBe emptyList()
    }

    test("includes impacted source files selected by the source selector, with content") {
        val repo = fixtureRepo()
        val context = builder.buildContext(story("Rework the planner"), repo)

        context.impactedSourceFiles.shouldHaveSize(1)
        val sourceFile = context.impactedSourceFiles.first()
        sourceFile.path shouldBe "src/main/kotlin/core/planner/StoryPlanner.kt"
        sourceFile.content shouldBe "interface StoryPlanner"
    }

    test("includes the related test for each selected source file, when it exists") {
        val repo = fixtureRepo()
        val context = builder.buildContext(story("Rework the planner"), repo)

        context.relatedTests.shouldHaveSize(1)
        context.relatedTests.first().path shouldBe "src/test/kotlin/core/planner/StoryPlannerTest.kt"
    }

    test("excludes tests for source files that have no corresponding test") {
        val repo = fixtureRepo()
        val context = builder.buildContext(story("Rework the configuration"), repo)

        context.impactedSourceFiles.map { it.path } shouldContain "src/main/kotlin/core/configuration/ConfigurationLoader.kt"
        context.relatedTests shouldBe emptyList()
    }

    test("selects no source files when nothing in the story text matches") {
        val repo = fixtureRepo()
        val context = builder.buildContext(story("Write the README"), repo)

        context.impactedSourceFiles shouldBe emptyList()
        context.relatedTests shouldBe emptyList()
    }

    test("builds context from the project's own real repository without error") {
        val repositoryRoot = Path.of("").toAbsolutePath()
        val realStory = Story(
            id = "ADO-057",
            title = "Implement Story Planner",
            description = "Complete the Story Planner implementation.",
            status = StoryStatus.DONE,
        )

        val context = builder.buildContext(realStory, repositoryRoot)

        context.prdExcerpts.shouldHaveSize(1)
        context.architectureRules.shouldHaveSize(1)
        context.impactedSourceFiles.map { it.path } shouldContain "src/main/kotlin/core/planner/StoryPlanner.kt"
    }
})
