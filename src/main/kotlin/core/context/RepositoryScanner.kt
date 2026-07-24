package core.context

import java.nio.file.Path

/** Discovers files within a repository, relative to its root. */
interface RepositoryScanner {

    /**
     * Returns every regular file under [repositoryPath], as paths relative to it.
     *
     * Excludes version control, build output, and IDE/runtime metadata directories.
     *
     * @throws core.common.exception.ContextException if the repository cannot be read.
     */
    fun scan(repositoryPath: Path): List<Path>
}
