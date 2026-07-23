package core.planner

import core.common.exception.StoryLoadException
import models.Story
import models.StoryStatus

/**
 * [StoryParser] for the ADO backlog markdown format: a sequence of `ID — Title` sections,
 * each carrying `Status:`, `Depends On:`, `Description`, and `Acceptance Criteria` fields.
 */
class MarkdownStoryParser : StoryParser {

    override fun parse(content: String): List<Story> {
        val headers = HEADER_PATTERN.findAll(content).toList()

        return headers.mapIndexed { index, header ->
            val blockStart = header.range.last + 1
            val blockEnd = headers.getOrNull(index + 1)?.range?.first ?: content.length
            val block = DIVIDER_PATTERN.replace(content.substring(blockStart, blockEnd), "")

            parseStory(
                id = header.groupValues[1],
                title = header.groupValues[2].trim(),
                block = block,
            )
        }
    }

    private fun parseStory(id: String, title: String, block: String): Story {
        val statusToken = STATUS_PATTERN.find(block)?.groupValues?.get(1)?.trim()
        val status = statusToken?.let { StoryStatus.fromToken(it) }
            ?: throw StoryLoadException("Story $id has a missing or unsupported status: \"${statusToken ?: ""}\"")

        return Story(
            id = id,
            title = title,
            description = parseDescription(block),
            status = status,
            dependencies = parseDependencies(id, block),
            acceptanceCriteria = parseAcceptanceCriteria(block),
        )
    }

    private fun parseDescription(block: String): String {
        val start = DESCRIPTION_HEADER_PATTERN.find(block)?.range?.last?.plus(1) ?: return ""
        val end = ACCEPTANCE_CRITERIA_HEADER_PATTERN.find(block)?.range?.first ?: block.length
        if (end <= start) return ""
        return block.substring(start, end).trim()
    }

    private fun parseAcceptanceCriteria(block: String): List<String> {
        val start = ACCEPTANCE_CRITERIA_HEADER_PATTERN.find(block)?.range?.last?.plus(1) ?: return emptyList()
        return BULLET_PATTERN.findAll(block, start).map { it.groupValues[1].trim() }.toList()
    }

    private fun parseDependencies(storyId: String, block: String): List<String> {
        val raw = DEPENDS_ON_PATTERN.find(block)?.groupValues?.get(1)?.trim() ?: return emptyList()
        if (raw.equals("none", ignoreCase = true)) return emptyList()

        return raw.split(",").flatMap { token -> expandDependencyToken(storyId, token.trim()) }
    }

    private fun expandDependencyToken(storyId: String, token: String): List<String> {
        val range = RANGE_PATTERN.matchEntire(token)
        if (range != null) {
            val fromPrefix = range.groupValues[1]
            val fromDigits = range.groupValues[2]
            val toPrefix = range.groupValues[3]
            val toDigits = range.groupValues[4]

            if (fromPrefix != toPrefix) {
                throw StoryLoadException("Story $storyId has an invalid dependency range: \"$token\"")
            }

            val width = fromDigits.length
            return (fromDigits.toInt()..toDigits.toInt()).map { n -> "$fromPrefix-${n.toString().padStart(width, '0')}" }
        }

        if (!SINGLE_ID_PATTERN.matches(token)) {
            throw StoryLoadException("Story $storyId has an unparsable dependency: \"$token\"")
        }
        return listOf(token)
    }

    private companion object {
        val HEADER_PATTERN = Regex("(?m)^([A-Z]+-\\d+) — (.+)$")
        val STATUS_PATTERN = Regex("(?m)^Status:\\s*(.*)$")
        val DEPENDS_ON_PATTERN = Regex("(?m)^Depends On:\\s*(.+)$")
        val DESCRIPTION_HEADER_PATTERN = Regex("(?m)^Description\\s*$")
        val ACCEPTANCE_CRITERIA_HEADER_PATTERN = Regex("(?m)^Acceptance Criteria\\s*$")
        val BULLET_PATTERN = Regex("(?m)^\\* (.+)$")
        val DIVIDER_PATTERN = Regex("(?m)^⸻\\s*$")
        val RANGE_PATTERN = Regex("^([A-Za-z]+)-(\\d+) through ([A-Za-z]+)-(\\d+)$")
        val SINGLE_ID_PATTERN = Regex("^[A-Za-z]+-\\d+$")
    }
}
