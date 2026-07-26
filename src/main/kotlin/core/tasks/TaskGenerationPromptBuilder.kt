package core.tasks

/** Builds a vendor-neutral prompt that instructs an AI agent to produce an ADO backlog. */
interface TaskGenerationPromptBuilder {

    fun buildPrompt(documents: PlanningDocuments): String
}
