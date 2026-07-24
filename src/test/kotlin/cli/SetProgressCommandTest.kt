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

class SetProgressCommandTest : FunSpec({

    lateinit var progressTracker: ProgressTracker

    beforeTest {
        progressTracker = mockk()
        startKoin { modules(module { single { progressTracker } }) }
    }

    afterTest {
        stopKoin()
    }

    test("updates the story status and exits 0") {
        every {
            progressTracker.updateStatus(any<Path>(), "ADO-001", StoryStatus.IN_PROGRESS)
        } returns ProgressState().withStatus("ADO-001", StoryStatus.IN_PROGRESS)

        val result = SetProgressCommand().test(listOf("ADO-001", "in_progress", "--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "Updated ADO-001 to in_progress"
    }

    test("exits 1 for an unsupported status without calling the tracker") {
        val result = SetProgressCommand().test(listOf("ADO-001", "bogus", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "Unsupported status"
    }

    test("exits 1 and reports the error when persistence fails") {
        every {
            progressTracker.updateStatus(any<Path>(), "ADO-001", StoryStatus.DONE)
        } throws ProgressException("disk full")

        val result = SetProgressCommand().test(listOf("ADO-001", "done", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "disk full"
    }
})
