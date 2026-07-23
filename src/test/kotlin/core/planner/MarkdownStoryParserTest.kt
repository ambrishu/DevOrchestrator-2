package core.planner

import core.common.exception.StoryLoadException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import models.StoryStatus

private const val DIVIDER = "⸻"

class MarkdownStoryParserTest : FunSpec({

    val parser = MarkdownStoryParser()

    test("parses a single story with all fields") {
        val content = """
            ADO-001 — Initialize Gradle Kotlin Project

            Status: done

            Depends On: None

            Description

            Initialize the ADO repository as a Gradle Kotlin DSL project.

            Acceptance Criteria

            * Kotlin project is configured.
            * Java 21 is configured.

            $DIVIDER
        """.trimIndent()

        val stories = parser.parse(content)

        stories.shouldHaveSize(1)
        val story = stories.first()
        story.id shouldBe "ADO-001"
        story.title shouldBe "Initialize Gradle Kotlin Project"
        story.status shouldBe StoryStatus.DONE
        story.dependencies shouldBe emptyList()
        story.description shouldBe "Initialize the ADO repository as a Gradle Kotlin DSL project."
        story.acceptanceCriteria shouldContainExactly listOf(
            "Kotlin project is configured.",
            "Java 21 is configured.",
        )
    }

    test("parses multiple stories in document order") {
        val content = twoStoryFixture()

        val stories = parser.parse(content)

        stories.map { it.id } shouldBe listOf("ADO-001", "ADO-002")
    }

    test("parses a comma-separated dependency list") {
        val content = storyFixture(id = "ADO-055", dependsOn = "ADO-052, ADO-053, ADO-054")

        parser.parse(content).first().dependencies shouldBe listOf("ADO-052", "ADO-053", "ADO-054")
    }

    test("expands a \"through\" dependency range") {
        val content = storyFixture(id = "ADO-014", dependsOn = "ADO-001 through ADO-013")

        parser.parse(content).first().dependencies shouldBe (1..13).map { "ADO-%03d".format(it) }
    }

    test("expands a mixed comma and range dependency list") {
        val content = storyFixture(id = "ADO-125", dependsOn = "ADO-103, ADO-105 through ADO-108")

        parser.parse(content).first().dependencies shouldBe listOf("ADO-103", "ADO-105", "ADO-106", "ADO-107", "ADO-108")
    }

    test("treats \"None\" as no dependencies") {
        val content = storyFixture(id = "ADO-001", dependsOn = "None")

        parser.parse(content).first().dependencies shouldBe emptyList()
    }

    test("throws for an unsupported status") {
        val content = storyFixture(id = "ADO-001", status = "archived")

        shouldThrow<StoryLoadException> { parser.parse(content) }
    }

    test("returns an empty acceptance criteria list when the section is absent") {
        val content = """
            ADO-001 — No Acceptance Section

            Status: done

            Depends On: None

            Description

            Just a description, no acceptance criteria section.

            $DIVIDER
        """.trimIndent()

        parser.parse(content).first().acceptanceCriteria shouldBe emptyList()
    }

    test("does not mistake an Epic heading for a story") {
        val content = "Epic 1 — Repository Bootstrap\n\n" + storyFixture(id = "ADO-001")

        parser.parse(content).map { it.id } shouldBe listOf("ADO-001")
    }
})

private fun storyFixture(
    id: String = "ADO-001",
    title: String = "Example Story",
    status: String = "done",
    dependsOn: String = "None",
): String = """
    $id — $title

    Status: $status

    Depends On: $dependsOn

    Description

    An example story description.

    Acceptance Criteria

    * Something is true.

    $DIVIDER
""".trimIndent()

private fun twoStoryFixture(): String =
    storyFixture(id = "ADO-001", title = "First") + "\n\n" + storyFixture(id = "ADO-002", title = "Second")
