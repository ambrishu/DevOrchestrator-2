package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.ProgressException
import core.progress.ProgressTracker
import models.StoryStatus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/**
 * `ado progress set <story-id> <status>` — records a story's status in `.ado/progress.yaml`.
 *
 * This is the one write path in Milestone 4: unlike `show`, it exists specifically to persist
 * the update the user asked for.
 */
class SetProgressCommand :
    CliktCommand(name = "set", help = "Record a story's status in .ado/progress.yaml"),
    KoinComponent {

    private val progressTracker: ProgressTracker by inject()

    private val storyId: String by argument(name = "story-id", help = "Story ID, e.g. ADO-001")
    private val statusToken: String by argument(name = "status", help = "New status: todo, ready, in_progress, review, retrying, blocked, failed, passed, done")

    private val path: String by option("--path", help = "Repository path to write .ado/progress.yaml under (defaults to the current directory)")
        .default(".")

    override fun run() {
        val status = StoryStatus.fromToken(statusToken)
        if (status == null) {
            echo("Unsupported status: \"$statusToken\"", err = true)
            throw ProgramResult(1)
        }

        val repositoryPath = Paths.get(path).toAbsolutePath().normalize()

        try {
            progressTracker.updateStatus(repositoryPath, storyId, status)
        } catch (e: ProgressException) {
            echo(e.message ?: "Failed to update progress", err = true)
            throw ProgramResult(1)
        }

        echo("Updated $storyId to ${status.toToken()}")
    }
}
