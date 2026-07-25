package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import models.RepositoryStatus

class RepositoryStatusFormatterTest : FunSpec({

    test("a clean repository formats as a clear no-op message") {
        RepositoryStatusFormatter.format(RepositoryStatus(hasChanges = false)) shouldBe "No changes."
    }

    test("lists each changed file") {
        val output = RepositoryStatusFormatter.format(
            RepositoryStatus(hasChanges = true, changedFiles = listOf("README.md", "src/Foo.kt")),
        )

        output shouldContain "Changes (2):"
        output shouldContain "README.md"
        output shouldContain "src/Foo.kt"
    }
})
