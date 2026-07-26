package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** `ado tasks` — groups task backlog generation subcommands. Contains no logic of its own. */
class TasksCommand : CliktCommand(name = "tasks", help = "Generate the ADO task backlog") {

    init {
        subcommands(GenerateTasksCommand())
    }

    override fun run() = Unit
}
