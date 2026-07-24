package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import models.ProgressState
import models.StoryStatus

class ProgressStateFormatterTest : FunSpec({

    test("an empty state formats as a clear no-op message") {
        ProgressStateFormatter.format(ProgressState()) shouldBe "No progress recorded yet."
    }

    test("recorded stories are listed with their status") {
        val state = ProgressState()
            .withStatus("ADO-001", StoryStatus.DONE)
            .withStatus("ADO-002", StoryStatus.IN_PROGRESS)

        val output = ProgressStateFormatter.format(state)

        output shouldContain "ADO-001: done"
        output shouldContain "ADO-002: in_progress"
    }
})
