package app.data.repository

import app.data.db.tables.ServerCategories
import app.data.db.tables.ServerCategory
import app.data.db.tables.aiCharacter.AICharacters
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.category.models.ServerCategoryResponse
import app.utils.dbQuery
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.koin.core.annotation.Single

/**
 * Репозиторий для получения серверных категорий с агрегированными данными.
 */
@Single
class ServerCategoriesRepository {

    /**
     * Получает все серверные категории, отсортированные по `sortOrder`.
     *
     * Для каждой категории эффективно подсчитывается количество активных,
     * публичных персонажей в ней.
     *
     * @return Список DTO [ServerCategoryResponse] с полными данными.
     */
    suspend fun getServerCategories(): List<ServerCategoryResponse> {
        return dbQuery {
            // --- Шаг 1: Эффективно получаем количество персонажей для ВСЕХ категорий за 1 запрос ---
            val characterCountAlias = AICharacters.id.count().alias("character_count")

            val countsMap = AICharacters
                .select(AICharacters.category, characterCountAlias)
                .where { (AICharacters.visibility eq CharacterVisibility.PUBLIC) and (AICharacters.isBanned eq false) }
                .groupBy(AICharacters.category)
                .associate {
                    it[AICharacters.category] to it[characterCountAlias]
                }

            // --- Шаг 2: Получаем все категории в стиле DAO ---
            val categories = ServerCategory.all()
                .orderBy(ServerCategories.sortOrder to SortOrder.ASC)
                .toList()

            // --- Шаг 3: "Склеиваем" данные в финальный DTO ---
            categories.map { category ->
                val count = countsMap[category.categoryCode]

                ServerCategoryResponse(
                    id = category.id.value,
                    categoryCode = category.categoryCode,
                    localizedNames = Json.decodeFromString(category.localizedNames),
                    count = count?.toInt()
                )
            }
        }
    }
}
