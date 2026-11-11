package app.data.network.models.response.user

import app.data.db.tables.LlmConfig
import kotlinx.serialization.Serializable

@Serializable
data class LlmConfigResponse(
    val codeName: String,
    val displayName: String,
    val price: Int
)

fun LlmConfig.toSyncResponse(): LlmConfigResponse {
    return LlmConfigResponse(
        codeName = this.code,
        displayName = this.name,
        price = this.energyCost
    )
}