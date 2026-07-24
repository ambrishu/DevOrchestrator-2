package core.progress

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import core.common.exception.ProgressException
import io.github.oshai.kotlinlogging.KotlinLogging
import models.ProgressState
import models.StoryStatus
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/** [ProgressTracker] persisting state as YAML to `.ado/progress.yaml`. */
class FileProgressTracker(
    private val mapper: ObjectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule(),
) : ProgressTracker {

    override fun loadProgress(repositoryPath: Path): ProgressState {
        val progressFile = progressFile(repositoryPath)
        if (!Files.isRegularFile(progressFile)) {
            return ProgressState()
        }

        logger.debug { "Loading progress from $progressFile" }

        return try {
            val content = Files.readString(progressFile)
            if (content.isBlank()) ProgressState() else mapper.readValue(content, ProgressState::class.java)
        } catch (e: Exception) {
            throw ProgressException("Failed to parse progress file: $progressFile", e)
        }
    }

    override fun saveProgress(repositoryPath: Path, state: ProgressState) {
        val progressFile = progressFile(repositoryPath)

        logger.debug { "Saving progress to $progressFile" }

        try {
            Files.createDirectories(progressFile.parent)
            Files.writeString(progressFile, mapper.writeValueAsString(state))
        } catch (e: Exception) {
            throw ProgressException("Failed to write progress file: $progressFile", e)
        }
    }

    override fun updateStatus(repositoryPath: Path, storyId: String, status: StoryStatus): ProgressState {
        val updated = loadProgress(repositoryPath).withStatus(storyId, status)
        saveProgress(repositoryPath, updated)
        return updated
    }

    private fun progressFile(repositoryPath: Path): Path = repositoryPath.resolve(".ado").resolve("progress.yaml")
}
