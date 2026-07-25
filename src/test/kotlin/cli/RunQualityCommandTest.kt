package cli

import com.github.ajalt.clikt.testing.test
import core.common.exception.ConfigurationException
import core.validation.QualityGateEngine
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import models.QualityGateReport
import models.QualityGateResult
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class RunQualityCommandTest : FunSpec({

    lateinit var qualityGateEngine: QualityGateEngine

    beforeTest {
        qualityGateEngine = mockk()
        startKoin { modules(module { single { qualityGateEngine } }) }
    }

    afterTest {
        stopKoin()
    }

    test("prints the report and exits 0 when every gate passes") {
        every { qualityGateEngine.runQualityGates(any<Path>()) } returns
            QualityGateReport(listOf(QualityGateResult("build", true)))

        val result = RunQualityCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "Overall: PASS"
    }

    test("prints the report and exits 1 when a gate fails") {
        every { qualityGateEngine.runQualityGates(any<Path>()) } returns
            QualityGateReport(listOf(QualityGateResult("unitTest", false, "boom")))

        val result = RunQualityCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "Overall: FAIL"
    }

    test("exits 1 and reports the error when configuration is invalid") {
        every { qualityGateEngine.runQualityGates(any<Path>()) } throws ConfigurationException("bad yaml")

        val result = RunQualityCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "bad yaml"
    }
})
