package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConfigurationValidationResultTest : FunSpec({

    test("isValid is true when there are no errors") {
        ConfigurationValidationResult().isValid shouldBe true
    }

    test("isValid is false when errors are present") {
        ConfigurationValidationResult(listOf("something is wrong")).isValid shouldBe false
    }
})
