package app.data.db.tables.gifts

import app.core.BaseUuidTable
import app.data.db.tables.users.AppUsers
import org.jetbrains.exposed.v1.core.ReferenceOption

object UserSavedGiftPacks : BaseUuidTable("user_saved_gift_packs") {
    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val packId = reference("pack_id", GiftPacks, onDelete = ReferenceOption.CASCADE)
}
