package cli

import models.TaskGenerationResult

/** Renders a [TaskGenerationResult] as human-readable terminal output. */
object TaskGenerationResultFormatter {

    fun format(result: TaskGenerationResult): String = buildString {
        appendLine("Task Generation Result")
        appendLine("=======================")
        appendLine("Output: ${result.outputPath}")
        appendLine("Tasks Generated: ${result.storyCount}")
        val source = if (result.fromCache) {
            "cached (planning documents unchanged since the last generation)"
        } else {
            "freshly generated"
        }
        append("Source: $source")
    }
}
