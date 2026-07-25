package core.repair

import models.BuildResult
import models.FailureAnalysis

/** Interprets a failed [BuildResult] into a structured [FailureAnalysis]. Never modifies source code. */
interface FailureAnalyzer {

    fun analyze(buildResult: BuildResult): FailureAnalysis
}
