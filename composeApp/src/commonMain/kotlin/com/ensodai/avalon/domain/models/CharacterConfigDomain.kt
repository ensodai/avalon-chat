package com.ensodai.avalon.domain.models

data class CharacterConfigDomain(
    val id: Int,
    val characterId: Int,
    val shortDescription: String,
    val fullDescription: String?,
)
