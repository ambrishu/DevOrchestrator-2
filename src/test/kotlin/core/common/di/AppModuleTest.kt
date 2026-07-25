package core.common.di

import adapters.claude.ClaudeCodeAdapter
import adapters.claude.ClaudeCodeReviewAgent
import core.agent.AgentAdapter
import core.agent.DefaultPromptBuilder
import core.agent.PromptBuilder
import core.build.BuildExecutor
import core.build.DefaultBuildExecutor
import core.configuration.ConfigurationLoader
import core.configuration.YamlConfigurationLoader
import core.context.ContextBuilder
import core.context.DefaultContextBuilder
import core.context.DefaultRepositoryScanner
import core.context.DefaultSourceSelector
import core.context.DocumentationLoader
import core.context.FileDocumentationLoader
import core.context.RepositoryScanner
import core.context.SourceSelector
import core.execution.DefaultExecutionEngine
import core.execution.ExecutionEngine
import core.git.CommitMessageFormatter
import core.git.DefaultCommitMessageFormatter
import core.git.DefaultGitManager
import core.git.GitManager
import core.planner.DefaultDependencyResolver
import core.planner.DefaultStoryPlanner
import core.planner.DependencyResolver
import core.planner.MarkdownStoryParser
import core.planner.StoryLoader
import core.planner.StoryParser
import core.planner.StoryPlanner
import core.planner.TasksFileLoader
import core.progress.FileProgressTracker
import core.progress.ProgressTracker
import core.repair.DefaultFailureAnalyzer
import core.repair.DefaultRepairContextBuilder
import core.repair.DefaultRepairLoop
import core.repair.FailureAnalyzer
import core.repair.RepairContextBuilder
import core.repair.RepairLoop
import core.repository.DefaultRepositoryLoader
import core.repository.RepositoryLoader
import core.review.CodeReviewAgent
import core.review.DefaultReviewPromptBuilder
import core.review.ReviewPromptBuilder
import core.validation.DefaultQualityGateEngine
import core.validation.QualityGateEngine
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import org.koin.dsl.koinApplication
import utils.DefaultProcessExecutor
import utils.ProcessExecutor

class AppModuleTest : FunSpec({

    test("appModule resolves a ConfigurationLoader") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<ConfigurationLoader>().shouldBeInstanceOf<YamlConfigurationLoader>()
        koin.close()
    }

    test("appModule resolves a RepositoryLoader") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<RepositoryLoader>().shouldBeInstanceOf<DefaultRepositoryLoader>()
        koin.close()
    }

    test("appModule resolves a StoryParser") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<StoryParser>().shouldBeInstanceOf<MarkdownStoryParser>()
        koin.close()
    }

    test("appModule resolves a StoryLoader") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<StoryLoader>().shouldBeInstanceOf<TasksFileLoader>()
        koin.close()
    }

    test("appModule resolves a DependencyResolver") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<DependencyResolver>().shouldBeInstanceOf<DefaultDependencyResolver>()
        koin.close()
    }

    test("appModule resolves a StoryPlanner") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<StoryPlanner>().shouldBeInstanceOf<DefaultStoryPlanner>()
        koin.close()
    }

    test("appModule resolves a ProgressTracker") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<ProgressTracker>().shouldBeInstanceOf<FileProgressTracker>()
        koin.close()
    }

    test("appModule resolves a RepositoryScanner") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<RepositoryScanner>().shouldBeInstanceOf<DefaultRepositoryScanner>()
        koin.close()
    }

    test("appModule resolves a DocumentationLoader") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<DocumentationLoader>().shouldBeInstanceOf<FileDocumentationLoader>()
        koin.close()
    }

    test("appModule resolves a SourceSelector") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<SourceSelector>().shouldBeInstanceOf<DefaultSourceSelector>()
        koin.close()
    }

    test("appModule resolves a ContextBuilder") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<ContextBuilder>().shouldBeInstanceOf<DefaultContextBuilder>()
        koin.close()
    }

    test("appModule resolves a ProcessExecutor") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<ProcessExecutor>().shouldBeInstanceOf<DefaultProcessExecutor>()
        koin.close()
    }

    test("appModule resolves a PromptBuilder") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<PromptBuilder>().shouldBeInstanceOf<DefaultPromptBuilder>()
        koin.close()
    }

    test("appModule resolves an AgentAdapter") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<AgentAdapter>().shouldBeInstanceOf<ClaudeCodeAdapter>()
        koin.close()
    }

    test("appModule resolves an ExecutionEngine") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<ExecutionEngine>().shouldBeInstanceOf<DefaultExecutionEngine>()
        koin.close()
    }

    test("appModule resolves a BuildExecutor") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<BuildExecutor>().shouldBeInstanceOf<DefaultBuildExecutor>()
        koin.close()
    }

    test("appModule resolves a FailureAnalyzer") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<FailureAnalyzer>().shouldBeInstanceOf<DefaultFailureAnalyzer>()
        koin.close()
    }

    test("appModule resolves a RepairContextBuilder") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<RepairContextBuilder>().shouldBeInstanceOf<DefaultRepairContextBuilder>()
        koin.close()
    }

    test("appModule resolves a RepairLoop") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<RepairLoop>().shouldBeInstanceOf<DefaultRepairLoop>()
        koin.close()
    }

    test("appModule resolves a QualityGateEngine") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<QualityGateEngine>().shouldBeInstanceOf<DefaultQualityGateEngine>()
        koin.close()
    }

    test("appModule resolves a GitManager") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<GitManager>().shouldBeInstanceOf<DefaultGitManager>()
        koin.close()
    }

    test("appModule resolves a CommitMessageFormatter") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<CommitMessageFormatter>().shouldBeInstanceOf<DefaultCommitMessageFormatter>()
        koin.close()
    }

    test("appModule resolves a ReviewPromptBuilder") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<ReviewPromptBuilder>().shouldBeInstanceOf<DefaultReviewPromptBuilder>()
        koin.close()
    }

    test("appModule resolves a CodeReviewAgent") {
        val koin = koinApplication { modules(appModule) }.koin
        koin.get<CodeReviewAgent>().shouldBeInstanceOf<ClaudeCodeReviewAgent>()
        koin.close()
    }
})
