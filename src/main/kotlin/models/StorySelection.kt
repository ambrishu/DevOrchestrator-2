package models

/** Outcome of asking the Story Planner for the next executable story. */
sealed class StorySelection {
    data class Selected(val story: Story) : StorySelection()
    data object None : StorySelection()
}
