package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import core.common.exception.TaskGenerationException
import core.tasks.TaskGenerator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.nio.file.Paths

/**
 * `ado tasks generate` — generates `docs/TASKS.md` from a repository's planning documents
 * (`docs/AI_ENGINEERING_SPEC.md`, `docs/02-product-requirements.md`, `docs/03-system-architecture.md`).
 *
 * Requires the `claude` binary on `PATH`.
 */
class GenerateTasksCommand :
    CliktCommand(name = "generate", help = "Generate docs/TASKS.md from the repository's planning documents"),
    KoinComponent {

    private val taskGenerator: TaskGenerator by inject()

    private val path: String by option("--path", help = "Repository path to operate on (defaults to the current directory)")
        .default(".")

    private val force: Boolean by option("--force", help = "Overwrite an existing docs/TASKS.md with different content")
        .flag(default = false)

    private val regenerate: Boolean by option(
        "--regenerate",
        help = "Bypass the cache and re-invoke the agent even if the planning documents haven't changed",
    ).flag(default = false)

    override fun run() {
        val repositoryPath = Paths.get(path).toAbsolutePath().normalize()

        val result = try {
            taskGenerator.generate(repositoryPath, force, regenerate)
        } catch (e: TaskGenerationException) {
            echo(e.message ?: "Failed to generate the task backlog", err = true)
            throw ProgramResult(1)
        }

        echo(TaskGenerationResultFormatter.format(result))
    }
}
