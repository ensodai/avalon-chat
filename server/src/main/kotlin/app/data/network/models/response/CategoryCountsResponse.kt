package app.data.network.models.response

import kotlinx.serialization.Serializable

@Serializable
data class CategoryCountsResponse(
    val allCount: Int,
    val newCount: Int
)