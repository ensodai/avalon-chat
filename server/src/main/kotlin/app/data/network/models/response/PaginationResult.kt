package app.data.network.models.response
 
import com.ensodai.shared.avalon.character.models.CharacterPreviewResponse
import com.ensodai.shared.avalon.character.models.PaginatedResponse
import com.ensodai.shared.avalon.character.models.CharacterVisibility as SharedCharacterVisibility

import app.data.db.tables.aiCharacter.AICharacterStats
import app.data.db.tables.aiCharacter.AICharacters
import app.cache.RedisService
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import app.data.db.tables.images.CharacterAvatars
import app.data.db.tables.users.AppUsers
import app.utils.dbQuery
import app.utils.toServerUrl
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.uuid.Uuid





fun getCurrentWeekRange(): Pair<Instant, Instant> {
    val zone = ZoneOffset.UTC

    val startOfWeek = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .atStartOfDay(zone)
        .toInstant()
        .let { Instant.fromEpochMilliseconds(it.toEpochMilli()) }

    val endOfWeek = startOfWeek.plus(Duration.parse("6d23h59m59s"))

    return startOfWeek to endOfWeek
}

/**
 * Получает пагинированный список персонажей с фильтрацией по категории и динамической сортировкой.
 *
 * Этот метод оптимизирован для выполнения за минимальное количество запросов к БД.
 * Он поддерживает специальные категории:
 * - "all": все персонажи.
 * - "new": персонажи, созданные на текущей неделе (сортируются по дате создания).
 * Все остальные категории фильтруют по точному совпадению и сортируются по популярности.
 *
 * @param page Номер страницы для пагинации (начиная с 1).
 * @param category Строковый идентификатор категории ("all", "new", или любая другая).
 * @param limit Количество элементов на странице.
 * @return [PaginationResult] с найденными [CharacterPreview] и информацией о пагинации.
 */
//suspend fun paginateCharacters(page: Int, category: String, limit: Int = 10): PaginationResult<CharacterPreview> {
//    return dbQuery {
//        val offset = (page - 1) * limit
//
//        // --- Шаг 1: Подготовка подзапроса и алиасов ---
//        val avatarAlias = CharacterAvatars.imagePath.min().alias("avatar_image")
        val thumbHashAlias = CharacterAvatars.thumbHash.max().alias("thumb_hash_alias")
//        val avatarSubquery = CharacterAvatars
//            .select(CharacterAvatars.characterPublicId, avatarAlias, thumbHashAlias)
//            .groupBy(CharacterAvatars.characterPublicId)
//            .alias("avatar_subquery")
//
//        val userNickname = AppUsers.nickName.alias("user_nickname")
//
//        // --- Шаг 2: Формирование динамического условия фильтрации ---
//        var finalCondition: Op<Boolean> = (AICharacters.isPublicCharacter eq true) and
//                (AICharacters.isBanned eq false) and
//                (AppUsers.deletedAt.isNull()) and
//                (AppUsers.isBanned eq false) and
//                (AppUsers.isShadowBanned eq false)
//
//        when (category) {
//            "new" -> {
//                val (startOfWeek, endOfWeek) = getCurrentWeekRange() // Предполагается, что эта функция у вас есть
//                finalCondition = finalCondition and
//                        ((AICharacters.createdAt greaterEq startOfWeek) and (AICharacters.createdAt lessEq endOfWeek))
//            }
//            "all" -> {
//                // Никаких доп. условий
//            }
//            else -> {
//                finalCondition = finalCondition and (AICharacters.category eq category)
//            }
//        }
//
//        // --- Шаг 3: Сборка JOIN'ов ---
//        val joinedTables = AICharacters
//            .join(AppUsers, JoinType.INNER, onColumn = AICharacters.owner, otherColumn = AppUsers.id)
//            .join(avatarSubquery, JoinType.LEFT, onColumn = AICharacters.id, otherColumn = avatarSubquery[CharacterAvatars.characterPublicId])
//
//        val columnsToSelect = listOf(
//            AICharacters.id,
//            userNickname,
//            avatarSubquery[avatarAlias],
//            AICharacters.characterName,
//            AICharacters.shortDescription,
//            AICharacters.messageCount
//        )
//
//        // --- Шаг 4: Построение базового запроса ---
//        val baseQuery = joinedTables
//            .select(columnsToSelect)
//            .where { finalCondition }
//
//        // --- Шаг 5: Применение динамической сортировки ---
//        val sortedQuery = when (category) {
//            "new" -> baseQuery.orderBy(AICharacters.createdAt to SortOrder.DESC)
//            else -> baseQuery.orderBy(AICharacters.messageCount to SortOrder.DESC)
//        }
//
//        // --- Шаг 6: Получение пагинированного списка и общего количества ---
//        val itemsList = sortedQuery
//            .limit(limit)
//            .offset(offset.toLong())
//            .map { row ->
//                CharacterPreview(
//                    publicId = row[AICharacters.id].value.toString(),
//                    owner = row[userNickname] ?: "Unknown",
//                    avatar = row[avatarSubquery[avatarAlias]]?.toServerUrl(),
//                    thumbHash = row.getOrNull(avatarSubquery[thumbHashAlias]),
//                    characterName = row[AICharacters.characterName],
//                    shortDescription = row[AICharacters.shortDescription],
//                    messageCountRating = row[AICharacters.messageCount]
//                )
//            }
//
//        val totalItems = joinedTables
//            .select(AICharacters.id)
//            .where { finalCondition }
//            .count()
//
//        // --- Шаг 7: Формирование финального результата ---
//        val totalPages = if (limit > 0) ceil(totalItems.toDouble() / limit).toInt() else 0
//
//        PaginationResult(
//            page = page,
//            totalPages = totalPages,
//            items = itemsList,
//            limit = limit
//        )
//    }
//}

suspend fun paginateCharacters(page: Int, category: String, limit: Int = 10): PaginatedResponse<CharacterPreviewResponse> {
    val version = RedisService.getCategoryVersion(category)
    val cachedResponse = RedisService.getCachedPaginatedResponse(category, version, page, limit)
    if (cachedResponse != null) {
        return cachedResponse
    }

    return dbQuery {
        val offset = (page - 1) * limit

        // --- Шаг 1: Подготовка подзапроса для аватаров ---
        val avatarAlias = CharacterAvatars.imagePath.min().alias("avatar_image")
        val thumbHashAlias = CharacterAvatars.thumbHash.max().alias("thumb_hash_alias")
        val avatarSubquery = CharacterAvatars
            .select(CharacterAvatars.characterPublicId, avatarAlias, thumbHashAlias)
            .groupBy(CharacterAvatars.characterPublicId)
            .alias("avatar_subquery")

        // --- Шаг 2: Условия (оставляем как было) ---
        var finalCondition: Op<Boolean> = (AICharacters.visibility eq CharacterVisibility.PUBLIC) and
                (AICharacters.isBanned eq false) and
                (AppUsers.deletedAt.isNull()) and
                (AppUsers.isBanned eq false) and
                (AppUsers.isShadowBanned eq false)

        when (category) {
            "new" -> {
                val (startOfWeek, endOfWeek) = getCurrentWeekRange()
                finalCondition = finalCondition and
                        ((AICharacters.createdAt greaterEq startOfWeek) and (AICharacters.createdAt lessEq endOfWeek))
            }
            "all" -> { }
            else -> {
                finalCondition = finalCondition and (AICharacters.category eq category)
            }
        }

        // --- Шаг 3: Джойны ---
        val joinedTables = AICharacters
            .join(AppUsers, JoinType.INNER, onColumn = AICharacters.owner, otherColumn = AppUsers.id)
            .join(AICharacterStats, JoinType.LEFT, onColumn = AICharacters.id, otherColumn = AICharacterStats.characterId)
            .join(avatarSubquery, JoinType.LEFT, onColumn = AICharacters.id, otherColumn = avatarSubquery[CharacterAvatars.characterPublicId])

        // --- Шаг 4: Колонки ---
        val columnsToSelect = listOf(
            AICharacters.id,
            AppUsers.nickName,
            avatarSubquery[avatarAlias],
            avatarSubquery[thumbHashAlias],
            AICharacters.characterName,
            AICharacters.shortDescription,
            AICharacterStats.messageCount,
            AICharacterStats.popularityScore,
            AICharacters.visibility,
            AICharacters.updatedAt,
            AppUsers.deletedAt,
            AppUsers.isShadowBanned
        )

        val baseQuery = joinedTables
            .select(columnsToSelect)
            .where { finalCondition }

        // --- Шаг 5: СОРТИРОВКА (ИСПРАВЛЕНИЕ) ---
        val sortedQuery = when (category) {
            "new" -> baseQuery.orderBy(AICharacters.createdAt to SortOrder.DESC)
            else -> baseQuery.orderBy(AICharacterStats.popularityScore to SortOrder.DESC_NULLS_LAST)
        }

        // --- Шаг 6: Маппинг ---
        val itemsList = sortedQuery
            .limit(limit)
            .offset(offset.toLong())
            .map { row ->
                val msgCount = row.getOrNull(AICharacterStats.messageCount) ?: 0L

                CharacterPreviewResponse(
                    id = row[AICharacters.id].value,
                    ownerNick = row[AppUsers.nickName], // Берем напрямую из таблицы
                    avatarUrl = row[avatarSubquery[avatarAlias]]?.toServerUrl(),
                    thumbHash = row.getOrNull(avatarSubquery[thumbHashAlias]),
                    characterName = row[AICharacters.characterName],
                    shortDescription = row[AICharacters.shortDescription],
                    messageCountRating = msgCount.toInt(),
                    visibility = row[AICharacters.visibility],
                    updatedAt = row[AICharacters.updatedAt]
                )
            }

        // --- Шаг 7: Подсчет (ИСПРАВЛЕНИЕ) ---
        val countJoin = AICharacters
            .join(AppUsers, JoinType.INNER, onColumn = AICharacters.owner, otherColumn = AppUsers.id)

        val totalItems = countJoin
            .select(AICharacters.id)
            .where { finalCondition }
            .count()

        val totalPages = if (limit > 0) ceil(totalItems.toDouble() / limit).toInt() else 0

        val response = PaginatedResponse(
            page = page,
            totalPages = totalPages,
            items = itemsList,
            limit = limit
        )

        // Сохраняем в кэш Redis
        RedisService.cachePaginatedResponse(category, version, page, limit, response)

        response
    }
}