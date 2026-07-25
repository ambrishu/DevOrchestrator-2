package core.repair

import models.BuildResult
import models.ContextPackage
import models.RepairResult
import java.nio.file.Path

/** Automatically repairs a failed implementation: analyze, invoke the agent, rebuild, repeat. */
interface RepairLoop {

    /**
     * Attempts to repair [context]'s story starting from [initialFailure], stopping when the
     * build succeeds or [maxRetries] attempts have been made.
     */
    fun repair(context: ContextPackage, repositoryPath: Path, initialFailure: BuildResult, maxRetries: Int): RepairResult
}
