package core.configuration

import core.common.exception.ConfigurationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

class YamlConfigurationLoaderTest : FunSpec({

    val loader = YamlConfigurationLoader()

    test("exists returns false for a missing file") {
        val dir = Files.createTempDirectory("ado-config-test")
        loader.exists(dir.resolve("config.yaml")) shouldBe false
    }

    test("load throws ConfigurationException when the file is missing") {
        val dir = Files.createTempDirectory("ado-config-test")
        shouldThrow<ConfigurationException> {
            loader.load(dir.resolve("config.yaml"))
        }
    }

    test("load returns defaults for an empty file") {
        val dir = Files.createTempDirectory("ado-config-test")
        val file = dir.resolve("config.yaml")
        Files.writeString(file, "")

        loader.load(file) shouldBe models.AdoConfiguration()
    }

    test("load parses a fully specified configuration file") {
        val dir = Files.createTempDirectory("ado-config-test")
        val file = dir.resolve("config.yaml")
        Files.writeString(
            file,
            """
            agent:
              provider: claude-code
            build:
              command: ./gradlew build
            test:
              command: ./gradlew test
            review:
              enabled: false
            repair:
              retries: 3
            """.trimIndent(),
        )

        val config = loader.load(file)

        config.agent.provider shouldBe "claude-code"
        config.review.enabled shouldBe false
        config.repair.retries shouldBe 3
    }

    test("load fills in defaults for a partial configuration file") {
        val dir = Files.createTempDirectory("ado-config-test")
        val file = dir.resolve("config.yaml")
        Files.writeString(
            file,
            """
            repair:
              retries: 8
            """.trimIndent(),
        )

        val config = loader.load(file)

        config.repair.retries shouldBe 8
        config.agent.provider shouldBe "claude-code"
        config.build.command shouldBe "./gradlew build"
    }

    test("load throws ConfigurationException for malformed YAML") {
        val dir = Files.createTempDirectory("ado-config-test")
        val file = dir.resolve("config.yaml")
        Files.writeString(file, "agent: [this is not valid: yaml")

        shouldThrow<ConfigurationException> {
            loader.load(file)
        }
    }

    test("exists returns true once the file is present") {
        val dir = Files.createTempDirectory("ado-config-test")
        val file = dir.resolve("config.yaml")
        Files.writeString(file, "agent:\n  provider: codex\n")

        loader.exists(file) shouldBe true
    }
})
