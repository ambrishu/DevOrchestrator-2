package core.build

import models.BuildResult
import java.nio.file.Path

/** Executes the project's configured build command. Never interprets the result. */
interface BuildExecutor {

    /**
     * Runs the build command configured in `.ado/config.yaml` (or the default) against
     * [repositoryPath] and returns its raw result.
     *
     * @throws core.common.exception.BuildExecutionException if the build command cannot be started.
     */
    fun executeBuild(repositoryPath: Path): BuildResult
}
