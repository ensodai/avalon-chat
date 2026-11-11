package com.ensodai.shared.avalon.category.models

import kotlinx.serialization.Serializable

@Serializable
data class SelectableCategoryResponse(
    val id: Int? = null,
    val categoryCode: String,
    val localizedNames: Map<String, String>,
)

@Serializable
data class ServerCategoryResponse(
    val id: Int? = null,
    val categoryCode: String,
    val localizedNames: Map<String, String>,
    val count: Int? = null
)

@Serializable
data class CombinedCategoriesResponse(
    val selectableCategories: List<SelectableCategoryResponse>,
    val serverCategories: List<ServerCategoryResponse>
)
