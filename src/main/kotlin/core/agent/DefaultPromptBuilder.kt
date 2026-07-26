package core.agent

import models.ContextPackage

/**
 * [PromptBuilder] rendering the documented agent rules (`docs/TASKS.md` ADO-085) followed by
 * the context package in its documented priority order: story, acceptance criteria,
 * documentation, source, tests.
 */
class DefaultPromptBuilder : PromptBuilder {

    override fun buildPrompt(context: ContextPackage): String = buildString {
        appendLine("You are implementing exactly one story for the AI Development Orchestrator (ADO).")
        appendLine()
        appendLine("Rules:")
        AGENT_RULES.forEach { appendLine("- $it") }

        appendLine()
        appendLine("Story: ${context.story.id} — ${context.story.title}")
        if (context.story.description.isNotBlank()) {
            appendLine(context.story.description)
        }

        appendLine()
        appendLine("Acceptance Criteria:")
        if (context.acceptanceCriteria.isEmpty()) {
            appendLine("(none specified)")
        } else {
            context.acceptanceCriteria.forEach { appendLine("- $it") }
        }

        context.failureAnalysis?.let { analysis ->
            appendLine()
            appendLine("Previous Build Failure (${analysis.category}):")
            if (analysis.details.isEmpty()) {
                appendLine("(no specific detail lines captured)")
            } else {
                analysis.details.forEach { appendLine("- $it") }
            }
            appendLine("Fix only this failure. Do not make unrelated changes.")
        }

        context.reviewFeedback?.let { findings ->
            appendLine()
            appendLine("Previous Code Review Findings:")
            if (findings.isEmpty()) {
                appendLine("(no specific finding lines captured)")
            } else {
                findings.forEach { appendLine("- $it") }
            }
            appendLine("Fix only these findings. Do not make unrelated changes.")
        }

        if (context.prdExcerpts.isNotEmpty()) {
            appendLine()
            appendLine("Product Requirements:")
            context.prdExcerpts.forEach { appendLine(it) }
        }

        if (context.architectureRules.isNotEmpty()) {
            appendLine()
            appendLine("Architecture:")
            context.architectureRules.forEach { appendLine(it) }
        }

        if (context.impactedSourceFiles.isNotEmpty()) {
            appendLine()
            appendLine("Existing Source Files:")
            context.impactedSourceFiles.forEach { file ->
                appendLine("--- ${file.path} ---")
                appendLine(file.content)
            }
        }

        if (context.relatedTests.isNotEmpty()) {
            appendLine()
            appendLine("Related Tests:")
            context.relatedTests.forEach { file ->
                appendLine("--- ${file.path} ---")
                appendLine(file.content)
            }
        }
    }.trimEnd()

    private companion object {
        val AGENT_RULES = listOf(
            "Implement exactly one story.",
            "Do not implement future stories.",
            "Do not refactor unrelated code.",
            "Follow architecture documents.",
            "Write tests with production code.",
            "Keep changes focused.",
        )
    }
}
