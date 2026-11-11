package app.data.db.tables.gifts

import app.core.BaseUuidTable
import app.data.db.tables.users.AppUsers
import org.jetbrains.exposed.v1.core.ReferenceOption

object GiftPacks : BaseUuidTable("gift_packs") {
    val authorId = reference("author_id", AppUsers, onDelete = ReferenceOption.SET_NULL).nullable()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val coverUrl = varchar("cover_url", 512)
    val isPublic = bool("is_public").default(false)
    val price = integer("price").default(0)
}
