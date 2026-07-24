package utils

import models.ProcessResult
import java.nio.file.Path

/** Executes external processes and captures their result. */
interface ProcessExecutor {

    /**
     * Runs [command] to completion in [workingDirectory].
     *
     * @throws core.common.exception.ProcessExecutionException if the process cannot be started.
     */
    fun execute(command: List<String>, workingDirectory: Path): ProcessResult
}
