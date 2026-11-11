package com.ensodai.avalon.data.db.dto

sealed class CategoryItemDto {
    abstract val id: Int?
    abstract val categoryCode: String
    abstract val localizedName: String
}

data class ServerCategoryDto(
    override val id: Int?,
    override val categoryCode: String,
    override val localizedName: String,
    val isSpecial: Boolean = true,
    val count: Int
) : CategoryItemDto()

data class SelectableCategoryDto(
    override val id: Int?,
    override val categoryCode: String,
    override val localizedName: String,
    val isSpecial: Boolean = false,
) : CategoryItemDto()