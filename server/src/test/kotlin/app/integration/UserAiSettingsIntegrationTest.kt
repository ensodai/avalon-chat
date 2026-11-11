package app.di

import app.data.db.DatabaseFactory
import app.data.db.tables.users.AppUsers
import app.data.repository.UserAiSettingsRepository
import app.data.repository.UserRepository
import app.domain.chat.billing.AiStrategyResolver
import app.domain.chat.billing.BillingRouter
import app.domain.chat.billing.GenerationActionType
import app.domain.chat.billing.RouteResult
import app.service.ai.AiProviderService
import app.utils.dbQuery
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.update
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.uuid.Uuid

class UserAiSettingsIntegrationTest : KoinTest {

    private val adminUserId = Uuid.parse("8bd56bfb-54e2-4d44-b1c7-be6bc29cb99d")

    private val userRepository by inject<UserRepository>()
    private val userAiSettingsRepository by inject<UserAiSettingsRepository>()
    private val aiProviderService by inject<AiProviderService>()
    private val aiStrategyResolver by inject<AiStrategyResolver>()
    private val billingRouter by inject<BillingRouter>()

    companion object {
        @BeforeAll
        @JvmStatic
        fun initDb() {
            DatabaseFactory.init()
        }
    }

    @BeforeEach
    fun setup() = runBlocking {
        startKoin {
            allowOverride(true)
            modules(
                AppKoinModule().module(),
                PaymentModule().module(),
                TelegramModule().module(),
                SpecialBindingsModule().module()
            )
        }
        aiStrategyResolver.isDayZ = false
        aiStrategyResolver.adminTestUserIds.clear()
        aiProviderService.refreshCache()
    }

    @AfterEach
    fun teardown() {
        stopKoin()
    }

    @Test
    fun `before DayZ isDayZ=false regular user uses ServerEnergy without custom keys`() = runBlocking {
        aiStrategyResolver.isDayZ = false
        aiStrategyResolver.adminTestUserIds.clear()

        dbQuery {
            AppUsers.update({ AppUsers.id eq adminUserId }) {
                it[premiumStatus] = PremiumStatus.FREE
            }
        }
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            providerId = com.ensodai.shared.avalon.user.models.AiProviderType.OPENROUTER,
            isCustomProviderEnabled = false,
            apiKey = null
        )

        val routeResult = billingRouter.checkRoute(adminUserId, GenerationActionType.TEXT_REPLY)
        assertTrue(routeResult is RouteResult.ServerEnergy, "До наступления Day-Z обычный пользователь использует серверную энергию")
    }

    @Test
    fun `admin user with isDayZ=false has DayZ active and is blocked without custom key`() = runBlocking {
        aiStrategyResolver.isDayZ = false
        aiStrategyResolver.adminTestUserIds.add(adminUserId) // Добавляем админа в список тестирования

        dbQuery {
            AppUsers.update({ AppUsers.id eq adminUserId }) {
                it[premiumStatus] = PremiumStatus.FREE
            }
        }
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            providerId = com.ensodai.shared.avalon.user.models.AiProviderType.OPENROUTER,
            isCustomProviderEnabled = false,
            apiKey = null
        )

        val routeResult = billingRouter.checkRoute(adminUserId, GenerationActionType.TEXT_REPLY)
        assertEquals(RouteResult.Error.CustomKeyMissing, routeResult, "Для админа Day-Z активен, поэтому без ключа блокируется")
    }

    @Test
    fun `when isDayZ=true free user without custom key is blocked with CustomKeyMissing`() = runBlocking {
        aiStrategyResolver.isDayZ = true

        dbQuery {
            AppUsers.update({ AppUsers.id eq adminUserId }) {
                it[premiumStatus] = PremiumStatus.FREE
            }
        }
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            providerId = com.ensodai.shared.avalon.user.models.AiProviderType.OPENROUTER,
            isCustomProviderEnabled = false,
            apiKey = null
        )

        val routeResult = billingRouter.checkRoute(adminUserId, GenerationActionType.TEXT_REPLY)
        assertEquals(RouteResult.Error.CustomKeyMissing, routeResult, "В режиме Day-Z Free-пользователь без ключа блокируется")
    }

    @Test
    fun `when isDayZ=true paid subscriber without custom key continues to use ServerEnergy`() = runBlocking {
        aiStrategyResolver.isDayZ = true

        dbQuery {
            AppUsers.update({ AppUsers.id eq adminUserId }) {
                it[premiumStatus] = PremiumStatus.PREMIUM
            }
        }
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            providerId = com.ensodai.shared.avalon.user.models.AiProviderType.OPENROUTER,
            isCustomProviderEnabled = false,
            apiKey = null
        )

        val routeResult = billingRouter.checkRoute(adminUserId, GenerationActionType.TEXT_REPLY)
        assertTrue(routeResult is RouteResult.ServerEnergy, "В режиме Day-Z платный подписчик продолжает использовать серверную энергию")
    }

    @Test
    fun `user with explicit custom key always uses FreeCustomKey in both modes`() = runBlocking {
        dbQuery {
            AppUsers.update({ AppUsers.id eq adminUserId }) {
                it[premiumStatus] = PremiumStatus.FREE
            }
        }
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            providerId = com.ensodai.shared.avalon.user.models.AiProviderType.OPENROUTER,
            isCustomProviderEnabled = true,
            apiKey = "sk-user-custom-key",
            imageModel = "google/gemini-2.5-flash-image",
            imagePromptModel = "google/gemini-2.5-flash-image"
        )

        // При isDayZ = false
        aiStrategyResolver.isDayZ = false
        val routeResult1 = billingRouter.checkRoute(adminUserId, GenerationActionType.IMAGE_GIFT)
        assertTrue(routeResult1 is RouteResult.FreeCustomKey)

        // При isDayZ = true
        aiStrategyResolver.isDayZ = true
        val routeResult2 = billingRouter.checkRoute(adminUserId, GenerationActionType.IMAGE_GIFT)
        assertTrue(routeResult2 is RouteResult.FreeCustomKey)
    }

    @Test
    fun `user with google provider and api key uses FreeCustomKey and does not require image models`() = runBlocking {
        dbQuery {
            AppUsers.update({ AppUsers.id eq adminUserId }) {
                it[premiumStatus] = PremiumStatus.FREE
            }
        }
        userAiSettingsRepository.saveOrUpdateSettings(
            userId = adminUserId,
            providerId = com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE,
            isCustomProviderEnabled = true,
            apiKey = "AIzaSyTestGoogleKey123"
        )

        val strategy = aiStrategyResolver.resolveStrategy(adminUserId)
        assertTrue(strategy is app.domain.chat.billing.AiExecutionStrategy.CustomProvider)
        assertEquals("google", (strategy as app.domain.chat.billing.AiExecutionStrategy.CustomProvider).providerId)
        assertEquals("AIzaSyTestGoogleKey123", strategy.apiKey)

        // Проверяем текстовый роут
        val textRoute = billingRouter.checkRoute(adminUserId, GenerationActionType.TEXT_REPLY)
        assertTrue(textRoute is RouteResult.FreeCustomKey)

        // Проверяем генерацию картинок — для Google не требуются кастомные модели OpenRouter
        val imageRoute = billingRouter.checkRoute(adminUserId, GenerationActionType.IMAGE_GIFT)
        assertTrue(imageRoute is RouteResult.FreeCustomKey, "Для Google провайдера генерация картинок работает через дефолтный Imagen без доп. настроек")
    }

    @Test
    fun `ai provider service resolves correct urls and falls back to default on invalid provider`() = runBlocking {
        val openRouterUrl = aiProviderService.resolveBaseUrl(com.ensodai.shared.avalon.user.models.AiProviderType.OPENROUTER)
        assertEquals("https://openrouter.ai/api/v1", openRouterUrl)

        val openAiUrl = aiProviderService.resolveBaseUrl(com.ensodai.shared.avalon.user.models.AiProviderType.OPENAI)
        assertEquals("https://api.openai.com/v1", openAiUrl)

        val googleUrl = aiProviderService.resolveBaseUrl(com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE)
        assertEquals("https://generativelanguage.googleapis.com", googleUrl)

        val nullUrl = aiProviderService.resolveBaseUrl(null)
        assertEquals("https://generativelanguage.googleapis.com", nullUrl)
    }

    @Test
    fun `ai model catalog service provides rich curated models for Google and OpenAI`() = runBlocking {
        val catalogService by inject<app.service.ai.AiModelCatalogService>()

        val googleText = catalogService.getModels(
            com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE,
            com.ensodai.shared.avalon.user.models.AiModelCategory.TEXT
        )
        assertFalse(googleText.isEmpty())
        assertEquals("gemini-3.1-flash-lite", catalogService.getRecommendedModel(com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE, com.ensodai.shared.avalon.user.models.AiModelCategory.TEXT))
        assertTrue(googleText.any { it.id == "gemini-2.5-flash" && it.isRecommended })
        assertTrue(googleText.any { it.id == "gemini-3.1-flash-lite" && it.isRecommended })
        assertTrue(googleText.any { it.id == "gemini-2.5-pro" && it.isRecommended })

        val googleTts = catalogService.getModels(
            com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE,
            com.ensodai.shared.avalon.user.models.AiModelCategory.TTS
        )
        assertTrue(googleTts.any { it.id == "gemini-2.5-flash-preview-tts" && it.isRecommended })

        val googleImage = catalogService.getModels(
            com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE,
            com.ensodai.shared.avalon.user.models.AiModelCategory.IMAGE
        )
        assertTrue(googleImage.any { it.id == "gemini-2.5-flash-image" && it.isRecommended })

        val openAiText = catalogService.getModels(
            com.ensodai.shared.avalon.user.models.AiProviderType.OPENAI,
            com.ensodai.shared.avalon.user.models.AiModelCategory.TEXT
        )
        assertTrue(openAiText.any { it.id == "gpt-4o" && it.isRecommended })
    }
}
