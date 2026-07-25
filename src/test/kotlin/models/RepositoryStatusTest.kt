package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RepositoryStatusTest : FunSpec({

    test("changedFiles defaults to empty") {
        RepositoryStatus(hasChanges = false).changedFiles shouldBe emptyList()
    }

    test("carries hasChanges and the changed file list") {
        val status = RepositoryStatus(hasChanges = true, changedFiles = listOf("README.md", "src/Foo.kt"))

        status.hasChanges shouldBe true
        status.changedFiles shouldBe listOf("README.md", "src/Foo.kt")
    }
})
