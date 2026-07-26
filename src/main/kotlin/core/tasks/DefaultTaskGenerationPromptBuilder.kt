package core.tasks

/**
 * [TaskGenerationPromptBuilder] rendering a prompt whose output grammar matches
 * `core.planner.MarkdownStoryParser` exactly, so the generated backlog is guaranteed loadable by
 * the rest of ADO once validated.
 */
class DefaultTaskGenerationPromptBuilder : TaskGenerationPromptBuilder {

    override fun buildPrompt(documents: PlanningDocuments): String = buildString {
        appendLine("You are decomposing a project's planning documents into an ADO task backlog.")
        appendLine("This is a planning session only: do not write, edit, or execute anything.")
        appendLine("Produce the full contents of docs/TASKS.md and nothing else.")

        appendLine()
        appendLine("=== AI Engineering Spec (how to operate on this repository) ===")
        appendLine(documents.engineeringSpec)

        appendLine()
        appendLine("=== Product Requirements (what needs to be built) ===")
        appendLine(documents.productRequirements)

        appendLine()
        appendLine("=== System Architecture (how it should be structured) ===")
        appendLine(documents.systemArchitecture)

        appendLine()
        appendLine("Decompose the requirements into atomic implementation tasks. Each task must:")
        DECOMPOSITION_RULES.forEach { appendLine("- $it") }

        appendLine()
        appendLine("Emit ONLY the task list, using exactly this grammar for every task, in dependency order:")
        appendLine()
        appendLine("ID — Title")
        appendLine()
        appendLine("Status: todo")
        appendLine()
        appendLine("Depends On: None")
        appendLine()
        appendLine("Description")
        appendLine()
        appendLine("One or two sentences describing the change.")
        appendLine()
        appendLine("Acceptance Criteria")
        appendLine()
        appendLine("* First criterion")
        appendLine("* Second criterion")
        appendLine()
        appendLine("⸻")

        appendLine()
        appendLine("Formatting rules, all mandatory:")
        FORMAT_RULES.forEach { appendLine("- $it") }
    }.trimEnd()

    private companion object {
        val DECOMPOSITION_RULES = listOf(
            "implement exactly one focused change",
            "keep the repository buildable and testable on its own",
            "declare every dependency on another task explicitly",
            "avoid speculative or future-scope work not required by the documents",
        )

        val FORMAT_RULES = listOf(
            "every ID matches [A-Z]+-\\d+ (e.g. ADO-001), assigned sequentially, and is unique",
            "the header line is exactly \"ID — Title\" using an em dash (—), nothing else on that line",
            "Status is always \"todo\" — this is a fresh backlog, nothing has been executed yet",
            "Depends On lists comma-separated task IDs, a single \"ID through ID\" range, or \"None\"",
            "Acceptance Criteria bullets start with \"* \" (asterisk, single space)",
            "each task is separated from the next by a line containing only ⸻",
            "no prose, headings, commentary, or markdown code fences outside the task list itself",
        )
    }
}
