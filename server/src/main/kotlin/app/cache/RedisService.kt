package app.cache

import app.service.ImageSaveResult
import io.github.oshai.kotlinlogging.KotlinLogging
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.ensodai.shared.avalon.character.models.PaginatedResponse
import com.ensodai.shared.avalon.character.models.CharacterPreviewResponse
import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto

@Serializable
data class TempImageMeta(
    val path: String,
    val width: Int,
    val height: Int,
    val thumbHash: String,
    val sizeBytes: Long,
    val createdAt: Long
)

object RedisService {
    private val logger = KotlinLogging.logger {}
    private val redisUrl by lazy { System.getenv("REDIS_URL") ?: "redis://localhost:6379" }

    private val clientLazy = lazy { RedisClient.create(redisUrl) }
    private val client by clientLazy
    private val connectionLazy = lazy {
        client.connect().also {
            logger.info { "[RedisService] Connected to Redis" }
        }
    }
    private val connection by connectionLazy

    val commands: RedisCommands<String, String> by lazy { connection.sync() }
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Сохраняет метаданные картинки в Redis с TTL 24 часа
     * @return токен (UUID) для последующего использования
     */
    fun saveTempImage(meta: ImageSaveResult): String {
        val token = java.util.UUID.randomUUID().toString()
        val tempMeta = TempImageMeta(
            path = meta.path,
            width = meta.width,
            height = meta.height,
            thumbHash = meta.thumbHash,
            sizeBytes = meta.sizeBytes,
            createdAt = System.currentTimeMillis()
        )
        val jsonStr = json.encodeToString(tempMeta)
        
        // Сохраняем на 24 часа (86400 секунд)
        commands.setex(token, 86400, jsonStr)
        logger.debug { "[RedisService] Saved temp image token (TTL: 24h)" }
        return token
    }

    /**
     * Забирает метаданные по токену и удаляет их из Redis (атомарно)
     * @return метаданные или null если токен не найден/истёк
     */
    fun popTempImage(token: String): TempImageMeta? {
        val jsonStr = commands.getdel(token)
            ?: return null.also { logger.debug { "[RedisService] Temp image token not found" } }
        
        return try {
            val meta = json.decodeFromString<TempImageMeta>(jsonStr)
            logger.debug { "[RedisService] Popped temp image token" }
            meta
        } catch (e: Exception) {
            logger.warn { "[RedisService] Failed to deserialize temp image metadata: ${e.message}" }
            null
        }
    }

    /**
     * Проверка существования токена (без удаления)
     */
    fun hasToken(token: String): Boolean {
        return commands.exists(token) > 0
    }

    /**
     * Получить версию кэша для категории персонажей
     */
    fun getCategoryVersion(category: String): String {
        val key = "characters:version:cat:$category"
        return commands.get(key) ?: "1".also { commands.set(key, "1") }
    }

    /**
     * Инкрементировать версию категории (инвалидация кэша)
     */
    fun incrementCategoryVersion(category: String) {
        commands.incr("characters:version:cat:$category")
    }

    /**
     * Инвалидировать кэш для конкретной категории, а также общих категорий all и new
     */
    fun invalidateCategoryCache(category: String) {
        try {
            incrementCategoryVersion(category)
            if (category != "all") incrementCategoryVersion("all")
            if (category != "new") incrementCategoryVersion("new")
            logger.debug { "[RedisService] Invalidated character cache for category: $category" }
        } catch (e: Exception) {
            logger.warn { "[RedisService] Cache invalidation error: ${e.message}" }
        }
    }

    /**
     * Кэшировать пагинированный список персонажей
     */
    fun cachePaginatedResponse(
        category: String,
        version: String,
        page: Int,
        limit: Int,
        response: PaginatedResponse<CharacterPreviewResponse>
    ) {
        val key = "characters:cache:cat:$category:v:$version:page:$page:limit:$limit"
        try {
            val jsonStr = json.encodeToString(
                PaginatedResponse.serializer(CharacterPreviewResponse.serializer()),
                response
            )
            commands.setex(key, 300, jsonStr) // 5 минут TTL
        } catch (e: Exception) {
            logger.warn { "[RedisService] Error caching characters list: ${e.message}" }
        }
    }

    /**
     * Получить пагинированный список персонажей из кэша
     */
    fun getCachedPaginatedResponse(
        category: String,
        version: String,
        page: Int,
        limit: Int
    ): PaginatedResponse<CharacterPreviewResponse>? {
        val key = "characters:cache:cat:$category:v:$version:page:$page:limit:$limit"
        val jsonStr = commands.get(key) ?: return null
        return try {
            json.decodeFromString(
                PaginatedResponse.serializer(CharacterPreviewResponse.serializer()),
                jsonStr
            )
        } catch (e: Exception) {
            logger.warn { "[RedisService] Error deserializing characters cache: ${e.message}" }
            null
        }
    }

    /**
     * Получить версию глобального кэша баз знаний (lorebooks)
     */
     fun getLorebooksGlobalVersion(): String {
         val key = "lorebooks:version:global"
         return commands.get(key) ?: "1".also { commands.set(key, "1") }
     }

     /**
      * Инкрементировать версию глобального кэша баз знаний
      */
     fun incrementLorebooksVersion() {
         commands.incr("lorebooks:version:global")
     }

     /**
      * Инвалидировать кэш публичных баз знаний
      */
     fun invalidateLorebooksCache() {
         try {
             incrementLorebooksVersion()
             logger.debug { "[RedisService] Invalidated lorebooks cache" }
         } catch (e: Exception) {
             logger.warn { "[RedisService] Error invalidating lorebooks cache: ${e.message}" }
         }
     }

     /**
      * Кэшировать пагинированный список баз знаний
      */
     fun cachePaginatedLorebooks(
         query: String?,
         version: String,
         page: Int,
         limit: Int,
         response: PaginatedResponse<UserLorebookSummaryDto>
     ) {
         val qKey = query?.trim()?.lowercase() ?: "all"
         val key = "lorebooks:cache:global:v:$version:q:$qKey:p:$page:l:$limit"
         try {
             val jsonStr = json.encodeToString(
                 PaginatedResponse.serializer(UserLorebookSummaryDto.serializer()),
                 response
             )
             commands.setex(key, 300, jsonStr) // 5 минут TTL
         } catch (e: Exception) {
             logger.warn { "[RedisService] Error caching lorebooks: ${e.message}" }
         }
     }

     /**
      * Получить пагинированный список баз знаний из кэша
      */
     fun getCachedPaginatedLorebooks(
         query: String?,
         version: String,
         page: Int,
         limit: Int
     ): PaginatedResponse<UserLorebookSummaryDto>? {
         val qKey = query?.trim()?.lowercase() ?: "all"
         val key = "lorebooks:cache:global:v:$version:q:$qKey:p:$page:l:$limit"
         val jsonStr = commands.get(key) ?: return null
         return try {
             json.decodeFromString(
                 PaginatedResponse.serializer(UserLorebookSummaryDto.serializer()),
                 jsonStr
             )
         } catch (e: Exception) {
             logger.warn { "[RedisService] Error deserializing lorebooks cache: ${e.message}" }
             null
         }
     }

    /**
     * Закрыть соединение (при остановке приложения)
     */
    fun close() {
        if (connectionLazy.isInitialized()) {
            connection.close()
        }
        if (clientLazy.isInitialized()) {
            client.shutdown()
        }
    }
}
