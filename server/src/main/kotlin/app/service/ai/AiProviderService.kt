package app.service.ai

import app.data.db.tables.ai.AiProviderModel
import app.data.db.tables.ai.AiProviders
import app.utils.dbQuery
import com.ensodai.shared.avalon.user.models.AiProviderResponse
import com.ensodai.shared.avalon.user.models.AiProviderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

@Single
class AiProviderService(
    private val coroutineScope: CoroutineScope
) {
    private val logger = LoggerFactory.getLogger(AiProviderService::class.java)

    private val cachedProviders = AtomicReference<Map<AiProviderType, AiProviderModel>>(emptyMap())
    private val defaultProviderRef = AtomicReference<AiProviderModel?>(null)

    init {
        // Фоновое периодическое обновление кэша провайдеров раз в минуту
        coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    refreshCache()
                } catch (e: Exception) {
                    logger.error("Failed to refresh AI providers cache: ${e.message}")
                }
                delay(1.minutes)
            }
        }
    }

    suspend fun refreshCache() {
        val providers = dbQuery {
            AiProviders.selectAll()
                .where { AiProviders.deletedAt.isNull() }
                .orderBy(AiProviders.orderIndex)
                .map { row ->
                    AiProviderModel(
                        id = row[AiProviders.id].value,
                        providerId = row[AiProviders.providerId],
                        displayName = row[AiProviders.displayName],
                        baseUrl = row[AiProviders.baseUrl],
                        isActive = row[AiProviders.isActive],
                        isDefault = row[AiProviders.isDefault],
                        orderIndex = row[AiProviders.orderIndex]
                    )
                }
        }

        val map = providers.associateBy { it.providerId }
        val defaultProv = providers.firstOrNull { it.isDefault && it.isActive }
            ?: providers.firstOrNull { it.isActive }
            ?: AiProviderModel(
                id = Uuid.parse("00000000-0000-0000-0000-000000000005"),
                providerId = AiProviderType.GOOGLE,
                displayName = "Google AI Studio",
                baseUrl = "https://generativelanguage.googleapis.com",
                isActive = true,
                isDefault = true,
                orderIndex = 1
            )

        cachedProviders.set(map)
        defaultProviderRef.set(defaultProv)
    }

    fun getActiveProviders(): List<AiProviderResponse> {
        val providers = cachedProviders.get()
        if (providers.isEmpty()) {
            return listOf(
                AiProviderResponse(
                    id = AiProviderType.GOOGLE.id,
                    displayName = AiProviderType.GOOGLE.displayName,
                    isDefault = true
                )
            )
        }
        return providers.values
            .filter { it.isActive }
            .sortedBy { it.orderIndex }
            .map {
                AiProviderResponse(
                    id = it.providerId.id,
                    displayName = it.displayName,
                    isDefault = it.isDefault
                )
            }
    }

    fun resolveBaseUrl(provider: AiProviderType): String {
        val providers = cachedProviders.get()
        val p = providers[provider]
        if (p != null && p.isActive) {
            return p.baseUrl
        }
        return provider.defaultBaseUrl.takeIf { it.isNotBlank() }
            ?: defaultProviderRef.get()?.baseUrl
            ?: "https://generativelanguage.googleapis.com"
    }

    fun resolveBaseUrl(providerId: String?): String {
        val type = AiProviderType.fromId(providerId)
        return resolveBaseUrl(type)
    }
}
