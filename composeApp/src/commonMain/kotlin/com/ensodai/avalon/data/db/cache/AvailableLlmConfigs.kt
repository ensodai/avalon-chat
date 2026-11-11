package com.ensodai.avalon.data.db.cache

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "llm_configs")
data class AvailableLlmConfigs(
    @PrimaryKey
    @ColumnInfo(name = "code_name")
    val codeName: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "price")
    val price: Int
)
