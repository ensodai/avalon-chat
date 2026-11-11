package app.data.network.models.response

import kotlinx.serialization.Serializable

@Serializable
data class CombinedCategoriesResponse(
    val selectableCategories: List<SelectableCategoryResponse>,
    val serverCategories: List<ServerCategoryResponse>
)