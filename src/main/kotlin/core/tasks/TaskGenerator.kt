package core.tasks

import models.TaskGenerationResult
import java.nio.file.Path

/**
 * Generates `docs/TASKS.md` from a repository's planning documents.
 *
 * Vendor-neutral: the orchestrator never depends on a specific provider through this interface.
 */
interface TaskGenerator {

    /**
     * @param force Overwrites an existing `docs/TASKS.md` whose content differs from what would be
     * generated. Without it, an existing backlog with different content is left untouched and
     * [core.common.exception.TaskGenerationException] is thrown. Not needed when the content is
     * already up to date.
     * @param regenerate Bypasses the cache and re-invokes the agent even if the input documents
     * haven't changed since the last successful generation. Without it, unchanged documents
     * deterministically reproduce the last generated content instead of calling the agent again.
     * @throws core.common.exception.TaskGenerationException if a required planning document is missing,
     * the agent cannot be invoked, the generated backlog fails to parse, or an existing backlog
     * would be overwritten without [force].
     */
    fun generate(repositoryPath: Path, force: Boolean = false, regenerate: Boolean = false): TaskGenerationResult
}
