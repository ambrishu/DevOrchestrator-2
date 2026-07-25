package core.git

import models.Story

/** Generates a focused commit message for a story's changes. */
interface CommitMessageFormatter {

    fun format(story: Story): String
}
