package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import models.FailureAnalysis
import models.FailureCategory

class FailureAnalysisFormatterTest : FunSpec({

    test("shows the category and detail count with no details") {
        val output = FailureAnalysisFormatter.format(FailureAnalysis(FailureCategory.UNKNOWN))

        output shouldContain "Category: UNKNOWN"
        output shouldContain "Details (0):"
        output shouldContain "(none)"
    }

    test("lists each detail line") {
        val analysis = FailureAnalysis(
            category = FailureCategory.COMPILATION,
            details = listOf("e: Foo.kt: Unresolved reference: bar"),
        )

        val output = FailureAnalysisFormatter.format(analysis)

        output shouldContain "Category: COMPILATION"
        output shouldContain "e: Foo.kt: Unresolved reference: bar"
    }
})
