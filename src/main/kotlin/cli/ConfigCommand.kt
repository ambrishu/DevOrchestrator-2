package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** `ado config` — groups configuration inspection subcommands. Contains no logic of its own. */
class ConfigCommand : CliktCommand(name = "config", help = "Inspect and validate ADO configuration") {

    init {
        subcommands(ShowConfigCommand(), ValidateConfigCommand())
    }

    override fun run() = Unit
}
