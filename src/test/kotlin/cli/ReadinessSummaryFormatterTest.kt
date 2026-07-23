package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import models.RepositoryReadiness

class ReadinessSummaryFormatterTest : FunSpec({

    test("format reports READY when the repository has no issues") {
        val readiness = RepositoryReadiness(
            repositoryPath = "/repo",
            exists = true,
            isDirectory = true,
            isGitRepository = true,
            isWritable = true,
            hasDocumentation = true,
            hasConfiguration = false,
            issues = emptyList(),
        )

        val output = ReadinessSummaryFormatter.format(readiness, "not found (.ado/config.yaml)")

        output shouldContain "Status: READY"
        output shouldContain "Git repository:  yes"
    }

    test("format reports NOT READY and lists issues") {
        val readiness = RepositoryReadiness(
            repositoryPath = "/repo",
            exists = true,
            isDirectory = true,
            isGitRepository = false,
            isWritable = true,
            hasDocumentation = false,
            hasConfiguration = false,
            issues = listOf("Not a git repository (no .git found): /repo"),
        )

        val output = ReadinessSummaryFormatter.format(readiness, "not found (.ado/config.yaml)")

        output shouldContain "Status: NOT READY"
        output shouldContain "Issues:"
        output shouldContain "Not a git repository (no .git found): /repo"
    }
})
