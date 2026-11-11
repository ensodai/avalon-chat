package com.ensodai.avalon.domain.sessionWorker


import kotlinx.serialization.Serializable

@Serializable
data class JwtPayload(
    val exp: Long,
    val sub: String? = null
)
