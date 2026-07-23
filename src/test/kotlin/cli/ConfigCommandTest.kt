package cli

import com.github.ajalt.clikt.testing.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ConfigCommandTest : FunSpec({

    test("--help lists the show and validate subcommands") {
        val result = ConfigCommand().test(listOf("--help"))

        result.statusCode shouldBe 0
        result.output shouldContain "show"
        result.output shouldContain "validate"
    }
})
