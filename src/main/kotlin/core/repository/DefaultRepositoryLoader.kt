package core.repository

import core.common.exception.RepositoryException
import io.github.oshai.kotlinlogging.KotlinLogging
import models.RepositoryReadiness
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isWritable

private val logger = KotlinLogging.logger {}

/** [RepositoryLoader] backed by direct filesystem inspection. */
class DefaultRepositoryLoader : RepositoryLoader {

    override fun validate(path: Path): RepositoryReadiness {
        logger.debug { "Validating repository at $path" }

        val exists = try {
            path.exists()
        } catch (e: SecurityException) {
            throw RepositoryException("Unable to access repository path: $path", e)
        }

        val isDirectory = exists && path.isDirectory()
        val isGitRepository = isDirectory && Files.exists(path.resolve(".git"))
        val isWritable = isDirectory && path.isWritable()
        val hasDocumentation = isDirectory && Files.isDirectory(path.resolve("docs"))
        val hasConfiguration = isDirectory && Files.isRegularFile(path.resolve(".ado").resolve("config.yaml"))

        val issues = buildList {
            if (!exists) add("Repository path does not exist: $path")
            if (exists && !isDirectory) add("Repository path is not a directory: $path")
            if (isDirectory && !isGitRepository) add("Not a git repository (no .git found): $path")
            if (isDirectory && !isWritable) add("Repository path is not writable: $path")
        }

        return RepositoryReadiness(
            repositoryPath = path.toString(),
            exists = exists,
            isDirectory = isDirectory,
            isGitRepository = isGitRepository,
            isWritable = isWritable,
            hasDocumentation = hasDocumentation,
            hasConfiguration = hasConfiguration,
            issues = issues,
        )
    }
}
