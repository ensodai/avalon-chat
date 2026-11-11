package app.data.db.tables.gifts

import com.ensodai.shared.avalon.gifts.models.GiftActionType
import com.ensodai.shared.avalon.gifts.models.GiftEffectType
import app.core.BaseUuidTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ReferenceOption

object Gifts : BaseUuidTable("gifts") {
    val packId = reference("pack_id", GiftPacks, onDelete = ReferenceOption.CASCADE)
    val fileUrl = varchar("file_url", 512)
    val price = integer("price").default(0)
    val actionType = enumerationByName("action_type", 50, GiftActionType::class)
    val giftName = varchar("gift_name", 255)
    val giftDescription = varchar("gift_description", 512)
    val effectType = enumerationByName("effect_type", 50, GiftEffectType::class).default(GiftEffectType.NONE)
}
