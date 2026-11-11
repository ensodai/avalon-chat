package com.ensodai.avalon.data.db.models

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "main_rules")
data class MainRulesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "main_rules")
    val mainRules: String,
    @ColumnInfo(name = "default_gemini_model")
    val defaultGeminiModel: String,
    @ColumnInfo(name = "max_output_tokens")
    val maxOutputTokens: Int
)
