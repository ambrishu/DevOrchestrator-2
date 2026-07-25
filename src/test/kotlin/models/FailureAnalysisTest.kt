package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FailureAnalysisTest : FunSpec({

    test("details default to empty") {
        FailureAnalysis(FailureCategory.UNKNOWN).details shouldBe emptyList()
    }

    test("carries the category and relevant detail lines") {
        val analysis = FailureAnalysis(
            category = FailureCategory.COMPILATION,
            details = listOf("e: Foo.kt: Unresolved reference: bar"),
        )

        analysis.category shouldBe FailureCategory.COMPILATION
        analysis.details shouldBe listOf("e: Foo.kt: Unresolved reference: bar")
    }
})
