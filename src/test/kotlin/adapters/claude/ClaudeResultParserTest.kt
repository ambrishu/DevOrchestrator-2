package adapters.claude

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.ProcessResult

class ClaudeResultParserTest : FunSpec({

    val parser = ClaudeResultParser()

    test("maps stdout to the summary and stderr to implementation notes") {
        val result = parser.parse(ProcessResult(exitCode = 0, stdout = "Implemented the story.", stderr = "note", durationMillis = 5))

        result.summary shouldBe "Implemented the story."
        result.implementationNotes shouldBe "note"
    }

    test("leaves modified files empty (deferred to the Git Manager)") {
        val result = parser.parse(ProcessResult(exitCode = 0, stdout = "done", stderr = "", durationMillis = 5))

        result.modifiedFiles shouldBe emptyList()
    }
})
