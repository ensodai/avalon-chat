package com.ensodai.avalon.domain.models

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid

@Immutable
data class PersonaInfo(
    val id: Uuid,
    val name: String,
    val description: String,
    val isSelected: Boolean
)
