package core.common.di

import core.configuration.ConfigurationLoader
import core.configuration.YamlConfigurationLoader
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
})
