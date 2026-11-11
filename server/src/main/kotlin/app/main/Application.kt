package app.main

import app.data.db.DatabaseFactory
import app.di.koinPlugin
import app.plugins.*
import app.service.QuotaResetService
import app.service.outreach.OutreachScheduler
import app.service.outreach.WarmOutreachScheduler
import app.scheduler.PopularityScheduler
import io.ktor.server.application.*
import org.koin.ktor.ext.inject

fun Application.module() {

    DatabaseFactory.init()
    backendSecurityHeadersPlugin()
    gzipPlugin()
    koinPlugin()
    configureSerialization()
    configureResource()
    configureSecurity()
    configureWebSockets()
    rateLimit()
    // appVersionEnforcer() // Выпилено в пользу RPC-проверки версии
    xForwardedHeaders()
    corsPlugin()
    configureRPC()

    configureRouting()

    telegramBot()

    val outreachScheduler: OutreachScheduler by inject()
    val warmOutreachScheduler: WarmOutreachScheduler by inject()
    val popularityScheduler: PopularityScheduler by inject()

    QuotaResetService.start()
//    outreachScheduler.start()
//    warmOutreachScheduler.start()
    popularityScheduler.start()
//    ThumbhashMigrationService.start()

    logger()
}
