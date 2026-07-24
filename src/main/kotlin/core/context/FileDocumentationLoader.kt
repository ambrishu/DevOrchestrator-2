package core.context

import core.common.exception.ContextException
import java.nio.file.Files
import java.nio.file.Path

/** [DocumentationLoader] backed by direct filesystem reads. */
class FileDocumentationLoader : DocumentationLoader {

    override fun load(repositoryPath: Path, relativePath: String): String? {
        val file = repositoryPath.resolve(relativePath)
        if (!Files.isRegularFile(file)) {
            return null
        }

        return try {
            Files.readString(file)
        } catch (e: Exception) {
            throw ContextException("Failed to read documentation file: $file", e)
        }
    }
}
