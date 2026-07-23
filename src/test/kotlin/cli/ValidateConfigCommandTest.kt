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
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class ValidateConfigCommandTest : FunSpec({

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

    test("validate exits 1 when no configuration file exists") {
        every { configurationLoader.exists(any<Path>()) } returns false

        val result = ValidateConfigCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "Configuration file not found"
    }

    test("validate exits 0 and confirms validity when the configuration file is valid") {
        every { configurationLoader.exists(any<Path>()) } returns true
        every { configurationLoader.load(any<Path>()) } returns AdoConfiguration()

        val result = ValidateConfigCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "Configuration is valid"
    }

    test("validate exits 1 and reports errors when the configuration file is invalid") {
        every { configurationLoader.exists(any<Path>()) } returns true
        every { configurationLoader.load(any<Path>()) } throws ConfigurationException("repair.retries must not be negative, got -1")

        val result = ValidateConfigCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "repair.retries must not be negative"
    }
})
