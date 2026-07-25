package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/** `ado review` — groups AI code review subcommands. Contains no logic of its own. */
class ReviewCommand : CliktCommand(name = "review", help = "Perform an independent AI code review") {

    init {
        subcommands(RunReviewCommand())
    }

    override fun run() = Unit
}
