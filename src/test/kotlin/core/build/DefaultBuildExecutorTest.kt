package core.build

import core.common.exception.BuildExecutionException
import core.common.exception.ProcessExecutionException
import core.configuration.ConfigurationLoader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.AdoConfiguration
import models.BuildConfig
import models.BuildStatus
import models.ProcessResult
import utils.ProcessExecutor
import java.nio.file.Path

class DefaultBuildExecutorTest : FunSpec({

    val repositoryPath = Path.of("/repo")
    val configPath = repositoryPath.resolve(".ado").resolve("config.yaml")

    test("uses the default build command when no configuration file exists") {
        val configurationLoader = mockk<ConfigurationLoader> { every { exists(configPath) } returns false }
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repositoryPath) } returns
            ProcessResult(exitCode = 0, stdout = "", stderr = "", durationMillis = 5)

        DefaultBuildExecutor(configurationLoader, processExecutor).executeBuild(repositoryPath)

        verify { processExecutor.execute(listOf("./gradlew", "build"), repositoryPath) }
    }

    test("uses the configured build command when a configuration file exists") {
        val configurationLoader = mockk<ConfigurationLoader>()
        every { configurationLoader.exists(configPath) } returns true
        every { configurationLoader.load(configPath) } returns AdoConfiguration(build = BuildConfig(command = "mvn package"))
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repositoryPath) } returns
            ProcessResult(exitCode = 0, stdout = "", stderr = "", durationMillis = 5)

        DefaultBuildExecutor(configurationLoader, processExecutor).executeBuild(repositoryPath)

        verify { processExecutor.execute(listOf("mvn", "package"), repositoryPath) }
    }

    test("maps a zero exit code to SUCCESS") {
        val configurationLoader = mockk<ConfigurationLoader> { every { exists(configPath) } returns false }
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repositoryPath) } returns
            ProcessResult(exitCode = 0, stdout = "built", stderr = "", durationMillis = 100)

        val result = DefaultBuildExecutor(configurationLoader, processExecutor).executeBuild(repositoryPath)

        result.status shouldBe BuildStatus.SUCCESS
        result.stdout shouldBe "built"
        result.durationMillis shouldBe 100
    }

    test("maps a non-zero exit code to FAILURE") {
        val configurationLoader = mockk<ConfigurationLoader> { every { exists(configPath) } returns false }
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repositoryPath) } returns
            ProcessResult(exitCode = 1, stdout = "", stderr = "compile error", durationMillis = 50)

        val result = DefaultBuildExecutor(configurationLoader, processExecutor).executeBuild(repositoryPath)

        result.status shouldBe BuildStatus.FAILURE
        result.stderr shouldBe "compile error"
    }

    test("collects lines mentioning \"warning\" from both stdout and stderr, case-insensitively") {
        val configurationLoader = mockk<ConfigurationLoader> { every { exists(configPath) } returns false }
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repositoryPath) } returns ProcessResult(
            exitCode = 0,
            stdout = "Compiling\nWARNING: unused import\nDone",
            stderr = "warning: deprecated API",
            durationMillis = 5,
        )

        val result = DefaultBuildExecutor(configurationLoader, processExecutor).executeBuild(repositoryPath)

        result.warnings shouldBe listOf("WARNING: unused import", "warning: deprecated API")
    }

    test("throws BuildExecutionException when the build command cannot be started") {
        val configurationLoader = mockk<ConfigurationLoader> { every { exists(configPath) } returns false }
        val processExecutor = mockk<ProcessExecutor>()
        every { processExecutor.execute(any(), repositoryPath) } throws ProcessExecutionException("no such file")

        shouldThrow<BuildExecutionException> {
            DefaultBuildExecutor(configurationLoader, processExecutor).executeBuild(repositoryPath)
        }
    }
})
