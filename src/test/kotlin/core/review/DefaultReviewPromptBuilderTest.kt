package core.review

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import models.ContextPackage
import models.SourceFile
import models.Story
import models.StoryStatus

class DefaultReviewPromptBuilderTest : FunSpec({

    val builder = DefaultReviewPromptBuilder()

    val story = Story(
        id = "ADO-002",
        title = "Second Story",
        description = "Implements the second thing.",
        status = StoryStatus.TODO,
    )

    test("includes the story id, title, and description") {
        val prompt = builder.buildPrompt(ContextPackage(story, emptyList()), emptyList())

        prompt shouldContain "ADO-002 — Second Story"
        prompt shouldContain "Implements the second thing."
    }

    test("includes acceptance criteria") {
        val prompt = builder.buildPrompt(ContextPackage(story, listOf("It works")), emptyList())

        prompt shouldContain "It works"
    }

    test("includes the documented review checks") {
        val prompt = builder.buildPrompt(ContextPackage(story, emptyList()), emptyList())

        prompt shouldContain "Architecture compliance"
        prompt shouldContain "SOLID principles"
        prompt shouldContain "Naming"
        prompt shouldContain "Readability"
        prompt shouldContain "Maintainability"
        prompt shouldContain "Performance"
        prompt shouldContain "Thread safety"
        prompt shouldContain "Test coverage"
    }

    test("includes the output protocol instructions") {
        val prompt = builder.buildPrompt(ContextPackage(story, emptyList()), emptyList())

        prompt shouldContain "BLOCKING:"
        prompt shouldContain "RECOMMENDATION:"
    }

    test("explicitly tells the agent not to write a BLOCKING: none confirmation line") {
        val prompt = builder.buildPrompt(ContextPackage(story, emptyList()), emptyList())

        prompt shouldContain "omit it"
        prompt shouldContain "BLOCKING: none"
    }

    test("includes changed file paths and content") {
        val prompt = builder.buildPrompt(
            ContextPackage(story, emptyList()),
            listOf(SourceFile("src/main/kotlin/Foo.kt", "class Foo")),
        )

        prompt shouldContain "src/main/kotlin/Foo.kt"
        prompt shouldContain "class Foo"
    }

    test("notes when there are no changed files") {
        val prompt = builder.buildPrompt(ContextPackage(story, emptyList()), emptyList())

        prompt shouldContain "no changed files detected"
    }
})
