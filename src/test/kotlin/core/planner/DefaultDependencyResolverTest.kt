package core.planner

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.Story
import models.StoryStatus

class DefaultDependencyResolverTest : FunSpec({

    val resolver = DefaultDependencyResolver()

    fun story(id: String, status: StoryStatus, dependencies: List<String> = emptyList()) =
        Story(id = id, title = id, description = "", status = status, dependencies = dependencies)

    test("a story with no dependencies is always satisfied") {
        val target = story("ADO-001", StoryStatus.TODO)
        resolver.isSatisfied(target, listOf(target)) shouldBe true
    }

    test("a story is satisfied when every dependency is done") {
        val dep = story("ADO-001", StoryStatus.DONE)
        val target = story("ADO-002", StoryStatus.TODO, listOf("ADO-001"))

        resolver.isSatisfied(target, listOf(dep, target)) shouldBe true
    }

    test("a story is unsatisfied when a dependency is not done") {
        val dep = story("ADO-001", StoryStatus.IN_PROGRESS)
        val target = story("ADO-002", StoryStatus.TODO, listOf("ADO-001"))

        resolver.isSatisfied(target, listOf(dep, target)) shouldBe false
    }

    test("a story is unsatisfied when a dependency does not exist") {
        val target = story("ADO-002", StoryStatus.TODO, listOf("ADO-999"))

        resolver.isSatisfied(target, listOf(target)) shouldBe false
    }

    test("a story is unsatisfied if any of several dependencies is incomplete") {
        val depA = story("ADO-001", StoryStatus.DONE)
        val depB = story("ADO-002", StoryStatus.TODO)
        val target = story("ADO-003", StoryStatus.TODO, listOf("ADO-001", "ADO-002"))

        resolver.isSatisfied(target, listOf(depA, depB, target)) shouldBe false
    }
})
