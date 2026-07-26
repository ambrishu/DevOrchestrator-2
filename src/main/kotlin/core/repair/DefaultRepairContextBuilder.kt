package core.repair

import models.ContextPackage
import models.FailureAnalysis
import models.ReviewResult

/** [RepairContextBuilder] that carries every original field forward, adding only the failure. */
class DefaultRepairContextBuilder : RepairContextBuilder {

    override fun buildRepairContext(original: ContextPackage, failureAnalysis: FailureAnalysis): ContextPackage =
        original.copy(failureAnalysis = failureAnalysis)

    override fun buildReviewRepairContext(original: ContextPackage, reviewResult: ReviewResult): ContextPackage =
        original.copy(reviewFeedback = reviewResult.blockingIssues)
}
