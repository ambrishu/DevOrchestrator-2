package core.context

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.nio.file.Files

class FileDocumentationLoaderTest : FunSpec({

    val loader = FileDocumentationLoader()

    test("returns null when the file does not exist") {
        val repo = Files.createTempDirectory("ado-doc-loader-test")
        loader.load(repo, "docs/missing.md").shouldBeNull()
    }

    test("returns the file content when it exists") {
        val repo = Files.createTempDirectory("ado-doc-loader-test")
        val docsDir = Files.createDirectory(repo.resolve("docs"))
        Files.writeString(docsDir.resolve("01-vision.md"), "# Vision\n\nContent.")

        loader.load(repo, "docs/01-vision.md") shouldBe "# Vision\n\nContent."
    }
})
