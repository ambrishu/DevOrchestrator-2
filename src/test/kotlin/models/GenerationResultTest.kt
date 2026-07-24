package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GenerationResultTest : FunSpec({

    test("modifiedFiles and implementationNotes default to empty") {
        val result = GenerationResult(summary = "Did the thing.")

        result.modifiedFiles shouldBe emptyList()
        result.implementationNotes shouldBe ""
    }

    test("carries modified files, summary, and implementation notes") {
        val result = GenerationResult(
            modifiedFiles = listOf("src/main/kotlin/Foo.kt"),
            summary = "Implemented Foo.",
            implementationNotes = "Used a straightforward approach.",
        )

        result.modifiedFiles shouldBe listOf("src/main/kotlin/Foo.kt")
        result.summary shouldBe "Implemented Foo."
        result.implementationNotes shouldBe "Used a straightforward approach."
    }
})
