package core.context

import models.Story
import java.nio.file.Path

/** Selects the source files relevant to implementing a story. */
interface SourceSelector {

    /** Returns the subset of [sourceFiles] relevant to [story], in deterministic order. */
    fun select(story: Story, sourceFiles: List<Path>): List<Path>
}
