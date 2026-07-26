package core.repair

import models.ContextPackage
import models.FailureAnalysis
import models.ReviewResult

/** Folds a prior failure — a build failure or a code review's blocking issues — into an existing [ContextPackage]. */
interface RepairContextBuilder {

    /** Returns a copy of [original] carrying [failureAnalysis], with the current story still identified. */
    fun buildRepairContext(original: ContextPackage, failureAnalysis: FailureAnalysis): ContextPackage

    /** Returns a copy of [original] carrying [reviewResult]'s blocking issues, for a review-repair attempt. */
    fun buildReviewRepairContext(original: ContextPackage, reviewResult: ReviewResult): ContextPackage
}
