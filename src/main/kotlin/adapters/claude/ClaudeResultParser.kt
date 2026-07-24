package adapters.claude

import models.GenerationResult
import models.ProcessResult

/**
 * Converts a successful `claude --print` invocation into a [GenerationResult].
 *
 * MVP scope: `claude --print` returns free text, not a structured file-change report, so
 * `modifiedFiles` is left empty here. Reliable modified-file detection belongs to the Git
 * Manager (diffing the working tree before/after invocation), which is out of scope for the
 * Claude Adapter.
 */
class ClaudeResultParser {

    fun parse(result: ProcessResult): GenerationResult = GenerationResult(
        modifiedFiles = emptyList(),
        summary = result.stdout,
        implementationNotes = result.stderr,
    )
}
