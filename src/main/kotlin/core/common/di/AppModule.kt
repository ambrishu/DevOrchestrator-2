package core.common.di

import core.configuration.ConfigurationLoader
import core.configuration.ConfigurationValidator
import core.configuration.DefaultConfigurationValidator
import core.configuration.YamlConfigurationLoader
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
}
