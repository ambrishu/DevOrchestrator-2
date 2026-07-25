package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AdoConfigurationTest : FunSpec({

    test("default configuration matches the documented schema defaults") {
        val config = AdoConfiguration()

        config.agent.provider shouldBe "claude-code"
        config.build.command shouldBe "./gradlew build"
        config.test.command shouldBe "./gradlew test"
        config.review.enabled shouldBe true
        config.repair.retries shouldBe 5
        config.formatting.command shouldBe ""
        config.staticAnalysis.command shouldBe ""
        config.integrationTest.command shouldBe ""
        config.architectureValidation.command shouldBe ""
    }

    test("configuration is a data class supporting structural equality") {
        AdoConfiguration() shouldBe AdoConfiguration()
    }

    test("sections can be overridden independently") {
        val config = AdoConfiguration(agent = AgentConfig(provider = "codex"))

        config.agent.provider shouldBe "codex"
        config.build.command shouldBe "./gradlew build"
    }
})
