package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** `ado context` — groups context assembly subcommands. Contains no logic of its own. */
class ContextCommand : CliktCommand(name = "context", help = "Inspect assembled story context") {

    init {
        subcommands(ShowContextCommand())
    }

    override fun run() = Unit
}
