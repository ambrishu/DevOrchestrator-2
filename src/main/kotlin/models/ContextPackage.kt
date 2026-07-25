package models

/**
 * The self-contained, minimal context assembled for implementing exactly one [Story].
 *
 * Fields follow the priority order documented in `docs/07-context-engine.md` §5: story and
 * acceptance criteria first, then documentation, then source code, then tests. `adrReferences`,
 * `codingStandards`, and `expectedDeliverables` are part of the documented contract but stay
 * empty until this repository has ADR files, a coding-standards document, and a defined
 * deliverable-extraction rule to populate them from.
 *
 * [failureAnalysis] is null for ordinary generation. The Repair Loop sets it to fold a prior
 * build failure into the same context shape, so [core.agent.AgentAdapter.generate] and
 * [core.agent.PromptBuilder] need no repair-specific variant.
 */
data class ContextPackage(
    val story: Story,
    val acceptanceCriteria: List<String>,
    val prdExcerpts: List<String> = emptyList(),
    val architectureRules: List<String> = emptyList(),
    val adrReferences: List<String> = emptyList(),
    val codingStandards: List<String> = emptyList(),
    val impactedSourceFiles: List<SourceFile> = emptyList(),
    val relatedTests: List<SourceFile> = emptyList(),
    val expectedDeliverables: List<String> = emptyList(),
    val failureAnalysis: FailureAnalysis? = null,
)
