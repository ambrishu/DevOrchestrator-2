package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import models.ContextPackage
import models.SourceFile
import models.Story
import models.StoryStatus

class ContextPackageFormatterTest : FunSpec({

    val story = Story(id = "ADO-001", title = "Example", description = "", status = StoryStatus.TODO)

    test("shows the story header and section counts") {
        val context = ContextPackage(story = story, acceptanceCriteria = listOf("A", "B"))

        val output = ContextPackageFormatter.format(context)

        output shouldContain "ADO-001 — Example"
        output shouldContain "Acceptance Criteria:    2"
        output shouldContain "PRD included:           no"
        output shouldContain "Architecture included:  no"
        output shouldContain "Impacted Source Files (0):"
        output shouldContain "Related Tests (0):"
    }

    test("lists impacted source files and related tests by path") {
        val context = ContextPackage(
            story = story,
            acceptanceCriteria = emptyList(),
            prdExcerpts = listOf("prd"),
            impactedSourceFiles = listOf(SourceFile("src/main/kotlin/models/Story.kt", "content")),
            relatedTests = listOf(SourceFile("src/test/kotlin/models/StoryTest.kt", "content")),
        )

        val output = ContextPackageFormatter.format(context)

        output shouldContain "PRD included:           yes"
        output shouldContain "src/main/kotlin/models/Story.kt"
        output shouldContain "src/test/kotlin/models/StoryTest.kt"
    }
})
