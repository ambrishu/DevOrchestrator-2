package models

/** A repository file selected into a [ContextPackage], identified by its repository-relative path. */
data class SourceFile(
    val path: String,
    val content: String,
)
