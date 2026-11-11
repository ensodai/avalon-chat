package app.data.network.models.response

import kotlinx.serialization.Serializable

@Serializable
data class SelectableCategoryResponse(
    val id: Int? = null,
    val categoryCode: String,
    val localizedNames: Map<String, String>,
)