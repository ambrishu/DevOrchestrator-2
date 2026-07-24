package core.context

import java.nio.file.Path

/** Classifies repository-relative paths into production source, test, and documentation files. */
object RepositoryFileClassifier {

    fun isSourceFile(relativePath: Path): Boolean =
        relativePath.startsWith("src/main/kotlin") && relativePath.toString().endsWith(".kt")

    fun isTestFile(relativePath: Path): Boolean =
        relativePath.startsWith("src/test/kotlin") && relativePath.toString().endsWith(".kt")

    fun isDocumentationFile(relativePath: Path): Boolean =
        relativePath.startsWith("docs") && relativePath.toString().endsWith(".md")
}
