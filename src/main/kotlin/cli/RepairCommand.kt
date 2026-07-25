package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** `ado repair` — groups repair subcommands. Contains no logic of its own. */
class RepairCommand : CliktCommand(name = "repair", help = "Automatically repair a failing build") {

    init {
        subcommands(RunRepairCommand())
    }

    override fun run() = Unit
}
