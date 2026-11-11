package app.data.db.tables

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

object ServerCategories : IntIdTable("server_categories") {
    val categoryCode = varchar("category_code", 255)
    val localizedNames = text("localized_names")
    val sortOrder = integer("sort_order").default(0)
    val isSpecial = bool("is_special").default(true)
}

class ServerCategory(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ServerCategory>(ServerCategories)

    var categoryCode: String by ServerCategories.categoryCode
    var localizedNames: String by ServerCategories.localizedNames
    var sortOrder: Int by ServerCategories.sortOrder
    var isSpecial: Boolean by ServerCategories.isSpecial
}
