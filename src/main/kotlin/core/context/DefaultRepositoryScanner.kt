package core.context

import core.common.exception.ContextException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/** [RepositoryScanner] backed by a filesystem walk. */
class DefaultRepositoryScanner : RepositoryScanner {

    override fun scan(repositoryPath: Path): List<Path> {
        if (!Files.isDirectory(repositoryPath)) {
            throw ContextException("Repository path is not a directory: $repositoryPath")
        }

        return try {
            Files.walk(repositoryPath).use { paths ->
                paths
                    .filter { it.isRegularFile() }
                    .map { repositoryPath.relativize(it) }
                    .filter { relative -> relative.none { segment -> segment.toString() in EXCLUDED_DIRECTORIES } }
                    .toList()
                    .sortedBy { it.toString() }
            }
        } catch (e: Exception) {
            throw ContextException("Failed to scan repository: $repositoryPath", e)
        }
    }

    private companion object {
        val EXCLUDED_DIRECTORIES = setOf(".git", "build", ".gradle", ".idea", ".ado", "node_modules")
    }
}
