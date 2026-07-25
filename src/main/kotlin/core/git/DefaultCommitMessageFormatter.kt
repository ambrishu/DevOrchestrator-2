package core.git

import models.Story

/**
 * [CommitMessageFormatter] producing a Conventional Commits-style message: `type(scope):
 * description`. Every story implementation is a new feature from ADO's perspective, so type is
 * always `feat`; scope is the story ID, since the story is the unit of work being committed.
 */
class DefaultCommitMessageFormatter : CommitMessageFormatter {

    override fun format(story: Story): String = "feat(${story.id}): ${story.title.lowercase()}"
}
