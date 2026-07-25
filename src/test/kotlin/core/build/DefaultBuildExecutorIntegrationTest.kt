package core.build

import core.common.exception.BuildExecutionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.BuildStatus
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

class DefaultBuildExecutorIntegrationTest : FunSpec({

    val executor = DefaultBuildExecutor()

    fun createExecutableScript(repo: java.nio.file.Path, name: String, script: String): java.nio.file.Path {
        val binDir = Files.createDirectories(repo.resolve("bin"))
        val file = binDir.resolve(name)
        Files.writeString(file, script)
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rwxr-xr-x"))
        return file
    }

    test("throws when the default build command has no gradlew to run in an empty repository") {
        val repo = Files.createTempDirectory("ado-build-executor-integration-test")

        shouldThrow<BuildExecutionException> { executor.executeBuild(repo) }
    }

    test("runs the configured build command and reports success with collected warnings") {
        val repo = Files.createTempDirectory("ado-build-executor-integration-test")
        val script = createExecutableScript(
            repo,
            "fake-build",
            "#!/bin/sh\necho Compiling\necho 'warning: unused variable x'\nexit 0\n",
        )
        val adoDir = Files.createDirectory(repo.resolve(".ado"))
        Files.writeString(adoDir.resolve("config.yaml"), "build:\n  command: $script\n")

        val result = executor.executeBuild(repo)

        result.status shouldBe BuildStatus.SUCCESS
        result.warnings shouldBe listOf("warning: unused variable x")
    }

    test("runs the configured build command and reports failure") {
        val repo = Files.createTempDirectory("ado-build-executor-integration-test")
        val script = createExecutableScript(
            repo,
            "fake-build-fail",
            "#!/bin/sh\necho 'error: something broke' 1>&2\nexit 1\n",
        )
        val adoDir = Files.createDirectory(repo.resolve(".ado"))
        Files.writeString(adoDir.resolve("config.yaml"), "build:\n  command: $script\n")

        val result = executor.executeBuild(repo)

        result.status shouldBe BuildStatus.FAILURE
        result.stderr shouldBe "error: something broke"
    }
})
