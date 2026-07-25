package adapters.claude

import models.ProcessResult
import models.ReviewResult

/** Parses a `claude --print` review invocation's stdout into a [ReviewResult]. */
class ReviewResultParser {

    fun parse(result: ProcessResult): ReviewResult {
        val blockingIssues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        result.stdout.lineSequence().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith(BLOCKING_PREFIX, ignoreCase = true) ->
                    blockingIssues.add(trimmed.substring(BLOCKING_PREFIX.length).trim())

                trimmed.startsWith(RECOMMENDATION_PREFIX, ignoreCase = true) ->
                    recommendations.add(trimmed.substring(RECOMMENDATION_PREFIX.length).trim())
            }
        }

        return ReviewResult(blockingIssues = blockingIssues, recommendations = recommendations)
    }

    private companion object {
        const val BLOCKING_PREFIX = "BLOCKING:"
        const val RECOMMENDATION_PREFIX = "RECOMMENDATION:"
    }
}
