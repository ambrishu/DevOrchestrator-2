package core.validation

import models.QualityGateReport
import java.nio.file.Path

/** Runs every mandatory quality gate sequentially. Failed gates prevent successful completion. */
interface QualityGateEngine {

    /**
     * @throws core.common.exception.ConfigurationException if `.ado/config.yaml` exists but is invalid.
     */
    fun runQualityGates(repositoryPath: Path): QualityGateReport
}
