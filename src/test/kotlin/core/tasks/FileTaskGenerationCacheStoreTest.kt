package core.tasks

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import models.TaskGenerationCache
import java.nio.file.Files
import java.nio.file.Path

class FileTaskGenerationCacheStoreTest : FunSpec({

    val store = FileTaskGenerationCacheStore()

    fun repo(): Path = Files.createTempDirectory("ado-task-cache-test")

    test("returns null when no cache file exists") {
        store.load(repo()).shouldBeNull()
    }

    test("round-trips a saved cache") {
        val repository = repo()
        val cache = TaskGenerationCache(inputHash = "abc123", content = "ADO-001 — Example")

        store.save(repository, cache)

        store.load(repository) shouldBe cache
    }

    test("treats a corrupt cache file as absent rather than throwing") {
        val repository = repo()
        Files.createDirectories(repository.resolve(".ado"))
        Files.writeString(repository.resolve(".ado").resolve("task-generation-cache.yaml"), "not: [valid, yaml: at all")

        store.load(repository).shouldBeNull()
    }

    test("treats a blank cache file as absent") {
        val repository = repo()
        Files.createDirectories(repository.resolve(".ado"))
        Files.writeString(repository.resolve(".ado").resolve("task-generation-cache.yaml"), "")

        store.load(repository).shouldBeNull()
    }

    test("overwrites a previously saved cache") {
        val repository = repo()
        store.save(repository, TaskGenerationCache(inputHash = "first", content = "first content"))
        store.save(repository, TaskGenerationCache(inputHash = "second", content = "second content"))

        store.load(repository) shouldBe TaskGenerationCache(inputHash = "second", content = "second content")
    }
})
