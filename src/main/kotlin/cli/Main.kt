package cli

import core.common.di.appModule
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    startKoin {
        modules(appModule)
    }
    AdoCommand().main(args)
}
