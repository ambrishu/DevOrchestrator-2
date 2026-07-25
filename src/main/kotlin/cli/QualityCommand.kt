package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** `ado quality` — groups quality gate subcommands. Contains no logic of its own. */
class QualityCommand : CliktCommand(name = "quality", help = "Run quality gates") {

    init {
        subcommands(RunQualityCommand())
    }

    override fun run() = Unit
}
