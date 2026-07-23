package core.common.di

import core.configuration.ConfigurationLoader
import core.configuration.YamlConfigurationLoader
import core.repository.DefaultRepositoryLoader
import core.repository.RepositoryLoader
import org.koin.dsl.module

/** Wires foundation-level singletons used by the CLI. */
val appModule = module {
    single<ConfigurationLoader> { YamlConfigurationLoader() }
    single<RepositoryLoader> { DefaultRepositoryLoader() }
}
