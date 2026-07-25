package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class QualityGateResultTest : FunSpec({

    test("details default to empty") {
        QualityGateResult(name = "build", passed = true).details shouldBe ""
    }

    test("carries the gate name, pass status, and details") {
        val result = QualityGateResult(name = "unitTest", passed = false, details = "3 tests failed")

        result.name shouldBe "unitTest"
        result.passed shouldBe false
        result.details shouldBe "3 tests failed"
    }
})
