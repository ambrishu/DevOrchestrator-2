package core.progress

import core.common.exception.ProgressException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.ProgressState
import models.StoryStatus
import java.nio.file.Files

class FileProgressTrackerTest : FunSpec({

    val tracker = FileProgressTracker()

    test("loadProgress returns an empty state when the file does not exist") {
        val repo = Files.createTempDirectory("ado-progress-test")

        tracker.loadProgress(repo) shouldBe ProgressState()
    }

    test("loadProgress returns an empty state when the file is blank") {
        val repo = Files.createTempDirectory("ado-progress-test")
        val adoDir = Files.createDirectory(repo.resolve(".ado"))
        Files.writeString(adoDir.resolve("progress.yaml"), "")

        tracker.loadProgress(repo) shouldBe ProgressState()
    }

    test("loadProgress throws ProgressException for malformed YAML") {
        val repo = Files.createTempDirectory("ado-progress-test")
        val adoDir = Files.createDirectory(repo.resolve(".ado"))
        Files.writeString(adoDir.resolve("progress.yaml"), "stories: [this is not: valid")

        shouldThrow<ProgressException> { tracker.loadProgress(repo) }
    }

    test("saveProgress creates .ado/progress.yaml and it can be read back") {
        val repo = Files.createTempDirectory("ado-progress-test")
        val state = ProgressState().withStatus("ADO-001", StoryStatus.DONE)

        tracker.saveProgress(repo, state)

        Files.isRegularFile(repo.resolve(".ado").resolve("progress.yaml")) shouldBe true
        tracker.loadProgress(repo) shouldBe state
    }

    test("saveProgress overwrites a previously saved state") {
        val repo = Files.createTempDirectory("ado-progress-test")
        tracker.saveProgress(repo, ProgressState().withStatus("ADO-001", StoryStatus.TODO))

        val replacement = ProgressState().withStatus("ADO-002", StoryStatus.DONE)
        tracker.saveProgress(repo, replacement)

        tracker.loadProgress(repo) shouldBe replacement
    }

    test("updateStatus records a new story and persists it immediately") {
        val repo = Files.createTempDirectory("ado-progress-test")

        val result = tracker.updateStatus(repo, "ADO-001", StoryStatus.IN_PROGRESS)

        result.statusOf("ADO-001") shouldBe StoryStatus.IN_PROGRESS
        tracker.loadProgress(repo).statusOf("ADO-001") shouldBe StoryStatus.IN_PROGRESS
    }

    test("updateStatus preserves previously recorded stories") {
        val repo = Files.createTempDirectory("ado-progress-test")
        tracker.updateStatus(repo, "ADO-001", StoryStatus.DONE)

        val result = tracker.updateStatus(repo, "ADO-002", StoryStatus.TODO)

        result.statusOf("ADO-001") shouldBe StoryStatus.DONE
        result.statusOf("ADO-002") shouldBe StoryStatus.TODO
    }

    test("updateStatus overwrites an existing story's recorded status") {
        val repo = Files.createTempDirectory("ado-progress-test")
        tracker.updateStatus(repo, "ADO-001", StoryStatus.TODO)

        val result = tracker.updateStatus(repo, "ADO-001", StoryStatus.DONE)

        result.statusOf("ADO-001") shouldBe StoryStatus.DONE
    }
})
