package cli

import models.StorySelection

/** Renders a [StorySelection] as human-readable terminal output. */
object StorySelectionFormatter {

    fun format(selection: StorySelection): String = when (selection) {
        is StorySelection.Selected -> buildString {
            val story = selection.story
            appendLine("Next Story:  ${story.id} — ${story.title}")
            appendLine("Status:      ${story.status}")
            appendLine("Depends On:  ${if (story.dependencies.isEmpty()) "none" else story.dependencies.joinToString(", ")}")
            append("Description: ${story.description.ifBlank { "(none)" }}")
        }

        StorySelection.None -> "No executable story found."
    }
}
