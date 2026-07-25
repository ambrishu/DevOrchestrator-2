package core.common.exception

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class AdoExceptionTest : FunSpec({

    test("ConfigurationException is an AdoException") {
        val exception = ConfigurationException("bad config")
        exception.shouldBeInstanceOf<AdoException>()
        exception.message shouldBe "bad config"
    }

    test("RepositoryException is an AdoException") {
        val exception = RepositoryException("bad repository")
        exception.shouldBeInstanceOf<AdoException>()
        exception.message shouldBe "bad repository"
    }

    test("StoryLoadException is an AdoException") {
        val exception = StoryLoadException("bad story")
        exception.shouldBeInstanceOf<AdoException>()
        exception.message shouldBe "bad story"
    }

    test("ProgressException is an AdoException") {
        val exception = ProgressException("bad progress")
        exception.shouldBeInstanceOf<AdoException>()
        exception.message shouldBe "bad progress"
    }

    test("ContextException is an AdoException") {
        val exception = ContextException("bad context")
        exception.shouldBeInstanceOf<AdoException>()
        exception.message shouldBe "bad context"
    }

    test("ProcessExecutionException is an AdoException") {
        val exception = ProcessExecutionException("could not start process")
        exception.shouldBeInstanceOf<AdoException>()
        exception.message shouldBe "could not start process"
    }

    test("AgentInvocationException is an AdoException") {
        val exception = AgentInvocationException("agent failed")
        exception.shouldBeInstanceOf<AdoException>()
        exception.message shouldBe "agent failed"
    }

    test("BuildExecutionException is an AdoException") {
        val exception = BuildExecutionException("could not start the build")
        exception.shouldBeInstanceOf<AdoException>()
        exception.message shouldBe "could not start the build"
    }

    test("exceptions preserve their cause") {
        val cause = IllegalStateException("root cause")
        val exception = ConfigurationException("wrapped", cause)
        exception.cause shouldBe cause
    }
})
