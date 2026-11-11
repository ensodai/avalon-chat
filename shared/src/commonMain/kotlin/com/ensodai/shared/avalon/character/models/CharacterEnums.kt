package com.ensodai.shared.avalon.character.models

import kotlinx.serialization.Serializable

@Serializable
enum class CharacterVisibility {
    PUBLIC,
    PRIVATE,
    LINK_ONLY
}

@Serializable
enum class InteractionFormat {
    LITERARY_RPG,
    CHAT_SIMULATOR
}
