package models

/** An immutable unit of backlog work parsed from `docs/TASKS.md`. */
data class Story(
    val id: String,
    val title: String,
    val description: String,
    val status: StoryStatus,
    val dependencies: List<String> = emptyList(),
    val acceptanceCriteria: List<String> = emptyList(),
)
