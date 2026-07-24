package core.context

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Path

class RepositoryFileClassifierTest : FunSpec({

    test("a file under src/main/kotlin ending in .kt is a source file") {
        RepositoryFileClassifier.isSourceFile(Path.of("src/main/kotlin/models/Story.kt")) shouldBe true
    }

    test("a file under src/test/kotlin is not a source file") {
        RepositoryFileClassifier.isSourceFile(Path.of("src/test/kotlin/models/StoryTest.kt")) shouldBe false
    }

    test("a non-.kt file under src/main/kotlin is not a source file") {
        RepositoryFileClassifier.isSourceFile(Path.of("src/main/kotlin/resources/logback.xml")) shouldBe false
    }

    test("a file under src/test/kotlin ending in .kt is a test file") {
        RepositoryFileClassifier.isTestFile(Path.of("src/test/kotlin/models/StoryTest.kt")) shouldBe true
    }

    test("a file under src/main/kotlin is not a test file") {
        RepositoryFileClassifier.isTestFile(Path.of("src/main/kotlin/models/Story.kt")) shouldBe false
    }

    test("a markdown file under docs is a documentation file") {
        RepositoryFileClassifier.isDocumentationFile(Path.of("docs/01-vision.md")) shouldBe true
    }

    test("a non-markdown file under docs is not a documentation file") {
        RepositoryFileClassifier.isDocumentationFile(Path.of("docs/diagram.png")) shouldBe false
    }

    test("a markdown file outside docs is not a documentation file") {
        RepositoryFileClassifier.isDocumentationFile(Path.of("README.md")) shouldBe false
    }
})
