package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** `ado git` — groups git subcommands. Contains no logic of its own. */
class GitCommand : CliktCommand(name = "git", help = "Inspect and commit repository changes") {

    init {
        subcommands(GitStatusCommand(), GitCommitCommand())
    }

    override fun run() = Unit
}
