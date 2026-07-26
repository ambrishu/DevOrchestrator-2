package core.tasks

import models.TaskGenerationCache
import java.nio.file.Path

/** Persists the last successful task generation so unchanged input documents produce identical output. */
interface TaskGenerationCacheStore {

    /** Returns null if there is no cache yet, or it cannot be read — callers should treat that as a cache miss. */
    fun load(repositoryPath: Path): TaskGenerationCache?

    /** @throws core.common.exception.TaskGenerationException if the cache cannot be written. */
    fun save(repositoryPath: Path, cache: TaskGenerationCache)
}
