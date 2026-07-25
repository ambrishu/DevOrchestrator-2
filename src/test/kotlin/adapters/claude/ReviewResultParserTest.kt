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
})
