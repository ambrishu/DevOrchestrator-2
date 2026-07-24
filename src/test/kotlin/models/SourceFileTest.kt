package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SourceFileTest : FunSpec({

    test("carries a repository-relative path and content") {
        val file = SourceFile(path = "src/main/kotlin/models/Story.kt", content = "package models")

        file.path shouldBe "src/main/kotlin/models/Story.kt"
        file.content shouldBe "package models"
    }

    test("supports structural equality") {
        SourceFile("a", "b") shouldBe SourceFile("a", "b")
    }
})
