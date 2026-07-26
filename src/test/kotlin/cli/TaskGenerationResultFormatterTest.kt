package cli

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import models.TaskGenerationResult

class TaskGenerationResultFormatterTest : FunSpec({

    test("includes the output path and story count") {
        val formatted = TaskGenerationResultFormatter.format(
            TaskGenerationResult(outputPath = "/repo/docs/TASKS.md", storyCount = 12),
        )

        formatted shouldContain "/repo/docs/TASKS.md"
        formatted shouldContain "12"
    }

    test("reports a freshly generated backlog") {
        val formatted = TaskGenerationResultFormatter.format(
            TaskGenerationResult(outputPath = "/repo/docs/TASKS.md", storyCount = 1, fromCache = false),
        )

        formatted shouldContain "freshly generated"
    }

    test("reports a cached backlog") {
        val formatted = TaskGenerationResultFormatter.format(
            TaskGenerationResult(outputPath = "/repo/docs/TASKS.md", storyCount = 1, fromCache = true),
        )

        formatted shouldContain "cached"
    }
})
