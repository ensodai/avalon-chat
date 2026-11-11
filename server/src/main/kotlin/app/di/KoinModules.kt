package app.di

import app.cache.RedisService
import app.data.repository.GlobalSettingsRepository
import app.service.paymentService.PaymentNotificationService
import app.service.VertexAuthData
import app.service.paymentService.BillingValidatorManager
import app.service.paymentService.PaymentService
import app.service.paymentService.TariffService
import app.utils.EnvProvider
import com.github.kotlintelegrambot.Bot
import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.JsonParser
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineExceptionHandler
import app.utils.logger.WssLogger
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import tgBot.BotConfig
import java.io.ByteArrayInputStream

/**
 * Корневой модуль — сканирует все @Single/@Factory классы в пакете app.
 * Включает все подмодули.
 */
@Module(includes = [
    PaymentModule::class,
    TelegramModule::class,
    SpecialBindingsModule::class,
])
@ComponentScan("app")
class AppKoinModule

/**
 * Точка входа Koin для Ktor-приложения.
 * Аналог @KoinApplication в Android.
 * Собирует все модули в одну иерархию через AppKoinModule.
 */

@KoinApplication(modules = [AppKoinModule::class])
object KtorKoinAppConfig


/**
 * Payment module — сканирует payment сервисы.
 * Бинды RuStoreBillingValidator, GoogleBillingValidator, PaymentNotificationService,
 * BillingValidatorManager, PaymentService, WebhookService
 * предоставляются через SpecialBindingsModule.
 */
@Module
@ComponentScan("app.service.paymentService")
class PaymentModule

/**
 * Telegram module — сканирует tgBot пакет.
 * Bot и adminId предоставляются через SpecialBindingsModule.
 */
@Module
@ComponentScan("tgBot")
class TelegramModule

/**
 * Специальные бинды которые нельзя выразить через аннотации:
 * - HttpClient (нужна конфигурация)
 * - CoroutineScope (application scope)
 * - Json (сериализация)
 * - RuStoreBillingValidator (String params из EnvProvider)
 * - GoogleBillingValidator (String params из EnvProvider)
 * - Bot (Telegram bot из EnvProvider)
 * - Long (admin chat id)
 * - VertexAuthData (сложная инициализация)
 * - PaymentNotificationService (Long param)
 * - PaymentService, WebhookServices, BillingValidatorManager (зависят от вышеперечисленного)
 */
@Module
class SpecialBindingsModule {

    @Single
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }


    @Single
    fun provideApplicationJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Single
    fun provideHttpClient(json: Json): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 5_000
            socketTimeoutMillis = 15_000
        }
    }

    @Single
    fun providePaymentNotificationService(): PaymentNotificationService = PaymentNotificationService(
        adminChatId = EnvProvider.getTelegramAdminId(),
    )

    @Single
    fun provideTelegramBot(
        repository: tgBot.repository.TelegramBotRepository,
        userRepository: app.data.repository.UserRepositoryApp,
        adminSubscriptionService: app.service.AdminSubscriptionService,
        paymentService: app.service.paymentService.PaymentService,
    ): Bot {
        val botToken = EnvProvider.getTelegramBotToken()
        val adminId = EnvProvider.getTelegramAdminId()
        return BotConfig.createBot(
            botToken = botToken,
            adminId = adminId,
            repository = repository,
            userRepository = userRepository,
            adminSubscriptionService = adminSubscriptionService,
            paymentService = paymentService,
        )
    }

    @Single
    fun provideAdminChatId(): Long = EnvProvider.getTelegramAdminId()

    @Single
    fun provideVertexAuthData(): VertexAuthData {
        val jsonString = EnvProvider.getVertexServiceAccountJSON()
        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        val projectId = if (jsonObject.has("project_id")) jsonObject.get("project_id").asString else error("No project_id")
        val credentials = GoogleCredentials.fromStream(
            ByteArrayInputStream(jsonString.toByteArray(Charsets.UTF_8))
        ).createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
        return VertexAuthData(projectId, credentials)
    }

    @Single
    fun provideRedis(): RedisService = RedisService

    @Single
    fun providePaymentService(
        validator: BillingValidatorManager,
        tariffService: TariffService,
        globalSettingsRepository: GlobalSettingsRepository,
        notificationService: PaymentNotificationService,
    ): PaymentService = PaymentService(
        validator = validator,
        tariffService = tariffService,
        globalSettingsRepository = globalSettingsRepository,
        notificationService = notificationService,
    )

    @Single
    fun provideBillingValidatorManager(): BillingValidatorManager = BillingValidatorManager()
}

