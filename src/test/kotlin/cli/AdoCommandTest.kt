package cli

import com.github.ajalt.clikt.testing.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class AdoCommandTest : FunSpec({

    test("--help lists the version and init subcommands") {
        val result = AdoCommand().test(listOf("--help"))

        result.statusCode shouldBe 0
        result.output shouldContain "version"
        result.output shouldContain "init"
        result.output shouldContain "config"
        result.output shouldContain "plan"
    }

    test("root command dispatches to the version subcommand") {
        val result = AdoCommand().test(listOf("version"))

        result.statusCode shouldBe 0
        result.output shouldContain "ado version"
    }

    test("root command with no arguments exits successfully without error") {
        val result = AdoCommand().test(emptyList())

        result.statusCode shouldBe 0
    }
})
