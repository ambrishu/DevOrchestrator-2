package core.repository

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

class DefaultRepositoryLoaderTest : FunSpec({

    val loader = DefaultRepositoryLoader()

    test("a non-existent path is not ready and reports an issue") {
        val dir = Files.createTempDirectory("ado-repo-test")
        val missing = dir.resolve("does-not-exist")

        val readiness = loader.validate(missing)

        readiness.exists shouldBe false
        readiness.isGitRepository shouldBe false
        readiness.isReady shouldBe false
        readiness.issues shouldBe listOf("Repository path does not exist: $missing")
    }

    test("an existing directory without a .git entry is not a git repository") {
        val dir = Files.createTempDirectory("ado-repo-test")

        val readiness = loader.validate(dir)

        readiness.exists shouldBe true
        readiness.isDirectory shouldBe true
        readiness.isGitRepository shouldBe false
        readiness.isReady shouldBe false
    }

    test("a directory with a .git entry is a git repository and ready") {
        val dir = Files.createTempDirectory("ado-repo-test")
        Files.createDirectory(dir.resolve(".git"))

        val readiness = loader.validate(dir)

        readiness.isGitRepository shouldBe true
        readiness.isReady shouldBe true
        readiness.issues shouldBe emptyList()
    }

    test("a git worktree with a .git file (not directory) is still recognized") {
        val dir = Files.createTempDirectory("ado-repo-test")
        Files.writeString(dir.resolve(".git"), "gitdir: /elsewhere/.git/worktrees/example\n")

        val readiness = loader.validate(dir)

        readiness.isGitRepository shouldBe true
    }

    test("documentation is detected when a docs directory is present") {
        val dir = Files.createTempDirectory("ado-repo-test")
        Files.createDirectory(dir.resolve(".git"))
        Files.createDirectory(dir.resolve("docs"))

        loader.validate(dir).hasDocumentation shouldBe true
    }

    test("documentation is not detected when no docs directory is present") {
        val dir = Files.createTempDirectory("ado-repo-test")
        Files.createDirectory(dir.resolve(".git"))

        loader.validate(dir).hasDocumentation shouldBe false
    }

    test("configuration is detected when .ado/config.yaml is present") {
        val dir = Files.createTempDirectory("ado-repo-test")
        Files.createDirectory(dir.resolve(".git"))
        val adoDir = Files.createDirectory(dir.resolve(".ado"))
        Files.writeString(adoDir.resolve("config.yaml"), "agent:\n  provider: claude-code\n")

        loader.validate(dir).hasConfiguration shouldBe true
    }

    test("configuration is not detected when .ado/config.yaml is absent") {
        val dir = Files.createTempDirectory("ado-repo-test")
        Files.createDirectory(dir.resolve(".git"))

        loader.validate(dir).hasConfiguration shouldBe false
    }

    test("validate never creates files or directories in the target path") {
        val dir = Files.createTempDirectory("ado-repo-test")

        loader.validate(dir)

        Files.list(dir).use { it.count() } shouldBe 0
    }

    test("a file path (not a directory) is reported as not a directory") {
        val dir = Files.createTempDirectory("ado-repo-test")
        val file = dir.resolve("some-file.txt")
        Files.writeString(file, "content")

        val readiness = loader.validate(file)

        readiness.exists shouldBe true
        readiness.isDirectory shouldBe false
        readiness.isReady shouldBe false
    }
})
