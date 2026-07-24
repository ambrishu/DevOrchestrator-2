package models

/** Output of an [core.agent.AgentAdapter] invocation for one story. */
data class GenerationResult(
    val modifiedFiles: List<String> = emptyList(),
    val summary: String,
    val implementationNotes: String = "",
)
