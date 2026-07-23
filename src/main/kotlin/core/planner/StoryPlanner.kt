package core.planner

import models.Story
import models.StorySelection

/** Determines the next executable story. Only one story may be active at a time. */
interface StoryPlanner {

    /** Selects the first `todo` story, in document order, whose dependencies are all `done`. */
    fun selectNext(stories: List<Story>): StorySelection
}
