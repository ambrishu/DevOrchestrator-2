package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** `ado progress` — groups progress inspection and update subcommands. Contains no logic of its own. */
class ProgressCommand : CliktCommand(name = "progress", help = "Inspect and update ADO progress state") {

    init {
        subcommands(ShowProgressCommand(), SetProgressCommand())
    }

    override fun run() = Unit
}
