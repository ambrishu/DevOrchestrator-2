package core.context

import core.common.exception.ContextException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path

class DefaultRepositoryScannerTest : FunSpec({

    val scanner = DefaultRepositoryScanner()

    fun createFile(root: Path, relative: String) {
        val file = root.resolve(relative)
        Files.createDirectories(file.parent)
        Files.writeString(file, "content")
    }

    test("throws when the repository path is not a directory") {
        val repo = Files.createTempDirectory("ado-scanner-test")
        shouldThrow<ContextException> { scanner.scan(repo.resolve("missing")) }
    }

    test("discovers regular files as relative paths") {
        val repo = Files.createTempDirectory("ado-scanner-test")
        createFile(repo, "src/main/kotlin/models/Story.kt")
        createFile(repo, "docs/01-vision.md")

        val files = scanner.scan(repo).map { it.toString() }

        files shouldContain "src/main/kotlin/models/Story.kt"
        files shouldContain "docs/01-vision.md"
    }

    test("excludes files under .git") {
        val repo = Files.createTempDirectory("ado-scanner-test")
        createFile(repo, ".git/HEAD")
        createFile(repo, "README.md")

        val files = scanner.scan(repo).map { it.toString() }

        files shouldNotContain ".git/HEAD"
        files shouldContain "README.md"
    }

    test("excludes files under build, .gradle, .idea, .ado, and node_modules") {
        val repo = Files.createTempDirectory("ado-scanner-test")
        createFile(repo, "build/classes/Main.class")
        createFile(repo, ".gradle/cache.bin")
        createFile(repo, ".idea/workspace.xml")
        createFile(repo, ".ado/progress.yaml")
        createFile(repo, "node_modules/pkg/index.js")
        createFile(repo, "README.md")

        val files = scanner.scan(repo).map { it.toString() }

        files shouldBe listOf("README.md")
    }

    test("returns an empty list for an empty repository") {
        val repo = Files.createTempDirectory("ado-scanner-test")
        scanner.scan(repo) shouldBe emptyList()
    }
})
