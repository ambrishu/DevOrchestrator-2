package adapters.claude

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.ProcessResult

class ReviewResultParserTest : FunSpec({

    val parser = ReviewResultParser()

    fun processResult(stdout: String) = ProcessResult(exitCode = 0, stdout = stdout, stderr = "", durationMillis = 5)

    test("parses a single blocking issue") {
        val result = parser.parse(processResult("BLOCKING: Thread-unsafe access to shared state"))

        result.blockingIssues shouldBe listOf("Thread-unsafe access to shared state")
        result.recommendations shouldBe emptyList()
    }

    test("parses a single recommendation") {
        val result = parser.parse(processResult("RECOMMENDATION: Consider extracting a helper function"))

        result.recommendations shouldBe listOf("Consider extracting a helper function")
        result.blockingIssues shouldBe emptyList()
    }

    test("parses multiple findings of both kinds, preserving order") {
        val result = parser.parse(
            processResult(
                """
                BLOCKING: Missing null check
                RECOMMENDATION: Rename variable for clarity
                BLOCKING: Unbounded recursion
                """.trimIndent(),
            ),
        )

        result.blockingIssues shouldBe listOf("Missing null check", "Unbounded recursion")
        result.recommendations shouldBe listOf("Rename variable for clarity")
    }

    test("matches prefixes case-insensitively") {
        val result = parser.parse(processResult("blocking: lowercase prefix"))

        result.blockingIssues shouldBe listOf("lowercase prefix")
    }

    test("ignores lines that do not match either prefix") {
        val result = parser.parse(processResult("Just some narrative text\nBLOCKING: real issue"))

        result.blockingIssues shouldBe listOf("real issue")
    }

    test("returns an empty result when there is no output") {
        val result = parser.parse(processResult(""))

        result.blockingIssues shouldBe emptyList()
        result.recommendations shouldBe emptyList()
    }

    test("trims surrounding whitespace from the finding text") {
        val result = parser.parse(processResult("  BLOCKING:   extra spaces around the message   "))

        result.blockingIssues shouldBe listOf("extra spaces around the message")
    }

    test("ignores a BLOCKING line that is itself a no-issue confirmation") {
        val result = parser.parse(
            processResult("BLOCKING: none — the implementation satisfies the acceptance criteria"),
        )

        result.blockingIssues shouldBe emptyList()
    }

    test("recognizes common phrasings of \"no issue\" after the BLOCKING prefix") {
        listOf(
            "BLOCKING: none",
            "BLOCKING: None.",
            "BLOCKING: N/A",
            "BLOCKING: no issues found",
            "BLOCKING: nothing to report",
            "BLOCKING: - none",
            "BLOCKING: no blocking issues identified",
        ).forEach { line ->
            parser.parse(processResult(line)).blockingIssues shouldBe emptyList()
        }
    }

    test("still reports a real issue that happens to start with a similar word") {
        val result = parser.parse(processResult("BLOCKING: Nothing validates the input before parsing it"))

        result.blockingIssues shouldBe listOf("Nothing validates the input before parsing it")
    }

    test("still reports a real issue phrased as \"None of the...\"") {
        val result = parser.parse(processResult("BLOCKING: None of the tests cover the retry-exhausted path"))

        result.blockingIssues shouldBe listOf("None of the tests cover the retry-exhausted path")
    }
})
