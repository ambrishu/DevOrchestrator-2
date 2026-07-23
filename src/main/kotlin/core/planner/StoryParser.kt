package core.planner

import models.Story

/** Parses ADO backlog markdown content into [Story] models. */
interface StoryParser {

    /**
     * Parses every story section in [content], in document order.
     *
     * @throws core.common.exception.StoryLoadException if a story section has an unsupported status.
     */
    fun parse(content: String): List<Story>
}
