package core.common.di

import adapters.claude.ClaudeCodeAdapter
import adapters.claude.ClaudeCodeInvoker
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
import core.repository.DefaultRepositoryLoader
import core.repository.RepositoryLoader
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

    single<ExecutionEngine> {
        DefaultExecutionEngine(
            storyLoader = get(),
            storyPlanner = get(),
            progressTracker = get(),
            contextBuilder = get(),
            agentAdapter = get(),
        )
    }
}
