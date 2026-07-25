package core.review

import models.ContextPackage
import models.ReviewResult
import java.nio.file.Path

/**
 * Performs an independent AI review of a story's changes.
 *
 * Vendor-neutral: the orchestrator never depends on a specific provider through this interface.
 */
interface CodeReviewAgent {

    /**
     * @throws core.common.exception.ReviewFailureException if the review agent cannot be invoked or fails.
     */
    fun review(context: ContextPackage, repositoryPath: Path): ReviewResult
}
