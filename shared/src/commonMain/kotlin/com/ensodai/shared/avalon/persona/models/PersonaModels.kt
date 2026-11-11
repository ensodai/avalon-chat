@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.shared.avalon.persona.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class PersonaResponse(
    @Contextual
    val id: Uuid,
    val name: String,
    val description: String,
    @Contextual
    val createdAt: Instant
)

@Serializable
data class PersonaCreateRequest(
    val name: String,
    val description: String
)

@Serializable
data class PersonaUpdateRequest(
    @Contextual
    val personaId: Uuid,
    val name: String?,
    val description: String?
)

@Serializable
data class SelectPersonaRequest(@Contextual val personaId: Uuid?)
