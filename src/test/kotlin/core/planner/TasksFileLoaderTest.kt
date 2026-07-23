package core.planner

import core.common.exception.StoryLoadException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path

private const val DIVIDER = "⸻"

class TasksFileLoaderTest : FunSpec({

    val loader = TasksFileLoader()

    fun repoWithTasksFile(content: String): Path {
        val repo = Files.createTempDirectory("ado-planner-test")
        val docsDir = Files.createDirectory(repo.resolve("docs"))
        Files.writeString(docsDir.resolve("TASKS.md"), content)
        return repo
    }

    test("throws when docs/TASKS.md does not exist") {
        val repo = Files.createTempDirectory("ado-planner-test")
        shouldThrow<StoryLoadException> { loader.loadStories(repo) }
    }

    test("loads and parses every story in the file") {
        val content = """
            ADO-001 — First

            Status: done

            Depends On: None

            Description

            First story.

            $DIVIDER

            ADO-002 — Second

            Status: todo

            Depends On: ADO-001

            Description

            Second story.

            $DIVIDER
        """.trimIndent()

        val stories = loader.loadStories(repoWithTasksFile(content))

        stories.shouldHaveSize(2)
        stories.map { it.id } shouldBe listOf("ADO-001", "ADO-002")
    }

    test("throws when a story depends on an unknown story ID") {
        val content = """
            ADO-001 — Only Story

            Status: todo

            Depends On: ADO-999

            Description

            References a story that does not exist.

            $DIVIDER
        """.trimIndent()

        shouldThrow<StoryLoadException> { loader.loadStories(repoWithTasksFile(content)) }
    }

    test("parses the project's own docs/TASKS.md without error") {
        val repositoryRoot = Path.of("").toAbsolutePath()

        val stories = loader.loadStories(repositoryRoot)

        stories.shouldHaveSize(157)
        val ids = stories.map { it.id }
        ids.toSet().shouldHaveSize(ids.size)
    }
})
