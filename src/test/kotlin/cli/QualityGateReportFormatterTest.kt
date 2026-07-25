package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import models.QualityGateReport
import models.QualityGateResult

class QualityGateReportFormatterTest : FunSpec({

    test("shows PASS for a passing gate and an overall PASS") {
        val report = QualityGateReport(listOf(QualityGateResult("build", true)))

        val output = QualityGateReportFormatter.format(report)

        output shouldContain "build: PASS"
        output shouldContain "Overall: PASS"
    }

    test("shows FAIL with details for a failing gate and an overall FAIL") {
        val report = QualityGateReport(
            listOf(QualityGateResult("build", true), QualityGateResult("unitTest", false, "3 tests failed")),
        )

        val output = QualityGateReportFormatter.format(report)

        output shouldContain "unitTest: FAIL — 3 tests failed"
        output shouldContain "Overall: FAIL"
    }

    test("only shows the first line of multi-line details") {
        val report = QualityGateReport(listOf(QualityGateResult("build", false, "line one\nline two")))

        val output = QualityGateReportFormatter.format(report)

        output shouldContain "build: FAIL — line one"
        output shouldNotContain "line two"
    }
})
