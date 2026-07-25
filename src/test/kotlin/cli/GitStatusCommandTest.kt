package cli

import com.github.ajalt.clikt.testing.test
import core.common.exception.GitOperationException
import core.git.GitManager
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import models.RepositoryStatus
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class GitStatusCommandTest : FunSpec({

    lateinit var gitManager: GitManager

    beforeTest {
        gitManager = mockk()
        startKoin { modules(module { single { gitManager } }) }
    }

    afterTest {
        stopKoin()
    }

    test("prints the status and exits 0 for a clean repository") {
        every { gitManager.inspectStatus(any<Path>()) } returns RepositoryStatus(hasChanges = false)

        val result = GitStatusCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "No changes."
    }

    test("prints changed files and exits 0 for a dirty repository") {
        every { gitManager.inspectStatus(any<Path>()) } returns
            RepositoryStatus(hasChanges = true, changedFiles = listOf("README.md"))

        val result = GitStatusCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "README.md"
    }

    test("exits 1 and reports the error when status cannot be read") {
        every { gitManager.inspectStatus(any<Path>()) } throws GitOperationException("not a git repository")

        val result = GitStatusCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "not a git repository"
    }
})
