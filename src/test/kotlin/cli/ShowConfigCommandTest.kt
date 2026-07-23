package cli

import com.github.ajalt.clikt.testing.test
import core.common.exception.ConfigurationException
import core.configuration.ConfigurationLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import models.AdoConfiguration
import models.AgentConfig
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class ShowConfigCommandTest : FunSpec({

    lateinit var configurationLoader: ConfigurationLoader

    beforeTest {
        configurationLoader = mockk()
        startKoin {
            modules(module { single { configurationLoader } })
        }
    }

    afterTest {
        stopKoin()
    }

    test("show prints defaults when no configuration file exists") {
        every { configurationLoader.exists(any<Path>()) } returns false

        val result = ShowConfigCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "defaults (no .ado/config.yaml found)"
        result.output shouldContain "claude-code"
    }

    test("show prints the loaded configuration when a file exists") {
        every { configurationLoader.exists(any<Path>()) } returns true
        every { configurationLoader.load(any<Path>()) } returns AdoConfiguration(agent = AgentConfig(provider = "codex"))

        val result = ShowConfigCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain ".ado/config.yaml"
        result.output shouldContain "codex"
    }

    test("show reports an error and exits 1 when the configuration file is invalid") {
        every { configurationLoader.exists(any<Path>()) } returns true
        every { configurationLoader.load(any<Path>()) } throws ConfigurationException("bad yaml")

        val result = ShowConfigCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "bad yaml"
    }
})
