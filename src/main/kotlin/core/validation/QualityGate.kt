package core.validation

import models.QualityGateResult
import java.nio.file.Path

/** A single mandatory quality check. Never throws; infrastructure failures become a failed result. */
interface QualityGate {

    val name: String

    fun execute(repositoryPath: Path): QualityGateResult
}
