package cli

import com.github.ajalt.clikt.testing.test
import core.common.exception.ConfigurationException
import core.configuration.ConfigurationLoader
import core.repository.RepositoryLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import models.AdoConfiguration
import models.RepositoryReadiness
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class InitCommandTest : FunSpec({

    lateinit var repositoryLoader: RepositoryLoader
    lateinit var configurationLoader: ConfigurationLoader

    beforeTest {
        repositoryLoader = mockk()
        configurationLoader = mockk()
        startKoin {
            modules(
                module {
                    single { repositoryLoader }
                    single { configurationLoader }
                },
            )
        }
    }

    afterTest {
        stopKoin()
    }

    test("init prints a READY summary and exits 0 for a ready repository") {
        every { repositoryLoader.validate(any()) } returns RepositoryReadiness(
            repositoryPath = "/repo",
            exists = true,
            isDirectory = true,
            isGitRepository = true,
            isWritable = true,
            hasDocumentation = true,
            hasConfiguration = false,
            issues = emptyList(),
        )

        val result = InitCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "Status: READY"
    }

    test("init prints a NOT READY summary and exits 1 for an invalid repository") {
        every { repositoryLoader.validate(any()) } returns RepositoryReadiness(
            repositoryPath = "/repo",
            exists = true,
            isDirectory = true,
            isGitRepository = false,
            isWritable = true,
            hasDocumentation = false,
            hasConfiguration = false,
            issues = listOf("Not a git repository (no .git found): /repo"),
        )

        val result = InitCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "Status: NOT READY"
    }

    test("init reports a valid configuration when one is present and parses") {
        every { repositoryLoader.validate(any()) } returns RepositoryReadiness(
            repositoryPath = "/repo",
            exists = true,
            isDirectory = true,
            isGitRepository = true,
            isWritable = true,
            hasDocumentation = true,
            hasConfiguration = true,
            issues = emptyList(),
        )
        every { configurationLoader.load(any<Path>()) } returns AdoConfiguration()

        val result = InitCommand().test(listOf("--path", "/repo"))

        result.output shouldContain "found and valid"
    }

    test("init reports an invalid configuration when parsing fails") {
        every { repositoryLoader.validate(any()) } returns RepositoryReadiness(
            repositoryPath = "/repo",
            exists = true,
            isDirectory = true,
            isGitRepository = true,
            isWritable = true,
            hasDocumentation = true,
            hasConfiguration = true,
            issues = emptyList(),
        )
        every { configurationLoader.load(any<Path>()) } throws ConfigurationException("bad yaml")

        val result = InitCommand().test(listOf("--path", "/repo"))

        result.output shouldContain "found but invalid"
    }

    test("init never invokes the configuration loader when no configuration file exists") {
        every { repositoryLoader.validate(any()) } returns RepositoryReadiness(
            repositoryPath = "/repo",
            exists = true,
            isDirectory = true,
            isGitRepository = true,
            isWritable = true,
            hasDocumentation = true,
            hasConfiguration = false,
            issues = emptyList(),
        )

        val result = InitCommand().test(listOf("--path", "/repo"))

        result.output shouldContain "not found"
    }
})
