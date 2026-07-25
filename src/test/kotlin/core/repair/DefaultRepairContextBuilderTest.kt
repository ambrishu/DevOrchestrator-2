package core.repair

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.ContextPackage
import models.FailureAnalysis
import models.FailureCategory
import models.Story
import models.StoryStatus

class DefaultRepairContextBuilderTest : FunSpec({

    val builder = DefaultRepairContextBuilder()
    val story = Story(id = "ADO-001", title = "Example", description = "d", status = StoryStatus.TODO)

    test("carries the failure analysis into the returned context") {
        val original = ContextPackage(story, listOf("It works"))
        val analysis = FailureAnalysis(FailureCategory.TESTING, listOf("AssertionError"))

        val repairContext = builder.buildRepairContext(original, analysis)

        repairContext.failureAnalysis shouldBe analysis
    }

    test("preserves every other field from the original context") {
        val original = ContextPackage(
            story = story,
            acceptanceCriteria = listOf("It works"),
            prdExcerpts = listOf("PRD text"),
        )
        val analysis = FailureAnalysis(FailureCategory.COMPILATION)

        val repairContext = builder.buildRepairContext(original, analysis)

        repairContext.story shouldBe story
        repairContext.acceptanceCriteria shouldBe listOf("It works")
        repairContext.prdExcerpts shouldBe listOf("PRD text")
    }

    test("does not mutate the original context") {
        val original = ContextPackage(story, emptyList())

        builder.buildRepairContext(original, FailureAnalysis(FailureCategory.UNKNOWN))

        original.failureAnalysis shouldBe null
    }
})
