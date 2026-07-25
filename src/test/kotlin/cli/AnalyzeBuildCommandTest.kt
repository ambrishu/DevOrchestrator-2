package cli

import com.github.ajalt.clikt.testing.test
import core.build.BuildExecutor
import core.common.exception.BuildExecutionException
import core.repair.FailureAnalyzer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import models.BuildResult
import models.BuildStatus
import models.FailureAnalysis
import models.FailureCategory
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class AnalyzeBuildCommandTest : FunSpec({

    lateinit var buildExecutor: BuildExecutor
    lateinit var failureAnalyzer: FailureAnalyzer

    beforeTest {
        buildExecutor = mockk()
        failureAnalyzer = mockk()
        startKoin {
            modules(module { single { buildExecutor }; single { failureAnalyzer } })
        }
    }

    afterTest {
        stopKoin()
    }

    test("prints a no-op message and exits 0 when the build succeeds") {
        every { buildExecutor.executeBuild(any<Path>()) } returns
            BuildResult(status = BuildStatus.SUCCESS, stdout = "", stderr = "", durationMillis = 5)

        val result = AnalyzeBuildCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "Nothing to analyze."
    }

    test("analyzes and exits 1 when the build fails") {
        val buildResult = BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = "boom", durationMillis = 5)
        every { buildExecutor.executeBuild(any<Path>()) } returns buildResult
        every { failureAnalyzer.analyze(buildResult) } returns
            FailureAnalysis(FailureCategory.COMPILATION, listOf("boom"))

        val result = AnalyzeBuildCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "Category: COMPILATION"
        result.output shouldContain "boom"
    }

    test("exits 1 and reports the error when the build cannot be started") {
        every { buildExecutor.executeBuild(any<Path>()) } throws BuildExecutionException("no such file")

        val result = AnalyzeBuildCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "no such file"
    }
})
