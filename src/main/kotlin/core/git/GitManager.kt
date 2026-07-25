package core.git

import models.CommitResult
import models.RepositoryStatus
import java.nio.file.Path

/** Owns all git operations against a repository. Never pushes; local commits only. */
interface GitManager {

    /**
     * @throws core.common.exception.GitOperationException if git status cannot be read.
     */
    fun inspectStatus(repositoryPath: Path): RepositoryStatus

    /**
     * Stages every change and commits it with [message].
     *
     * An ordinary commit failure (nothing staged, a rejecting hook) is returned as a
     * [CommitResult] with `success = false`, not thrown.
     *
     * @throws core.common.exception.GitOperationException if staging fails or git cannot be started.
     */
    fun createCommit(repositoryPath: Path, message: String): CommitResult
}
