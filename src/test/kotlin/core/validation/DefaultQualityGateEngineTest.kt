package core.validation

import core.build.BuildExecutor
import core.configuration.ConfigurationLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import models.AdoConfiguration
import models.ArchitectureValidationConfig
import models.BuildResult
import models.BuildStatus
import models.FormattingConfig
import models.IntegrationTestConfig
import models.ProcessResult
import models.StaticAnalysisConfig
import models.TestConfig
import utils.ProcessExecutor
import java.nio.file.Path

class DefaultQualityGateEngineTest : FunSpec({

    val repositoryPath = Path.of("/repo")
    val configPath = repositoryPath.resolve(".ado").resolve("config.yaml")

    fun successfulBuild() = BuildResult(status = BuildStatus.SUCCESS, stdout = "built", stderr = "", durationMillis = 5)
    fun successfulProcess() = ProcessResult(exitCode = 0, stdout = "ok", stderr = "", durationMillis = 5)

    test("runs all six gates, in the documented order, using defaults when no config file exists") {
        val configurationLoader = mockk<ConfigurationLoader> { every { exists(configPath) } returns false }
        val buildExecutor = mockk<BuildExecutor> { every { executeBuild(repositoryPath) } returns successfulBuild() }
        val processExecutor = mockk<ProcessExecutor> { every { execute(any(), repositoryPath) } returns successfulProcess() }

        val report = DefaultQualityGateEngine(configurationLoader, buildExecutor, processExecutor).runQualityGates(repositoryPath)

        report.results.map { it.name } shouldBe listOf(
            "build", "formatting", "staticAnalysis", "unitTest", "integrationTest", "architectureValidation",
        )
        report.allPassed shouldBe true
    }

    test("formatting, static analysis, integration test, and architecture validation are skipped when unconfigured") {
        val configurationLoader = mockk<ConfigurationLoader> { every { exists(configPath) } returns false }
        val buildExecutor = mockk<BuildExecutor> { every { executeBuild(repositoryPath) } returns successfulBuild() }
        val processExecutor = mockk<ProcessExecutor> { every { execute(any(), repositoryPath) } returns successfulProcess() }

        val report = DefaultQualityGateEngine(configurationLoader, buildExecutor, processExecutor).runQualityGates(repositoryPath)

        val skipped = report.results.filter { it.name != "build" && it.name != "unitTest" }
        skipped.forEach { it.passed shouldBe true; it.details shouldBe "Gate not configured; skipped." }
    }

    test("unit tests always run using test.command, even with no configuration file") {
        val configurationLoader = mockk<ConfigurationLoader> { every { exists(configPath) } returns false }
        val buildExecutor = mockk<BuildExecutor> { every { executeBuild(repositoryPath) } returns successfulBuild() }
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(listOf("./gradlew", "test"), repositoryPath) } returns successfulProcess()
        every { processExecutor.execute(match { it != listOf("./gradlew", "test") }, repositoryPath) } returns successfulProcess()

        val report = DefaultQualityGateEngine(configurationLoader, buildExecutor, processExecutor).runQualityGates(repositoryPath)

        report.results.first { it.name == "unitTest" }.passed shouldBe true
    }

    test("runs configured commands for the four optional gates") {
        val configurationLoader = mockk<ConfigurationLoader>()
        every { configurationLoader.exists(configPath) } returns true
        every { configurationLoader.load(configPath) } returns AdoConfiguration(
            formatting = FormattingConfig("ktlint"),
            staticAnalysis = StaticAnalysisConfig("detekt"),
            test = TestConfig("./gradlew test"),
            integrationTest = IntegrationTestConfig("./gradlew integrationTest"),
            architectureValidation = ArchitectureValidationConfig("archunit-check"),
        )
        val buildExecutor = mockk<BuildExecutor> { every { executeBuild(repositoryPath) } returns successfulBuild() }
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(listOf("ktlint"), repositoryPath) } returns successfulProcess()
        every { processExecutor.execute(listOf("detekt"), repositoryPath) } returns successfulProcess()
        every { processExecutor.execute(listOf("./gradlew", "test"), repositoryPath) } returns successfulProcess()
        every { processExecutor.execute(listOf("./gradlew", "integrationTest"), repositoryPath) } returns successfulProcess()
        every { processExecutor.execute(listOf("archunit-check"), repositoryPath) } returns successfulProcess()

        val report = DefaultQualityGateEngine(configurationLoader, buildExecutor, processExecutor).runQualityGates(repositoryPath)

        report.allPassed shouldBe true
        report.results.none { it.details == "Gate not configured; skipped." } shouldBe true
    }

    test("a single failing gate makes the overall report fail without hiding the others") {
        val configurationLoader = mockk<ConfigurationLoader> { every { exists(configPath) } returns false }
        val buildExecutor = mockk<BuildExecutor> { every { executeBuild(repositoryPath) } returns successfulBuild() }
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(listOf("./gradlew", "test"), repositoryPath) } returns
            ProcessResult(exitCode = 1, stdout = "", stderr = "1 test failed", durationMillis = 5)
        every { processExecutor.execute(match { it != listOf("./gradlew", "test") }, repositoryPath) } returns successfulProcess()

        val report = DefaultQualityGateEngine(configurationLoader, buildExecutor, processExecutor).runQualityGates(repositoryPath)

        report.allPassed shouldBe false
        report.results.first { it.name == "unitTest" }.passed shouldBe false
        report.results.first { it.name == "build" }.passed shouldBe true
    }
})
