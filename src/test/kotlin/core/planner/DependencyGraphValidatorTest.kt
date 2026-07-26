package core.planner

import core.common.exception.StoryLoadException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import models.Story
import models.StoryStatus

class DependencyGraphValidatorTest : FunSpec({

    fun story(id: String, dependencies: List<String> = emptyList()) =
        Story(id = id, title = id, description = "", status = StoryStatus.TODO, dependencies = dependencies)

    test("does not throw when every dependency resolves to a known story") {
        shouldNotThrowAny {
            DependencyGraphValidator.validate(listOf(story("ADO-001"), story("ADO-002", listOf("ADO-001"))))
        }
    }

    test("throws when a story depends on an unknown story ID") {
        shouldThrow<StoryLoadException> {
            DependencyGraphValidator.validate(listOf(story("ADO-001", listOf("ADO-999"))))
        }
    }

    test("does not throw for a backlog with no dependencies at all") {
        shouldNotThrowAny {
            DependencyGraphValidator.validate(listOf(story("ADO-001"), story("ADO-002")))
        }
    }
})
