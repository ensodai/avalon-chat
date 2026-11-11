package app.data.db.tables

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

object Categories : IntIdTable("categories") {
    val categoryCode = varchar("category", 255)
    val localizedNames = text("localized_names")
    val sortOrder = integer("sort_order").default(0)
    val isSpecial = bool("is_special").default(false)
}

class Category(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Category>(Categories)

    var categoryCode: String by Categories.categoryCode
    var localizedNames: String by Categories.localizedNames
    var sortOrder: Int by Categories.sortOrder
    var isSpecial: Boolean by Categories.isSpecial
}

data class CategoryDto(
    val categoryCode: String,
    val localizedNames: Map<String, String>,
    val sortOrder: Int,
    val isSpecial: Boolean
)
