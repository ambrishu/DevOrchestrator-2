package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import models.GenerationResult

class GenerationResultFormatterTest : FunSpec({

    test("shows an empty modified files list and the summary") {
        val output = GenerationResultFormatter.format(GenerationResult(summary = "Implemented the story."))

        output shouldContain "Modified Files (0):"
        output shouldContain "(none)"
        output shouldContain "Implemented the story."
    }

    test("lists modified files and implementation notes when present") {
        val result = GenerationResult(
            modifiedFiles = listOf("src/main/kotlin/Foo.kt"),
            summary = "Implemented Foo.",
            implementationNotes = "Took a straightforward approach.",
        )

        val output = GenerationResultFormatter.format(result)

        output shouldContain "Modified Files (1):"
        output shouldContain "src/main/kotlin/Foo.kt"
        output shouldContain "Implementation Notes:"
        output shouldContain "Took a straightforward approach."
    }
})
