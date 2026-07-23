package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import models.AdoConfiguration
import models.RepairConfig

class ConfigurationSummaryFormatterTest : FunSpec({

    test("format includes the source and every configuration section") {
        val output = ConfigurationSummaryFormatter.format(
            AdoConfiguration(repair = RepairConfig(retries = 7)),
            "defaults (no .ado/config.yaml found)",
        )

        output shouldContain "Source:          defaults (no .ado/config.yaml found)"
        output shouldContain "agent.provider:  claude-code"
        output shouldContain "build.command:   ./gradlew build"
        output shouldContain "test.command:    ./gradlew test"
        output shouldContain "review.enabled:  true"
        output shouldContain "repair.retries:  7"
    }
})
