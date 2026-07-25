package cli

import com.github.ajalt.clikt.testing.test
import core.build.BuildExecutor
import core.common.exception.BuildExecutionException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import models.BuildResult
import models.BuildStatus
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class RunBuildCommandTest : FunSpec({

    lateinit var buildExecutor: BuildExecutor

    beforeTest {
        buildExecutor = mockk()
        startKoin { modules(module { single { buildExecutor } }) }
    }

    afterTest {
        stopKoin()
    }

    test("prints the result and exits 0 on a successful build") {
        every { buildExecutor.executeBuild(any<Path>()) } returns
            BuildResult(status = BuildStatus.SUCCESS, stdout = "built", stderr = "", durationMillis = 10)

        val result = RunBuildCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "Status:    SUCCESS"
    }

    test("prints the result and exits 1 on a failed build") {
        every { buildExecutor.executeBuild(any<Path>()) } returns
            BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = "compile error", durationMillis = 10)

        val result = RunBuildCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "Status:    FAILURE"
    }

    test("exits 1 and reports the error when the build cannot be started") {
        every { buildExecutor.executeBuild(any<Path>()) } throws BuildExecutionException("no such file")

        val result = RunBuildCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "no such file"
    }
})
