package core.review

import models.ContextPackage
import models.SourceFile

/** Builds a vendor-neutral code review prompt from a story's context and its changed files. */
interface ReviewPromptBuilder {

    fun buildPrompt(context: ContextPackage, changedFiles: List<SourceFile>): String
}
