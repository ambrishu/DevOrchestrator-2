package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.CommitResult

class CommitResultFormatterTest : FunSpec({

    test("formats a success with the commit SHA") {
        CommitResultFormatter.format(CommitResult(success = true, commitSha = "abc123")) shouldBe "Committed abc123"
    }

    test("formats a failure with the reason") {
        CommitResultFormatter.format(CommitResult(success = false, failureReason = "nothing to commit")) shouldBe
            "Commit failed: nothing to commit"
    }
})
