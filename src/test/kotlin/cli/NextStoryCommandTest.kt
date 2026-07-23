package cli

import com.github.ajalt.clikt.testing.test
import core.common.exception.StoryLoadException
import core.planner.StoryLoader
import core.planner.StoryPlanner
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import models.Story
import models.StorySelection
import models.StoryStatus
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class NextStoryCommandTest : FunSpec({

    lateinit var storyLoader: StoryLoader
    lateinit var storyPlanner: StoryPlanner

    beforeTest {
        storyLoader = mockk()
        storyPlanner = mockk()
        startKoin {
            modules(
                module {
                    single { storyLoader }
                    single { storyPlanner }
                },
            )
        }
    }

    afterTest {
        stopKoin()
    }

    test("prints the selected story and exits 0") {
        val story = Story(id = "ADO-002", title = "Second", description = "d", status = StoryStatus.TODO)
        every { storyLoader.loadStories(any<Path>()) } returns listOf(story)
        every { storyPlanner.selectNext(listOf(story)) } returns StorySelection.Selected(story)

        val result = NextStoryCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "ADO-002"
    }

    test("prints the no-op message and exits 0 when nothing is executable") {
        every { storyLoader.loadStories(any<Path>()) } returns emptyList()
        every { storyPlanner.selectNext(emptyList()) } returns StorySelection.None

        val result = NextStoryCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "No executable story found."
    }

    test("exits 1 and reports the error when stories fail to load") {
        every { storyLoader.loadStories(any<Path>()) } throws StoryLoadException("tasks file not found")

        val result = NextStoryCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "tasks file not found"
    }
})
