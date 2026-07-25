package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import models.ReviewResult

class ReviewResultFormatterTest : FunSpec({

    test("shows zero counts and (none) when there are no findings") {
        val output = ReviewResultFormatter.format(ReviewResult())

        output shouldContain "Blocking Issues (0):"
        output shouldContain "Recommendations (0):"
        output shouldContain "(none)"
    }

    test("lists blocking issues and recommendations") {
        val result = ReviewResult(
            blockingIssues = listOf("Thread-unsafe access"),
            recommendations = listOf("Extract a helper function"),
        )

        val output = ReviewResultFormatter.format(result)

        output shouldContain "Blocking Issues (1):"
        output shouldContain "Thread-unsafe access"
        output shouldContain "Recommendations (1):"
        output shouldContain "Extract a helper function"
    }
})
