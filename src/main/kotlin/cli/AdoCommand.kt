package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** Root `ado` command. Contains no orchestration logic; dispatches to subcommands. */
class AdoCommand : CliktCommand(name = "ado", help = "AI Development Orchestrator") {

    init {
        subcommands(
            VersionCommand(),
            InitCommand(),
            ConfigCommand(),
            PlanCommand(),
            ProgressCommand(),
            ContextCommand(),
            AgentCommand(),
            BuildCommand(),
            RepairCommand(),
            QualityCommand(),
            RunCommand(),
        )
    }

    override fun run() = Unit
}
