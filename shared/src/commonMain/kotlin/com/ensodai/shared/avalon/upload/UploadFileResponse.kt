package com.ensodai.shared.avalon.upload

import kotlinx.serialization.Serializable

@Serializable
data class UploadFileResponse(
    val token: String,
    val imageUrl: String,
    val width: Int,
    val height: Int,
    val thumbHash: String,
    val sizeBytes: Long
)
