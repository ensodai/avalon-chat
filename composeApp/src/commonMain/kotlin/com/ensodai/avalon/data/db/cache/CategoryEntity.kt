package com.ensodai.avalon.data.db.cache

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.ensodai.shared.avalon.category.models.*

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Int? = null,
    @ColumnInfo(name = "category_code")
    val categoryCode: String,
    @ColumnInfo(name = "localized_name")
    val localizedName: String
)

fun List<SelectableCategoryResponse>.toCategoryEntities(locale: String): List<CategoryEntity> =
    map {
        CategoryEntity(
            id = it.id,
            categoryCode = it.categoryCode,
            localizedName = it.localizedNames[locale] ?: it.localizedNames["en"] ?: it.categoryCode
        )
    }

