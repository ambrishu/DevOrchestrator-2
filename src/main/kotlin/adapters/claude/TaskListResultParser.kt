package adapters.claude

import models.ProcessResult

/**
 * Cleans a `claude --print` task-generation invocation's stdout into raw backlog markdown.
 *
 * The prompt asks for nothing but the task list, but agents sometimes wrap it in a markdown code
 * fence or prepend an introductory sentence anyway. Both are stripped here so the caller can hand
 * the result straight to `core.planner.MarkdownStoryParser` without tripping over commentary that
 * isn't part of the backlog grammar.
 */
class TaskListResultParser {

    fun parse(result: ProcessResult): String {
        var content = result.stdout.trim()
        content = stripCodeFence(content)
        content = stripLeadingPreamble(content)
        return content.trim()
    }

    private fun stripCodeFence(content: String): String {
        if (!content.startsWith("```")) return content

        val lines = content.lines().toMutableList()
        lines.removeAt(0)
        if (lines.isNotEmpty() && lines.last().trim() == "```") {
            lines.removeAt(lines.lastIndex)
        }
        return lines.joinToString("\n")
    }

    private fun stripLeadingPreamble(content: String): String {
        val firstHeader = HEADER_PATTERN.find(content) ?: return content
        return content.substring(firstHeader.range.first)
    }

    private companion object {
        val HEADER_PATTERN = Regex("(?m)^[A-Z]+-\\d+ — .+$")
    }
}
