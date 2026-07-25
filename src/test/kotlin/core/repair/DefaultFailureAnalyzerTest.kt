package core.repair

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.BuildResult
import models.BuildStatus
import models.FailureCategory

class DefaultFailureAnalyzerTest : FunSpec({

    val analyzer = DefaultFailureAnalyzer()

    fun buildResult(stdout: String = "", stderr: String = "") =
        BuildResult(status = BuildStatus.FAILURE, stdout = stdout, stderr = stderr, durationMillis = 5)

    test("detects a compilation failure via an unresolved reference") {
        val analysis = analyzer.analyze(buildResult(stdout = "e: Foo.kt: (10, 5): Unresolved reference: bar"))

        analysis.category shouldBe FailureCategory.COMPILATION
        analysis.details shouldBe listOf("e: Foo.kt: (10, 5): Unresolved reference: bar")
    }

    test("detects a compilation failure via a javac-style cannot find symbol") {
        analyzer.analyze(buildResult(stdout = "Foo.java:10: error: cannot find symbol")).category shouldBe
            FailureCategory.COMPILATION
    }

    test("detects a test failure") {
        val analysis = analyzer.analyze(buildResult(stdout = "> Task :test FAILED\nThere were failing tests."))

        analysis.category shouldBe FailureCategory.TESTING
        analysis.details shouldBe listOf("> Task :test FAILED", "There were failing tests.")
    }

    test("detects a dependency failure") {
        analyzer.analyze(
            buildResult(stderr = "Could not resolve com.example:library:1.0."),
        ).category shouldBe FailureCategory.DEPENDENCY
    }

    test("detects a formatting failure") {
        analyzer.analyze(buildResult(stdout = "ktlint found 3 formatting violations")).category shouldBe
            FailureCategory.FORMATTING
    }

    test("detects an architecture failure") {
        analyzer.analyze(buildResult(stdout = "ArchUnit: architecture violation detected")).category shouldBe
            FailureCategory.ARCHITECTURE
    }

    test("prioritizes formatting over the broader compilation error pattern") {
        val analysis = analyzer.analyze(buildResult(stdout = "ktlint: error: formatting violation on line 3"))

        analysis.category shouldBe FailureCategory.FORMATTING
    }

    test("falls back to UNKNOWN when no known signal is present") {
        val analysis = analyzer.analyze(buildResult(stdout = "Something odd happened."))

        analysis.category shouldBe FailureCategory.UNKNOWN
        analysis.details shouldBe listOf("Something odd happened.")
    }

    test("UNKNOWN prefers stderr over stdout when both are non-blank") {
        val analysis = analyzer.analyze(buildResult(stdout = "irrelevant stdout", stderr = "the real problem"))

        analysis.details shouldBe listOf("the real problem")
    }

    test("only the matching lines are included, not unrelated output") {
        val analysis = analyzer.analyze(
            buildResult(stdout = "Compiling sources...\ne: Foo.kt: Unresolved reference: bar\nDone."),
        )

        analysis.details shouldBe listOf("e: Foo.kt: Unresolved reference: bar")
    }

    test("duplicate matching lines are not repeated") {
        val analysis = analyzer.analyze(
            buildResult(stdout = "error: bad thing\nerror: bad thing"),
        )

        analysis.details shouldBe listOf("error: bad thing")
    }
})
