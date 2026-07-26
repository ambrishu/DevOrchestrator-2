package core.tasks

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain

class DefaultTaskGenerationPromptBuilderTest : FunSpec({

    val builder = DefaultTaskGenerationPromptBuilder()
    val documents = PlanningDocuments(
        engineeringSpec = "Spec content marker",
        productRequirements = "PRD content marker",
        systemArchitecture = "Architecture content marker",
    )

    test("embeds all three documents") {
        val prompt = builder.buildPrompt(documents)

        prompt shouldContain "Spec content marker"
        prompt shouldContain "PRD content marker"
        prompt shouldContain "Architecture content marker"
    }

    test("specifies the exact backlog header grammar") {
        val prompt = builder.buildPrompt(documents)

        prompt shouldContain "ID — Title"
        prompt shouldContain "[A-Z]+-\\d+"
    }

    test("requires todo status and explicit dependencies") {
        val prompt = builder.buildPrompt(documents)

        prompt shouldContain "Status: todo"
        prompt shouldContain "Depends On"
    }

    test("forbids commentary outside the task list") {
        val prompt = builder.buildPrompt(documents)

        prompt shouldContain "no prose, headings, commentary, or markdown code fences outside the task list"
    }
})
