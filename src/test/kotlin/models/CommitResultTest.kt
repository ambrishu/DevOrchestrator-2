package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class CommitResultTest : FunSpec({

    test("a successful result carries the commit SHA") {
        val result = CommitResult(success = true, commitSha = "abc123")

        result.success shouldBe true
        result.commitSha shouldBe "abc123"
        result.failureReason.shouldBeNull()
    }

    test("a failed result carries a failure reason") {
        val result = CommitResult(success = false, failureReason = "nothing to commit")

        result.success shouldBe false
        result.commitSha.shouldBeNull()
        result.failureReason shouldBe "nothing to commit"
    }
})
