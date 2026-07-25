package cli

import com.github.ajalt.clikt.testing.test
import core.common.exception.StoryLoadException
import core.execution.ExecutionEngine
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import models.ExecutionSummary
import models.StoryExecutionResult
import models.StoryStatus
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class RunCommandTest : FunSpec({

    lateinit var executionEngine: ExecutionEngine

    beforeTest {
        executionEngine = mockk()
        startKoin { modules(module { single { executionEngine } }) }
    }

    afterTest {
        stopKoin()
    }

    test("prints the summary and exits 0 when every story succeeds") {
        every { executionEngine.run(any<Path>()) } returns ExecutionSummary(
            listOf(StoryExecutionResult("ADO-001", StoryStatus.REVIEW, "Implemented it.")),
        )

        val result = RunCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "ADO-001: review"
    }

    test("prints the no-op message and exits 0 when nothing is executable") {
        every { executionEngine.run(any<Path>()) } returns ExecutionSummary()

        val result = RunCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "Nothing to do."
    }

    test("exits 1 when a story ends up blocked") {
        every { executionEngine.run(any<Path>()) } returns ExecutionSummary(
            listOf(StoryExecutionResult("ADO-001", StoryStatus.BLOCKED, "claude not found")),
        )

        val result = RunCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "ADO-001: blocked"
    }

    test("exits 1 and reports the error when stories fail to load") {
        every { executionEngine.run(any<Path>()) } throws StoryLoadException("tasks file not found")

        val result = RunCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "tasks file not found"
    }
})
