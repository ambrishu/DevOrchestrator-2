package core.common.di

import adapters.claude.ClaudeCodeAdapter
import adapters.claude.ClaudeCodeInvoker
import adapters.claude.ClaudeCodeReviewAgent
import core.agent.AgentAdapter
import core.agent.DefaultPromptBuilder
import core.agent.PromptBuilder
import core.build.BuildExecutor
import core.build.DefaultBuildExecutor
import core.configuration.ConfigurationLoader
import core.configuration.ConfigurationValidator
import core.configuration.DefaultConfigurationValidator
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
import org.koin.dsl.module
import utils.DefaultProcessExecutor
import utils.ProcessExecutor

/** Wires foundation-level singletons used by the CLI. */
val appModule = module {
    single<ConfigurationValidator> { DefaultConfigurationValidator() }
    single<ConfigurationLoader> { YamlConfigurationLoader(validator = get()) }
    single<RepositoryLoader> { DefaultRepositoryLoader() }

    single<StoryParser> { MarkdownStoryParser() }
    single<StoryLoader> { TasksFileLoader(parser = get()) }
    single<DependencyResolver> { DefaultDependencyResolver() }
    single<StoryPlanner> { DefaultStoryPlanner(dependencyResolver = get()) }

    single<ProgressTracker> { FileProgressTracker() }

    single<RepositoryScanner> { DefaultRepositoryScanner() }
    single<DocumentationLoader> { FileDocumentationLoader() }
    single<SourceSelector> { DefaultSourceSelector() }
    single<ContextBuilder> {
        DefaultContextBuilder(repositoryScanner = get(), documentationLoader = get(), sourceSelector = get())
    }

    single<ProcessExecutor> { DefaultProcessExecutor() }
    single<PromptBuilder> { DefaultPromptBuilder() }
    single<AgentAdapter> {
        ClaudeCodeAdapter(promptBuilder = get(), invoker = ClaudeCodeInvoker(processExecutor = get()))
    }

    single<BuildExecutor> { DefaultBuildExecutor(configurationLoader = get(), processExecutor = get()) }
    single<FailureAnalyzer> { DefaultFailureAnalyzer() }
    single<RepairContextBuilder> { DefaultRepairContextBuilder() }
    single<RepairLoop> {
        DefaultRepairLoop(
            failureAnalyzer = get(),
            repairContextBuilder = get(),
            agentAdapter = get(),
            buildExecutor = get(),
        )
    }

    single<QualityGateEngine> {
        DefaultQualityGateEngine(configurationLoader = get(), buildExecutor = get(), processExecutor = get())
    }

    single<GitManager> { DefaultGitManager(processExecutor = get()) }
    single<CommitMessageFormatter> { DefaultCommitMessageFormatter() }

    single<ReviewPromptBuilder> { DefaultReviewPromptBuilder() }
    single<CodeReviewAgent> {
        ClaudeCodeReviewAgent(
            gitManager = get(),
            reviewPromptBuilder = get(),
            invoker = ClaudeCodeInvoker(processExecutor = get()),
        )
    }

    single<ExecutionEngine> {
        DefaultExecutionEngine(
            storyLoader = get(),
            storyPlanner = get(),
            progressTracker = get(),
            contextBuilder = get(),
            agentAdapter = get(),
            buildExecutor = get(),
            repairLoop = get(),
            qualityGateEngine = get(),
            codeReviewAgent = get(),
            gitManager = get(),
            commitMessageFormatter = get(),
            configurationLoader = get(),
        )
    }
}
