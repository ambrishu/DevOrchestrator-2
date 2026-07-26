package cli

import com.github.ajalt.clikt.testing.test
import core.common.exception.TaskGenerationException
import core.tasks.TaskGenerator
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.TaskGenerationResult
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class GenerateTasksCommandTest : FunSpec({

    lateinit var taskGenerator: TaskGenerator

    beforeTest {
        taskGenerator = mockk()
        startKoin {
            modules(module { single { taskGenerator } })
        }
    }

    afterTest {
        stopKoin()
    }

    test("generates the backlog and prints the result") {
        every { taskGenerator.generate(any<Path>(), any(), any()) } returns
            TaskGenerationResult(outputPath = "/repo/docs/TASKS.md", storyCount = 5)

        val result = GenerateTasksCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "/repo/docs/TASKS.md"
        result.output shouldContain "5"
    }

    test("defaults force and regenerate to false") {
        every { taskGenerator.generate(any<Path>(), any(), any()) } returns
            TaskGenerationResult(outputPath = "/repo/docs/TASKS.md", storyCount = 1)

        GenerateTasksCommand().test(listOf("--path", "/repo"))

        verify { taskGenerator.generate(any<Path>(), false, false) }
    }

    test("passes force through when given") {
        every { taskGenerator.generate(any<Path>(), any(), any()) } returns
            TaskGenerationResult(outputPath = "/repo/docs/TASKS.md", storyCount = 1)

        GenerateTasksCommand().test(listOf("--path", "/repo", "--force"))

        verify { taskGenerator.generate(any<Path>(), true, false) }
    }

    test("passes regenerate through when given") {
        every { taskGenerator.generate(any<Path>(), any(), any()) } returns
            TaskGenerationResult(outputPath = "/repo/docs/TASKS.md", storyCount = 1)

        GenerateTasksCommand().test(listOf("--path", "/repo", "--regenerate"))

        verify { taskGenerator.generate(any<Path>(), false, true) }
    }

    test("exits 1 and reports the error when generation fails") {
        every { taskGenerator.generate(any<Path>(), any(), any()) } throws
            TaskGenerationException("docs/TASKS.md already exists; pass --force to overwrite it")

        val result = GenerateTasksCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "already exists"
    }
})
