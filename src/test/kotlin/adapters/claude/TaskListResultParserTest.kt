package adapters.claude

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.ProcessResult

class TaskListResultParserTest : FunSpec({

    val parser = TaskListResultParser()

    fun processResult(stdout: String) = ProcessResult(exitCode = 0, stdout = stdout, stderr = "", durationMillis = 5)

    val backlog = """
        ADO-001 — First Task

        Status: todo

        Depends On: None

        Description

        Does the first thing.

        Acceptance Criteria

        * It works

        ⸻
    """.trimIndent()

    test("returns clean backlog markdown unchanged") {
        parser.parse(processResult(backlog)) shouldBe backlog
    }

    test("strips a wrapping markdown code fence") {
        val fenced = "```markdown\n$backlog\n```"

        parser.parse(processResult(fenced)) shouldBe backlog
    }

    test("strips a plain wrapping code fence") {
        val fenced = "```\n$backlog\n```"

        parser.parse(processResult(fenced)) shouldBe backlog
    }

    test("strips an introductory sentence before the first task header") {
        val withPreamble = "Here is the generated backlog:\n\n$backlog"

        parser.parse(processResult(withPreamble)) shouldBe backlog
    }

    test("trims surrounding whitespace") {
        parser.parse(processResult("\n\n  $backlog  \n\n")) shouldBe backlog
    }
})
