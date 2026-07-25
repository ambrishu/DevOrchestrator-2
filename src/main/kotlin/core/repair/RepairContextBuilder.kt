package core.repair

import models.ContextPackage
import models.FailureAnalysis

/** Folds a build failure into an existing [ContextPackage] for a repair attempt. */
interface RepairContextBuilder {

    /** Returns a copy of [original] carrying [failureAnalysis], with the current story still identified. */
    fun buildRepairContext(original: ContextPackage, failureAnalysis: FailureAnalysis): ContextPackage
}
