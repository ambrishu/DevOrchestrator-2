package cli

import com.github.ajalt.clikt.testing.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import utils.AdoMetadata

class VersionCommandTest : FunSpec({

    test("version command prints the ADO version") {
        val result = VersionCommand().test(emptyList())

        result.statusCode shouldBe 0
        result.output shouldContain AdoMetadata.VERSION
    }
})
