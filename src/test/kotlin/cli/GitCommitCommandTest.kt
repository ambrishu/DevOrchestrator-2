package cli

import com.github.ajalt.clikt.testing.test
import core.common.exception.GitOperationException
import core.common.exception.StoryLoadException
import core.git.CommitMessageFormatter
import core.git.GitManager
import core.planner.StoryLoader
import core.planner.StoryPlanner
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import models.CommitResult
import models.Story
import models.StorySelection
import models.StoryStatus
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Path

class GitCommitCommandTest : FunSpec({

    lateinit var storyLoader: StoryLoader
    lateinit var storyPlanner: StoryPlanner
    lateinit var commitMessageFormatter: CommitMessageFormatter
    lateinit var gitManager: GitManager

    val story = Story(id = "ADO-001", title = "Example", description = "", status = StoryStatus.TODO)

    beforeTest {
        storyLoader = mockk()
        storyPlanner = mockk()
        commitMessageFormatter = mockk()
        gitManager = mockk()
        startKoin {
            modules(
                module {
                    single { storyLoader }
                    single { storyPlanner }
                    single { commitMessageFormatter }
                    single { gitManager }
                },
            )
        }
        every { storyLoader.loadStories(any<Path>()) } returns listOf(story)
        every { commitMessageFormatter.format(story) } returns "feat(ADO-001): example"
    }

    afterTest {
        stopKoin()
    }

    test("commits with the generated message and exits 0 on success") {
        every { gitManager.createCommit(any<Path>(), "feat(ADO-001): example") } returns
            CommitResult(success = true, commitSha = "abc123")

        val result = GitCommitCommand().test(listOf("ADO-001", "--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "Committed abc123"
    }

    test("exits 1 when the commit fails") {
        every { gitManager.createCommit(any<Path>(), "feat(ADO-001): example") } returns
            CommitResult(success = false, failureReason = "nothing to commit")

        val result = GitCommitCommand().test(listOf("ADO-001", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "nothing to commit"
    }

    test("falls back to the next executable story when no ID is given") {
        every { storyPlanner.selectNext(listOf(story)) } returns StorySelection.Selected(story)
        every { gitManager.createCommit(any<Path>(), "feat(ADO-001): example") } returns
            CommitResult(success = true, commitSha = "abc123")

        val result = GitCommitCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "Committed abc123"
    }

    test("prints a no-op message and exits 0 when no story is executable and none is given") {
        every { storyPlanner.selectNext(listOf(story)) } returns StorySelection.None

        val result = GitCommitCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 0
        result.output shouldContain "No executable story found."
    }

    test("exits 1 when the given story ID does not exist") {
        val result = GitCommitCommand().test(listOf("ADO-999", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "Story not found: ADO-999"
    }

    test("exits 1 and reports the error when stories fail to load") {
        every { storyLoader.loadStories(any<Path>()) } throws StoryLoadException("tasks file not found")

        val result = GitCommitCommand().test(listOf("--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "tasks file not found"
    }

    test("exits 1 and reports the error when the git operation fails") {
        every { gitManager.createCommit(any<Path>(), "feat(ADO-001): example") } throws
            GitOperationException("git not found")

        val result = GitCommitCommand().test(listOf("ADO-001", "--path", "/repo"))

        result.statusCode shouldBe 1
        result.output shouldContain "git not found"
    }
})
