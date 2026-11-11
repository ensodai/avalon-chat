package app.di

import io.ktor.server.application.*
import org.koin.core.context.GlobalContext
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.KOIN_ATTRIBUTE_KEY
import org.koin.ktor.plugin.setKoin
import org.koin.plugin.module.dsl.startKoin

fun Application.koinPlugin() {
    install(Koin) {
        modules(
            AppKoinModule().module(),
            PaymentModule().module(),
            TelegramModule().module(),
            SpecialBindingsModule().module(),
        )
    }
}
