package cli

import com.github.ajalt.clikt.testing.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ContextCommandTest : FunSpec({

    test("--help lists the show subcommand") {
        val result = ContextCommand().test(listOf("--help"))

        result.statusCode shouldBe 0
        result.output shouldContain "show"
    }
})
