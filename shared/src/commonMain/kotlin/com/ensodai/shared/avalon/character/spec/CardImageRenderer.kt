package com.ensodai.shared.avalon.character.spec

interface CardImageRenderer {
    suspend fun renderCardToPng(
        characterName: String,
        nickname: String?,
        creatorNick: String?,
        shortDescription: String,
        tags: List<String>,
        avatarBytes: ByteArray?,
        hasLorebook: Boolean,
        versionText: String,
        specVersion: String,
        tokenCount: Int,
        altGreetingsCount: Int,
        customization: AvalonCardCustomization
    ): ByteArray
}
