package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** `ado plan` — groups story planning subcommands. Contains no logic of its own. */
class PlanCommand : CliktCommand(name = "plan", help = "Inspect story planning") {

    init {
        subcommands(NextStoryCommand())
    }

    override fun run() = Unit
}
