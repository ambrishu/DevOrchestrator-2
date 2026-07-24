package cli

import com.github.ajalt.clikt.testing.test
import core.common.exception.ProgressException
import core.progress.ProgressTracker
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import models.ProgressState
import models.StoryStatus
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class ShowProgressCommandTest : FunSpec({

    lateinit var progressTracker: ProgressTracker

    beforeTest {
        progressTracker = mockk()
        startKoin { modules(module { single { progressTracker } }) }
    }

    afterTest {
        stopKoin()
    }

    test("prints the no-op message when nothing is recorded") {
        every { progressTracker.loadProgress(any<Path>()) } returns ProgressState()

        val result = ShowProgressCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "No progress recorded yet."
    }

    test("prints recorded story statuses") {
        every { progressTracker.loadProgress(any<Path>()) } returns ProgressState().withStatus("ADO-001", StoryStatus.DONE)

        val result = ShowProgressCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "ADO-001: done"
    }

    test("exits 1 and reports the error when progress fails to load") {
        every { progressTracker.loadProgress(any<Path>()) } throws ProgressException("bad progress file")

        val result = ShowProgressCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "bad progress file"
    }
})
