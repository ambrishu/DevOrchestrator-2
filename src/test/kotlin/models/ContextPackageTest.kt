package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ContextPackageTest : FunSpec({

    val story = Story(id = "ADO-001", title = "Example", description = "desc", status = StoryStatus.TODO)

    test("optional sections default to empty") {
        val context = ContextPackage(story = story, acceptanceCriteria = listOf("It works"))

        context.prdExcerpts shouldBe emptyList()
        context.architectureRules shouldBe emptyList()
        context.adrReferences shouldBe emptyList()
        context.codingStandards shouldBe emptyList()
        context.impactedSourceFiles shouldBe emptyList()
        context.relatedTests shouldBe emptyList()
        context.expectedDeliverables shouldBe emptyList()
        context.failureAnalysis shouldBe null
    }

    test("failureAnalysis can be set for a repair attempt") {
        val analysis = FailureAnalysis(FailureCategory.COMPILATION, listOf("e: Foo.kt: Unresolved reference: bar"))
        val context = ContextPackage(story = story, acceptanceCriteria = emptyList(), failureAnalysis = analysis)

        context.failureAnalysis shouldBe analysis
    }

    test("carries the story and its acceptance criteria") {
        val context = ContextPackage(story = story, acceptanceCriteria = listOf("It works", "It is tested"))

        context.story shouldBe story
        context.acceptanceCriteria shouldBe listOf("It works", "It is tested")
    }

    test("carries selected source files and related tests") {
        val sourceFile = SourceFile("src/main/kotlin/models/Story.kt", "package models")
        val testFile = SourceFile("src/test/kotlin/models/StoryTest.kt", "package models")

        val context = ContextPackage(
            story = story,
            acceptanceCriteria = emptyList(),
            impactedSourceFiles = listOf(sourceFile),
            relatedTests = listOf(testFile),
        )

        context.impactedSourceFiles shouldBe listOf(sourceFile)
        context.relatedTests shouldBe listOf(testFile)
    }
})
