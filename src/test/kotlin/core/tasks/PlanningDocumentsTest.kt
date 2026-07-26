package core.tasks

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class PlanningDocumentsTest : FunSpec({

    test("produces the same hash for identical documents") {
        val a = PlanningDocuments("spec", "prd", "architecture")
        val b = PlanningDocuments("spec", "prd", "architecture")

        a.contentHash() shouldBe b.contentHash()
    }

    test("produces a different hash when any document changes") {
        val base = PlanningDocuments("spec", "prd", "architecture")

        base.contentHash() shouldNotBe PlanningDocuments("changed spec", "prd", "architecture").contentHash()
        base.contentHash() shouldNotBe PlanningDocuments("spec", "changed prd", "architecture").contentHash()
        base.contentHash() shouldNotBe PlanningDocuments("spec", "prd", "changed architecture").contentHash()
    }

    test("produces a 64-character lowercase hex digest") {
        val hash = PlanningDocuments("spec", "prd", "architecture").contentHash()

        hash.length shouldBe 64
        hash shouldBe hash.lowercase()
    }
})
