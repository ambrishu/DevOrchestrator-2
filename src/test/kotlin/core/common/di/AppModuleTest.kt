package core.common.di

import core.configuration.ConfigurationLoader
import core.configuration.YamlConfigurationLoader
import core.planner.DefaultDependencyResolver
import core.planner.DefaultStoryPlanner
import core.planner.DependencyResolver
import core.planner.MarkdownStoryParser
import core.planner.StoryLoader
import core.planner.StoryParser
import core.planner.StoryPlanner
import core.planner.TasksFileLoader
import core.repository.DefaultRepositoryLoader
import core.repository.RepositoryLoader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import org.koin.dsl.koinApplication

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
})
