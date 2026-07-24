package core.agent

import models.ContextPackage
import models.GenerationResult
import java.nio.file.Path

/**
 * Vendor-neutral contract for invoking an AI coding agent.
 *
 * The orchestrator never depends on a specific provider through this interface.
 */
interface AgentAdapter {

    /**
     * Invokes the agent to implement [context]'s story against the repository at [repositoryPath].
     *
     * @throws core.common.exception.AgentInvocationException if the agent cannot be invoked or fails.
     */
    fun generate(context: ContextPackage, repositoryPath: Path): GenerationResult
}
