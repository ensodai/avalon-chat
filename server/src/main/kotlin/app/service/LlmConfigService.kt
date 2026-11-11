package app.service
import com.ensodai.shared.avalon.gifts.models.*

import app.data.db.tables.LlmConfig
import app.data.db.tables.LlmModels
import app.data.db.tables.LlmProvider
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import app.utils.dbQuery
import app.utils.logger.WssLogger
import kotlinx.coroutines.*
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.minutes
import org.koin.core.annotation.Single

@Single
class LlmConfigService {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val cache = AtomicReference<Map<String, LlmConfig>>(emptyMap())

    private val sortedListCache = AtomicReference<List<LlmConfig>>(emptyList())

    init {
        serviceScope.launch {
            refreshCache()
            while (isActive) {
                delay(10.minutes)
                refreshCache()
            }
        }
    }

    private suspend fun refreshCache() {
        try {
            WssLogger.info("LlmConfigService: Начинаем загрузку кэша моделей...")
            
            val models = dbQuery {
                val allRows = LlmModels.selectAll().toList()
                WssLogger.info("LlmConfigService: Найдено строк в БД: ${allRows.size}")
                
                allRows.forEach { row ->
                    WssLogger.info("LlmConfigService: Модель - code=${row[LlmModels.code]}, name=${row[LlmModels.name]}, active=${row[LlmModels.isActive]}")
                }
                
                LlmModels.selectAll()
                    .orderBy(LlmModels.sortOrder to SortOrder.ASC)
                    .map { row ->
                        LlmConfig(
                            code = row[LlmModels.code],
                            name = row[LlmModels.name],
                            modelAPIID = row[LlmModels.realModelId],
                            provider = row[LlmModels.provider],
                            energyCost = row[LlmModels.energyCost],
                            minStatus = row[LlmModels.minStatus],
                            isActive = row[LlmModels.isActive]
                        )
                    }
            }

            WssLogger.info("LlmConfigService: Загружено моделей: ${models.size}")

            // Обновляем мапу для поиска
            cache.set(models.associateBy { it.code })

            // Обновляем список для UI (только активные)
            sortedListCache.set(models.filter { it.isActive })
            
            WssLogger.info("LlmConfigService: Кэш обновлён успешно. Активных моделей: ${sortedListCache.get().size}")

        } catch (e: Exception) {
            WssLogger.fail("LlmConfigService: ОШИБКА при загрузке кэша: ${e.message}")
            e.printStackTrace()
        }
    }

    // 1. Получить конфиг по коду (для генерации)
    fun getConfig(code: String?, userPremiumStatus: PremiumStatus? = null): LlmConfig {
        // Если модели нет в кэше — отдаем стандартную дешевку
        val config = cache.get()[code] ?: fallbackConfig()
        
        if (userPremiumStatus != null && userPremiumStatus.ordinal < config.minStatus.ordinal) {
            return fallbackConfig()
        }
        
        return config
    }

    fun findConfig(code: String?): LlmConfig? {
        return code?.let { cache.get()[it] }
    }

    fun findConfigByApiId(modelApiId: String?): LlmConfig? {
        if (modelApiId == null) return null
        return cache.get().values.firstOrNull { it.modelAPIID == modelApiId }
    }

    // 2. Получить список для UI
    fun getAvailableModels(): List<LlmConfig> {
        return sortedListCache.get().filter { it.isActive }
    }

    private fun fallbackConfig() = LlmConfig(
        code = "gemini_flash_2.5_lite",
        name = "Flash 2.5 lite", // Название для UI
        modelAPIID = "gemini-2.5-flash-lite",
        provider = LlmProvider.GOOGLE,
        energyCost = 1,
        minStatus = PremiumStatus.FREE,
        isActive = true
    )
}
