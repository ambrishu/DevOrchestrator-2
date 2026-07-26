package models

/** Persisted record of the last successful task generation, keyed by the hash of its input documents. */
data class TaskGenerationCache(
    val inputHash: String,
    val content: String,
)
