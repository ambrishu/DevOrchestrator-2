package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** `ado agent` — groups AI agent invocation subcommands. Contains no logic of its own. */
class AgentCommand : CliktCommand(name = "agent", help = "Invoke the configured AI coding agent") {

    init {
        subcommands(GenerateCommand())
    }

    override fun run() = Unit
}
