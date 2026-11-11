package app.data.repository

import app.data.db.tables.*
import app.utils.dbQuery
import com.ensodai.shared.avalon.category.models.SelectableCategoryResponse
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.koin.core.annotation.Single

@Single
class CategoriesRepository {

    /**
     * Обновляет или вставляет (upsert) список категорий для выбора пользователем.
     */
    suspend fun upsertSelectableCategories(categories: List<CategoryDto>) {
        dbQuery {
            categories.forEach { categoryDto ->
                val existingCategory = Category.find { Categories.categoryCode eq categoryDto.categoryCode }.singleOrNull()

                if (existingCategory != null) {
                    existingCategory.apply {
                        localizedNames = Json.encodeToString(categoryDto.localizedNames)
                        sortOrder = categoryDto.sortOrder
                        isSpecial = categoryDto.isSpecial
                    }
                } else {
                    Category.new {
                        categoryCode = categoryDto.categoryCode
                        localizedNames = Json.encodeToString(categoryDto.localizedNames)
                        sortOrder = categoryDto.sortOrder
                        isSpecial = categoryDto.isSpecial
                    }
                }
            }
        }
    }

    /**
     * Полностью перезаписывает список серверных категорий.
     */
    suspend fun replaceServerCategories(categories: List<CategoryDto>) {
        dbQuery {
            ServerCategories.deleteAll()

            ServerCategories.batchInsert(categories) { category ->
                this[ServerCategories.categoryCode] = category.categoryCode
                this[ServerCategories.localizedNames] = Json.encodeToString(category.localizedNames)
                this[ServerCategories.sortOrder] = category.sortOrder
                this[ServerCategories.isSpecial] = category.isSpecial
            }
        }
    }

    /**
     * Получает все категории для выбора в формате KMP DTO.
     */
    suspend fun getSelectableCategories(): List<SelectableCategoryResponse> {
        return dbQuery {
            Category.all().map { category ->
                SelectableCategoryResponse(
                    id = category.id.value,
                    categoryCode = category.categoryCode,
                    localizedNames = Json.decodeFromString(category.localizedNames)
                )
            }
        }
    }

    /**
     * Получает количество категорий для выбора.
     */
    suspend fun getSelectableCategoriesCount(): Long {
        return dbQuery {
            Category.all().count()
        }
    }

    /**
     * Получает количество серверных категорий.
     */
    suspend fun getServerCategoriesCount(): Long {
        return dbQuery {
            ServerCategory.all().count()
        }
    }
}
