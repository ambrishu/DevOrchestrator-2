package core.validation

import core.build.BuildExecutor
import core.common.exception.BuildExecutionException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import models.BuildResult
import models.BuildStatus
import java.nio.file.Path

class BuildQualityGateTest : FunSpec({

    val repositoryPath = Path.of("/repo")

    test("name is \"build\"") {
        BuildQualityGate(mockk()).name shouldBe "build"
    }

    test("passes when the build succeeds") {
        val buildExecutor = mockk<BuildExecutor> {
            every { executeBuild(repositoryPath) } returns
                BuildResult(status = BuildStatus.SUCCESS, stdout = "built", stderr = "", durationMillis = 5)
        }

        val result = BuildQualityGate(buildExecutor).execute(repositoryPath)

        result.passed shouldBe true
        result.details shouldBe ""
    }

    test("fails with stderr details when the build fails") {
        val buildExecutor = mockk<BuildExecutor> {
            every { executeBuild(repositoryPath) } returns
                BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = "compile error", durationMillis = 5)
        }

        val result = BuildQualityGate(buildExecutor).execute(repositoryPath)

        result.passed shouldBe false
        result.details shouldBe "compile error"
    }

    test("fails with an explanatory message when the build cannot be started") {
        val buildExecutor = mockk<BuildExecutor> {
            every { executeBuild(repositoryPath) } throws BuildExecutionException("no such file")
        }

        val result = BuildQualityGate(buildExecutor).execute(repositoryPath)

        result.passed shouldBe false
        result.details shouldBe "no such file"
    }
})
