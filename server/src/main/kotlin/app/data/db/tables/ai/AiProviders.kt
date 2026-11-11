package app.data.db.tables.ai

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import com.ensodai.shared.avalon.user.models.AiProviderType
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid

object AiProviders : BaseUuidTable("ai_providers") {
    val providerId = enumerationByName<AiProviderType>("provider_id", 32).uniqueIndex("idx_ai_providers_provider_id")
    val displayName = varchar("display_name", 64)
    val baseUrl = varchar("base_url", 500)
    val isActive = bool("is_active").default(true)
    val isDefault = bool("is_default").default(false)
    val orderIndex = integer("order_index").default(0)
}

class AiProvider(id: EntityID<Uuid>) : BaseUuidEntity(id, AiProviders) {
    companion object : BaseUuidEntityClass<AiProvider>(AiProviders)

    var providerId by AiProviders.providerId
    var displayName by AiProviders.displayName
    var baseUrl by AiProviders.baseUrl
    var isActive by AiProviders.isActive
    var isDefault by AiProviders.isDefault
    var orderIndex by AiProviders.orderIndex
}

data class AiProviderModel(
    val id: Uuid,
    val providerId: AiProviderType,
    val displayName: String,
    val baseUrl: String,
    val isActive: Boolean,
    val isDefault: Boolean,
    val orderIndex: Int
)
