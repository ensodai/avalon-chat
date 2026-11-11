package app.data.network.models.response

import kotlinx.serialization.Serializable

@Serializable
data class ServerCategoryResponse(
    val id: Int? = null,
    val categoryCode: String,
    val localizedNames: Map<String, String>,
    val count: Int? = null
)