package cli

import com.github.ajalt.clikt.testing.test
import core.common.exception.ContextException
import core.common.exception.StoryLoadException
import core.context.ContextBuilder
import core.planner.StoryLoader
import core.planner.StoryPlanner
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import models.ContextPackage
import models.Story
import models.StorySelection
import models.StoryStatus
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class ShowContextCommandTest : FunSpec({

    lateinit var storyLoader: StoryLoader
    lateinit var storyPlanner: StoryPlanner
    lateinit var contextBuilder: ContextBuilder

    val story = Story(id = "ADO-002", title = "Second", description = "", status = StoryStatus.TODO)

    beforeTest {
        storyLoader = mockk()
        storyPlanner = mockk()
        contextBuilder = mockk()
        startKoin {
            modules(
                module {
                    single { storyLoader }
                    single { storyPlanner }
                    single { contextBuilder }
                },
            )
        }
        every { storyLoader.loadStories(any<Path>()) } returns listOf(story)
    }

    afterTest {
        stopKoin()
    }

    test("builds context for an explicitly given story ID") {
        every { contextBuilder.buildContext(story, any<Path>()) } returns ContextPackage(story, listOf("AC"))

        val result = ShowContextCommand().test(listOf("ADO-002", "--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "ADO-002"
    }

    test("exits 1 when the given story ID does not exist") {
        val result = ShowContextCommand().test(listOf("ADO-999", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "Story not found: ADO-999"
    }

    test("falls back to the next executable story when no ID is given") {
        every { storyPlanner.selectNext(listOf(story)) } returns StorySelection.Selected(story)
        every { contextBuilder.buildContext(story, any<Path>()) } returns ContextPackage(story, emptyList())

        val result = ShowContextCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "ADO-002"
    }

    test("prints a no-op message and exits 0 when no story is executable and none is given") {
        every { storyPlanner.selectNext(listOf(story)) } returns StorySelection.None

        val result = ShowContextCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "No executable story found."
    }

    test("exits 1 and reports the error when stories fail to load") {
        every { storyLoader.loadStories(any<Path>()) } throws StoryLoadException("tasks file not found")

        val result = ShowContextCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "tasks file not found"
    }

    test("exits 1 and reports the error when context building fails") {
        every { contextBuilder.buildContext(story, any<Path>()) } throws ContextException("repository not readable")

        val result = ShowContextCommand().test(listOf("ADO-002", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "repository not readable"
    }
})
