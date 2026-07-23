package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RepositoryReadinessTest : FunSpec({

    fun readyBase() = RepositoryReadiness(
        repositoryPath = "/repo",
        exists = true,
        isDirectory = true,
        isGitRepository = true,
        isWritable = true,
        hasDocumentation = true,
        hasConfiguration = true,
        issues = emptyList(),
    )

    test("isReady is true when all checks pass and there are no issues") {
        readyBase().isReady shouldBe true
    }

    test("isReady is false when the path does not exist") {
        readyBase().copy(exists = false).isReady shouldBe false
    }

    test("isReady is false when it is not a git repository") {
        readyBase().copy(isGitRepository = false).isReady shouldBe false
    }

    test("isReady is false when the path is not writable") {
        readyBase().copy(isWritable = false).isReady shouldBe false
    }

    test("isReady is false when issues are present, even if all flags pass") {
        readyBase().copy(issues = listOf("something odd")).isReady shouldBe false
    }

    test("missing documentation and configuration do not block readiness") {
        readyBase().copy(hasDocumentation = false, hasConfiguration = false).isReady shouldBe true
    }
})
