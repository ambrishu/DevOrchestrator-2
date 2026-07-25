package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** `ado build` — groups build execution subcommands. Contains no logic of its own. */
class BuildCommand : CliktCommand(name = "build", help = "Execute the project build") {

    init {
        subcommands(RunBuildCommand(), AnalyzeBuildCommand())
    }

    override fun run() = Unit
}
