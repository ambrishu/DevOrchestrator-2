package core.context

import java.nio.file.Path

/** Loads documentation content from a repository. */
interface DocumentationLoader {

    /**
     * Returns the text content of [relativePath] under [repositoryPath], or null if it does not exist.
     *
     * @throws core.common.exception.ContextException if the file exists but cannot be read.
     */
    fun load(repositoryPath: Path, relativePath: String): String?
}
