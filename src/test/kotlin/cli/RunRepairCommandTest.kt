package cli

import com.github.ajalt.clikt.testing.test
import core.build.BuildExecutor
import core.common.exception.AgentInvocationException
import core.common.exception.BuildExecutionException
import core.common.exception.ContextException
import core.common.exception.StoryLoadException
import core.configuration.ConfigurationLoader
import core.context.ContextBuilder
import core.planner.StoryLoader
import core.planner.StoryPlanner
import core.repair.RepairLoop
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.AdoConfiguration
import models.BuildResult
import models.BuildStatus
import models.ContextPackage
import models.RepairConfig
import models.RepairResult
import models.Story
import models.StorySelection
import models.StoryStatus
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class RunRepairCommandTest : FunSpec({

    lateinit var storyLoader: StoryLoader
    lateinit var storyPlanner: StoryPlanner
    lateinit var contextBuilder: ContextBuilder
    lateinit var buildExecutor: BuildExecutor
    lateinit var repairLoop: RepairLoop
    lateinit var configurationLoader: ConfigurationLoader

    val story = Story(id = "ADO-001", title = "Example", description = "", status = StoryStatus.TODO)
    val context = ContextPackage(story, emptyList())

    beforeTest {
        storyLoader = mockk()
        storyPlanner = mockk()
        contextBuilder = mockk()
        buildExecutor = mockk()
        repairLoop = mockk()
        configurationLoader = mockk()
        startKoin {
            modules(
                module {
                    single { storyLoader }
                    single { storyPlanner }
                    single { contextBuilder }
                    single { buildExecutor }
                    single { repairLoop }
                    single { configurationLoader }
                },
            )
        }
        every { storyLoader.loadStories(any<Path>()) } returns listOf(story)
        every { contextBuilder.buildContext(story, any<Path>()) } returns context
        every { configurationLoader.exists(any<Path>()) } returns false
    }

    afterTest {
        stopKoin()
    }

    test("reports nothing to repair and exits 0 when the build already succeeds") {
        every { buildExecutor.executeBuild(any<Path>()) } returns
            BuildResult(status = BuildStatus.SUCCESS, stdout = "", stderr = "", durationMillis = 5)

        val result = RunRepairCommand().test(listOf("ADO-001", "--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "Build already succeeds; nothing to repair."
        verify(exactly = 0) { repairLoop.repair(any(), any(), any(), any()) }
    }

    test("uses the default retry limit when no configuration file exists") {
        val initialFailure = BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = "boom", durationMillis = 5)
        every { buildExecutor.executeBuild(any<Path>()) } returns initialFailure
        every { repairLoop.repair(context, any(), initialFailure, 5) } returns
            RepairResult(BuildResult(status = BuildStatus.SUCCESS, stdout = "", stderr = "", durationMillis = 5), attempts = 1)

        val result = RunRepairCommand().test(listOf("ADO-001", "--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "Outcome:  SUCCESS"
        verify { repairLoop.repair(context, any(), initialFailure, 5) }
    }

    test("uses the configured retry limit when a configuration file exists") {
        val initialFailure = BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = "boom", durationMillis = 5)
        every { configurationLoader.exists(any<Path>()) } returns true
        every { configurationLoader.load(any<Path>()) } returns AdoConfiguration(repair = RepairConfig(retries = 2))
        every { buildExecutor.executeBuild(any<Path>()) } returns initialFailure
        every { repairLoop.repair(context, any(), initialFailure, 2) } returns
            RepairResult(initialFailure, attempts = 2)

        val result = RunRepairCommand().test(listOf("ADO-001", "--path", "/repo"))

        result.statusCode shouldBe 1
        verify { repairLoop.repair(context, any(), initialFailure, 2) }
    }

    test("exits 1 when retries are exhausted") {
        val initialFailure = BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = "boom", durationMillis = 5)
        every { buildExecutor.executeBuild(any<Path>()) } returns initialFailure
        every { repairLoop.repair(context, any(), initialFailure, 5) } returns RepairResult(initialFailure, attempts = 5)

        val result = RunRepairCommand().test(listOf("ADO-001", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "RETRIES EXHAUSTED"
    }

    test("falls back to the next executable story when no ID is given") {
        every { storyPlanner.selectNext(listOf(story)) } returns StorySelection.Selected(story)
        every { buildExecutor.executeBuild(any<Path>()) } returns
            BuildResult(status = BuildStatus.SUCCESS, stdout = "", stderr = "", durationMillis = 5)

        val result = RunRepairCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "nothing to repair"
    }

    test("prints a no-op message and exits 0 when no story is executable and none is given") {
        every { storyPlanner.selectNext(listOf(story)) } returns StorySelection.None

        val result = RunRepairCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "No executable story found."
    }

    test("exits 1 when the given story ID does not exist") {
        val result = RunRepairCommand().test(listOf("ADO-999", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "Story not found: ADO-999"
    }

    test("exits 1 and reports the error when stories fail to load") {
        every { storyLoader.loadStories(any<Path>()) } throws StoryLoadException("tasks file not found")

        val result = RunRepairCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "tasks file not found"
    }

    test("exits 1 and reports the error when context building fails") {
        every { contextBuilder.buildContext(story, any<Path>()) } throws ContextException("repository unreadable")

        val result = RunRepairCommand().test(listOf("ADO-001", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "repository unreadable"
    }

    test("exits 1 and reports the error when the initial build cannot be started") {
        every { buildExecutor.executeBuild(any<Path>()) } throws BuildExecutionException("no such file")

        val result = RunRepairCommand().test(listOf("ADO-001", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "no such file"
    }

    test("exits 1 and reports the error when the agent invocation fails during repair") {
        val initialFailure = BuildResult(status = BuildStatus.FAILURE, stdout = "", stderr = "boom", durationMillis = 5)
        every { buildExecutor.executeBuild(any<Path>()) } returns initialFailure
        every { repairLoop.repair(context, any(), initialFailure, 5) } throws AgentInvocationException("claude not found")

        val result = RunRepairCommand().test(listOf("ADO-001", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "claude not found"
    }
})
