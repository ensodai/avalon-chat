package com.ensodai.avalon.domain.model

/**
 * Platform-independent metadata for the application.
 * In KMP, this would be an 'expect' class or interface.
 */
data class PlatformMetadata(
    val versionName: String,
    val versionCode: Long,
    val platform: String = "Android" // Default for now
)
