package com.ensodai.avalon.data.db.dto

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class PersonaDto(
    val id: Uuid,
    val name: String,
    val description: String,
    val createdAt: Instant
)