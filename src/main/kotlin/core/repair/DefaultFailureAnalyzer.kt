package core.repair

import models.BuildResult
import models.FailureAnalysis
import models.FailureCategory

/**
 * [FailureAnalyzer] using deterministic text-pattern matching over build output.
 *
 * Categories are checked in order from most to least specific, so e.g. a formatter's own
 * "error:"-shaped message is categorized as FORMATTING rather than the broader COMPILATION
 * pattern. The first category with a matching line wins; its `details` are exactly the lines
 * that matched. If nothing matches, the result is [FailureCategory.UNKNOWN] with the non-blank
 * stderr (or stdout, if stderr is empty) as details.
 */
class DefaultFailureAnalyzer : FailureAnalyzer {

    override fun analyze(buildResult: BuildResult): FailureAnalysis {
        val lines = (buildResult.stdout.lineSequence() + buildResult.stderr.lineSequence()).toList()

        for ((category, patterns) in CATEGORY_PATTERNS) {
            val matches = lines.filter { line -> patterns.any { pattern -> pattern.containsMatchIn(line) } }
            if (matches.isNotEmpty()) {
                return FailureAnalysis(category, matches.distinct())
            }
        }

        val fallback = buildResult.stderr.ifBlank { buildResult.stdout }
            .lineSequence()
            .filter { it.isNotBlank() }
            .toList()
        return FailureAnalysis(FailureCategory.UNKNOWN, fallback)
    }

    private companion object {
        val CATEGORY_PATTERNS: List<Pair<FailureCategory, List<Regex>>> = listOf(
            FailureCategory.FORMATTING to listOf(
                Regex("(?i)ktlint"),
                Regex("(?i)spotless"),
                Regex("(?i)prettier"),
                Regex("(?i)formatting violation"),
            ),
            FailureCategory.ARCHITECTURE to listOf(
                Regex("(?i)archunit"),
                Regex("(?i)architecture violation"),
                Regex("(?i)forbidden dependency"),
            ),
            FailureCategory.DEPENDENCY to listOf(
                Regex("(?i)could not resolve"),
                Regex("(?i)could not download"),
                Regex("(?i)unresolved dependency"),
                Regex("(?i)npm err! 404"),
            ),
            FailureCategory.TESTING to listOf(
                Regex("(?i):test FAILED"),
                Regex("(?i)there were failing tests"),
                Regex("(?i)assertionerror"),
                Regex("(?i)expected:.*but was:"),
            ),
            FailureCategory.COMPILATION to listOf(
                Regex("(?i)unresolved reference"),
                Regex("(?i)cannot find symbol"),
                Regex("(?i):compile\\w* FAILED"),
                Regex("(?i)\\berror:"),
            ),
        )
    }
}
