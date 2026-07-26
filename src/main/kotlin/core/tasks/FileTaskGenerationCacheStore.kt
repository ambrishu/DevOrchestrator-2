package core.tasks

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import core.common.exception.TaskGenerationException
import models.TaskGenerationCache
import java.nio.file.Files
import java.nio.file.Path

/**
 * [TaskGenerationCacheStore] persisting to `.ado/task-generation-cache.yaml`.
 *
 * A cache that fails to read (missing, corrupt, hand-edited) is treated as absent rather than an
 * error — the caller falls back to a fresh generation, same as if this were the first run ever.
 */
class FileTaskGenerationCacheStore(
    private val mapper: ObjectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule(),
) : TaskGenerationCacheStore {

    override fun load(repositoryPath: Path): TaskGenerationCache? {
        val file = cacheFile(repositoryPath)
        if (!Files.isRegularFile(file)) return null

        return try {
            val content = Files.readString(file)
            if (content.isBlank()) null else mapper.readValue(content, TaskGenerationCache::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun save(repositoryPath: Path, cache: TaskGenerationCache) {
        val file = cacheFile(repositoryPath)
        try {
            Files.createDirectories(file.parent)
            Files.writeString(file, mapper.writeValueAsString(cache))
        } catch (e: Exception) {
            throw TaskGenerationException("Failed to write task generation cache: $file", e)
        }
    }

    private fun cacheFile(repositoryPath: Path): Path =
        repositoryPath.resolve(".ado").resolve("task-generation-cache.yaml")
}
