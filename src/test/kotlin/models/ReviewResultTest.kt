package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ReviewResultTest : FunSpec({

    test("hasBlockingIssues is false when there are no blocking issues") {
        ReviewResult().hasBlockingIssues shouldBe false
    }

    test("hasBlockingIssues is true when at least one blocking issue is present") {
        ReviewResult(blockingIssues = listOf("Thread-unsafe access")).hasBlockingIssues shouldBe true
    }

    test("carries blocking issues and recommendations independently") {
        val result = ReviewResult(
            blockingIssues = listOf("Missing null check"),
            recommendations = listOf("Consider extracting a helper function"),
        )

        result.blockingIssues shouldBe listOf("Missing null check")
        result.recommendations shouldBe listOf("Consider extracting a helper function")
    }
})
