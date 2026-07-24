package core.agent

import models.ContextPackage

/** Builds a vendor-neutral agent prompt from an assembled [ContextPackage]. */
interface PromptBuilder {

    fun buildPrompt(context: ContextPackage): String
}
