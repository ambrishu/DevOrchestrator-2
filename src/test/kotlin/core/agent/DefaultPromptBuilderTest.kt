package core.agent

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import models.ContextPackage
import models.FailureAnalysis
import models.FailureCategory
import models.SourceFile
import models.Story
import models.StoryStatus

class DefaultPromptBuilderTest : FunSpec({

    val builder = DefaultPromptBuilder()

    val story = Story(
        id = "ADO-002",
        title = "Second Story",
        description = "Implements the second thing.",
        status = StoryStatus.TODO,
    )

    test("includes the story id, title, and description") {
        val prompt = builder.buildPrompt(ContextPackage(story, emptyList()))

        prompt shouldContain "ADO-002 — Second Story"
        prompt shouldContain "Implements the second thing."
    }

    test("includes the documented agent rules") {
        val prompt = builder.buildPrompt(ContextPackage(story, emptyList()))

        prompt shouldContain "Implement exactly one story."
        prompt shouldContain "Do not implement future stories."
        prompt shouldContain "Do not refactor unrelated code."
        prompt shouldContain "Follow architecture documents."
        prompt shouldContain "Write tests with production code."
        prompt shouldContain "Keep changes focused."
    }

    test("includes acceptance criteria") {
        val prompt = builder.buildPrompt(ContextPackage(story, listOf("It works", "It is tested")))

        prompt shouldContain "It works"
        prompt shouldContain "It is tested"
    }

    test("includes PRD and architecture excerpts when present") {
        val context = ContextPackage(
            story = story,
            acceptanceCriteria = emptyList(),
            prdExcerpts = listOf("PRD text"),
            architectureRules = listOf("Architecture text"),
        )

        val prompt = builder.buildPrompt(context)

        prompt shouldContain "PRD text"
        prompt shouldContain "Architecture text"
    }

    test("includes selected source files and related tests with their content") {
        val context = ContextPackage(
            story = story,
            acceptanceCriteria = emptyList(),
            impactedSourceFiles = listOf(SourceFile("src/main/kotlin/Foo.kt", "class Foo")),
            relatedTests = listOf(SourceFile("src/test/kotlin/FooTest.kt", "class FooTest")),
        )

        val prompt = builder.buildPrompt(context)

        prompt shouldContain "src/main/kotlin/Foo.kt"
        prompt shouldContain "class Foo"
        prompt shouldContain "src/test/kotlin/FooTest.kt"
        prompt shouldContain "class FooTest"
    }

    test("does not include a failure section for ordinary generation") {
        val prompt = builder.buildPrompt(ContextPackage(story, emptyList()))

        prompt shouldNotContain "Previous Build Failure"
    }

    test("includes the failure category, detail lines, and a fix-only instruction when repairing") {
        val context = ContextPackage(
            story = story,
            acceptanceCriteria = emptyList(),
            failureAnalysis = FailureAnalysis(FailureCategory.COMPILATION, listOf("e: Foo.kt: Unresolved reference: bar")),
        )

        val prompt = builder.buildPrompt(context)

        prompt shouldContain "Previous Build Failure (COMPILATION)"
        prompt shouldContain "e: Foo.kt: Unresolved reference: bar"
        prompt shouldContain "Fix only this failure. Do not make unrelated changes."
    }
})
