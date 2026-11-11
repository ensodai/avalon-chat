package com.ensodai.avalon.data.db.cache

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.ensodai.shared.avalon.category.models.*

@Entity(
    tableName = "server_categories",
    indices = [Index(value = ["category_code"], unique = true)]
)
data class ServerCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(name = "category_code")
    val categoryCode: String,
    @ColumnInfo(name = "localized_name")
    val localizedName: String,
    @ColumnInfo(name = "count")
    val count: Int = 0
)

fun List<ServerCategoryResponse>.toServerCategoryEntities(locale: String): List<ServerCategoryEntity> =
    map {
        ServerCategoryEntity(
            id = it.id,
            categoryCode = it.categoryCode,
            localizedName = it.localizedNames[locale] ?: it.localizedNames["en"] ?: it.categoryCode,
            count = it.count ?: 0
        )
    }


fun ServerCategoryResponse.toServerCategoryEntity(localeCode: String): ServerCategoryEntity {
    val localizedName = this.localizedNames[localeCode] ?: this.localizedNames["en"] ?: ""
    return ServerCategoryEntity(
        id = this.id,
        categoryCode = this.categoryCode,
        localizedName = localizedName,
        count = this.count ?: 0
    )
}
