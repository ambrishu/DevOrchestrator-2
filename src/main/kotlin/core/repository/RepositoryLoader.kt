package core.repository

import models.RepositoryReadiness
import java.nio.file.Path

/**
 * Validates a repository path as an ADO target.
 *
 * Validation is read-only: no implementation may create, modify, or delete
 * files in the inspected repository.
 */
interface RepositoryLoader {

    /** Inspects [path] and returns a readiness snapshot. Never throws for an invalid path. */
    fun validate(path: Path): RepositoryReadiness
}
