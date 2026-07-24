package core.context

import models.ContextPackage
import models.Story
import java.nio.file.Path

/** Assembles the minimal [ContextPackage] required to implement one story. Never invokes an AI agent. */
interface ContextBuilder {

    /**
     * Builds the context for [story] using the repository at [repositoryPath].
     *
     * @throws core.common.exception.ContextException if the repository cannot be scanned or read.
     */
    fun buildContext(story: Story, repositoryPath: Path): ContextPackage
}
