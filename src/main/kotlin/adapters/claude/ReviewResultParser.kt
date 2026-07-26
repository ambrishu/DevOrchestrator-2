package adapters.claude

import models.ProcessResult
import models.ReviewResult

/**
 * Parses a `claude --print` review invocation's stdout into a [ReviewResult].
 *
 * The prompt instructs the agent to omit a `BLOCKING:` line entirely when there is nothing to
 * report, but that instruction isn't always followed — the agent sometimes writes something
 * like `BLOCKING: none, the implementation is correct` to explicitly confirm there's no issue.
 * Treating that as a real blocking issue would wrongly block a story that the review actually
 * passed, so lines whose content is itself a "no issue" statement are ignored.
 */
class ReviewResultParser {

    fun parse(result: ProcessResult): ReviewResult {
        val blockingIssues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        result.stdout.lineSequence().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith(BLOCKING_PREFIX, ignoreCase = true) -> {
                    val detail = trimmed.substring(BLOCKING_PREFIX.length).trim()
                    if (!isNoIssueMarker(detail)) blockingIssues.add(detail)
                }

                trimmed.startsWith(RECOMMENDATION_PREFIX, ignoreCase = true) ->
                    recommendations.add(trimmed.substring(RECOMMENDATION_PREFIX.length).trim())
            }
        }

        return ReviewResult(blockingIssues = blockingIssues, recommendations = recommendations)
    }

    /**
     * True only when the detail *is* a no-issue confirmation, not merely starts with a word that
     * could also open a real finding (e.g. "Nothing validates the input" is a real issue; a loose
     * prefix check on "nothing" would wrongly swallow it). A phrase counts only if it exactly
     * matches a known no-issue phrase, either as the whole detail or as the text up to the first
     * separator (dash/colon) — i.e. "none" and "none — see above" both match, but "none of the
     * tests cover X" does not, because "none of the tests cover x" isn't itself a listed phrase.
     */
    private fun isNoIssueMarker(detail: String): Boolean {
        val cleaned = detail.trimStart('-', '—', '–', ':', ' ').trim().trimEnd('.', '!').lowercase()
        if (cleaned in NO_ISSUE_PHRASES) return true

        val firstSegment = cleaned.split(SEPARATOR).firstOrNull()?.trim()
        return firstSegment != null && firstSegment in NO_ISSUE_PHRASES
    }

    private companion object {
        const val BLOCKING_PREFIX = "BLOCKING:"
        const val RECOMMENDATION_PREFIX = "RECOMMENDATION:"
        val SEPARATOR = Regex("""\s*[—–]\s*|\s+-\s+|:\s+""")
        val NO_ISSUE_PHRASES = setOf(
            "none", "n/a", "na", "nothing",
            "no issue", "no issues", "no issues found",
            "nothing to report", "no blocking issues",
            "no blocking issues found", "no blocking issues identified",
        )
    }
}
