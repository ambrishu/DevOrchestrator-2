package core.validation

import core.build.DefaultBuildExecutor
import core.configuration.YamlConfigurationLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import utils.DefaultProcessExecutor
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

class DefaultQualityGateEngineIntegrationTest : FunSpec({

    fun createExecutableScript(repo: java.nio.file.Path, name: String, script: String): java.nio.file.Path {
        val binDir = Files.createDirectories(repo.resolve("bin"))
        val file = binDir.resolve(name)
        Files.writeString(file, script)
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rwxr-xr-x"))
        return file
    }

    fun engine() = DefaultQualityGateEngine(
        configurationLoader = YamlConfigurationLoader(),
        buildExecutor = DefaultBuildExecutor(),
        processExecutor = DefaultProcessExecutor(),
    )

    test("passes overall when every configured command succeeds and unconfigured gates are skipped") {
        val repo = Files.createTempDirectory("ado-quality-gate-integration-test")
        val ok = createExecutableScript(repo, "ok", "#!/bin/sh\necho ok\nexit 0\n")
        val adoDir = Files.createDirectory(repo.resolve(".ado"))
        Files.writeString(
            adoDir.resolve("config.yaml"),
            "build:\n  command: $ok\ntest:\n  command: $ok\n",
        )

        val report = engine().runQualityGates(repo)

        report.allPassed shouldBe true
        report.results.first { it.name == "formatting" }.details shouldBe "Gate not configured; skipped."
    }

    test("fails overall when a configured gate command fails, while others still run") {
        val repo = Files.createTempDirectory("ado-quality-gate-integration-test")
        val ok = createExecutableScript(repo, "ok", "#!/bin/sh\necho ok\nexit 0\n")
        val fail = createExecutableScript(repo, "fail", "#!/bin/sh\necho 'violation' 1>&2\nexit 1\n")
        val adoDir = Files.createDirectory(repo.resolve(".ado"))
        Files.writeString(
            adoDir.resolve("config.yaml"),
            "build:\n  command: $ok\ntest:\n  command: $ok\nstaticAnalysis:\n  command: $fail\n",
        )

        val report = engine().runQualityGates(repo)

        report.allPassed shouldBe false
        report.results.first { it.name == "staticAnalysis" }.passed shouldBe false
        report.results.first { it.name == "build" }.passed shouldBe true
        report.results.first { it.name == "unitTest" }.passed shouldBe true
    }
})
