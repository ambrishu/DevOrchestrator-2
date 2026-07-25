package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FailureCategoryTest : FunSpec({

    test("the five documented categories plus UNKNOWN all exist") {
        FailureCategory.entries.map { it.name } shouldBe listOf(
            "COMPILATION", "TESTING", "DEPENDENCY", "FORMATTING", "ARCHITECTURE", "UNKNOWN",
        )
    }
})
