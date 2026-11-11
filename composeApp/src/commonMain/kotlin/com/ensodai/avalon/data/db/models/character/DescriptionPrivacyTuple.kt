package com.ensodai.avalon.data.db.models.character

import androidx.room3.ColumnInfo

data class DescriptionPrivacyTuple(
    @ColumnInfo(name = "full_description")
    val fullDescription: String?,
    @ColumnInfo(name = "is_public_full_description")
    val isPublicFullDescription: Boolean
)