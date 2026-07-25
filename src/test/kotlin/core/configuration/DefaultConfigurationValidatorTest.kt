package core.configuration

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.AdoConfiguration
import models.AgentConfig
import models.BuildConfig
import models.RepairConfig
import models.TestConfig

class DefaultConfigurationValidatorTest : FunSpec({

    val validator = DefaultConfigurationValidator()

    test("the default configuration is valid") {
        validator.validate(AdoConfiguration()).isValid shouldBe true
    }

    test("a blank agent provider is invalid") {
        val result = validator.validate(AdoConfiguration(agent = AgentConfig(provider = " ")))
        result.isValid shouldBe false
        result.errors shouldBe listOf("agent.provider must not be blank")
    }

    test("a blank build command is invalid") {
        val result = validator.validate(AdoConfiguration(build = BuildConfig(command = "")))
        result.isValid shouldBe false
        result.errors shouldBe listOf("build.command must not be blank")
    }

    test("a blank test command is invalid") {
        val result = validator.validate(AdoConfiguration(test = TestConfig(command = "")))
        result.isValid shouldBe false
        result.errors shouldBe listOf("test.command must not be blank")
    }

    test("negative repair retries is invalid") {
        val result = validator.validate(AdoConfiguration(repair = RepairConfig(retries = -1)))
        result.isValid shouldBe false
        result.errors shouldBe listOf("repair.retries must not be negative, got -1")
    }

    test("excessive repair retries is invalid") {
        val result = validator.validate(AdoConfiguration(repair = RepairConfig(retries = 21)))
        result.isValid shouldBe false
        result.errors shouldBe listOf("repair.retries must not exceed 20, got 21")
    }

    test("zero repair retries is valid") {
        validator.validate(AdoConfiguration(repair = RepairConfig(retries = 0))).isValid shouldBe true
    }

    test("blank formatting, static analysis, integration test, and architecture validation commands are valid") {
        validator.validate(AdoConfiguration()).isValid shouldBe true
    }

    test("multiple violations are all reported") {
        val result = validator.validate(
            AdoConfiguration(
                agent = AgentConfig(provider = ""),
                build = BuildConfig(command = ""),
            ),
        )

        result.errors shouldBe listOf(
            "agent.provider must not be blank",
            "build.command must not be blank",
        )
    }
})
