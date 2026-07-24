package cli

import com.github.ajalt.clikt.testing.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ProgressCommandTest : FunSpec({

    test("--help lists the show and set subcommands") {
        val result = ProgressCommand().test(listOf("--help"))

        result.statusCode shouldBe 0
        result.output shouldContain "show"
        result.output shouldContain "set"
    }
})
