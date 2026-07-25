package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class QualityGateReportTest : FunSpec({

    test("allPassed is true when every gate passed") {
        val report = QualityGateReport(
            listOf(QualityGateResult("build", true), QualityGateResult("unitTest", true)),
        )

        report.allPassed shouldBe true
    }

    test("allPassed is false when any gate failed") {
        val report = QualityGateReport(
            listOf(QualityGateResult("build", true), QualityGateResult("unitTest", false)),
        )

        report.allPassed shouldBe false
    }

    test("allPassed is true for an empty report") {
        QualityGateReport(emptyList()).allPassed shouldBe true
    }
})
