package cli

import com.github.ajalt.clikt.testing.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PlanCommandTest : FunSpec({

    test("--help lists the next subcommand") {
        val result = PlanCommand().test(listOf("--help"))

        result.statusCode shouldBe 0
        result.output shouldContain "next"
    }
})
