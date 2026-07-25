package core.execution

import models.ExecutionSummary
import java.nio.file.Path

/**
 * The central coordinator of ADO: runs stories to completion sequentially.
 *
 * Milestone scope: select → build context → invoke agent → record outcome. Build execution,
 * repair, quality gates, code review, and commit are later milestones and are not invoked here;
 * a successfully generated story is left in `review`, not `done`.
 */
interface ExecutionEngine {

    /**
     * Runs every executable story in the repository at [repositoryPath] until none remain or a
     * story fails.
     *
     * @throws core.common.exception.StoryLoadException if `docs/TASKS.md` cannot be loaded.
     */
    fun run(repositoryPath: Path): ExecutionSummary
}
