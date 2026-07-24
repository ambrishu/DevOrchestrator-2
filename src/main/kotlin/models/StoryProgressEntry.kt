package models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** A single story's recorded status within a [ProgressState] snapshot. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class StoryProgressEntry(
    val status: StoryStatus,
)
