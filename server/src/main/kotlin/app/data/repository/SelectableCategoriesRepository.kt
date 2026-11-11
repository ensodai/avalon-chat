package app.data.repository

import app.data.db.tables.Categories
import app.data.db.tables.Category
import com.ensodai.shared.avalon.category.models.SelectableCategoryResponse
import app.utils.dbQuery
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.koin.core.annotation.Single

/**
 * Репозиторий для получения категорий, доступных для выбора пользователем.
 */
@Single
class SelectableCategoriesRepository {

    /**
     * Получает все категории, доступные для выбора, за исключением "all".
     *
     * Категории отсортированы по полю `sortOrder`.
     *
     * @return Список DTO [SelectableCategoryResponse].
     */
    suspend fun getSelectableCategories(): List<SelectableCategoryResponse> {
        return dbQuery {
            Category
                .find { Categories.categoryCode neq "all" }
                .orderBy(Categories.sortOrder to SortOrder.ASC)
                .map { it.toSelectableCategoryResponse() }
        }
    }

    /**
     * Получает только специальные категории.
     *
     * Категории отсортированы по полю `sortOrder`.
     *
     * @return Список DTO [SelectableCategoryResponse].
     */
    suspend fun getSpecialCategories(): List<SelectableCategoryResponse> {
        return dbQuery {
            Category
                .find { Categories.isSpecial eq true }
                .orderBy(Categories.sortOrder to SortOrder.ASC)
                .map { it.toSelectableCategoryResponse() }
        }
    }
}

/**
 * Вспомогательная extension-функция для преобразования DAO-сущности в DTO.
 */
private fun Category.toSelectableCategoryResponse(): SelectableCategoryResponse {
    return SelectableCategoryResponse(
        id = this.id.value,
        categoryCode = this.categoryCode,
        localizedNames = Json.decodeFromString(this.localizedNames),
    )
}
