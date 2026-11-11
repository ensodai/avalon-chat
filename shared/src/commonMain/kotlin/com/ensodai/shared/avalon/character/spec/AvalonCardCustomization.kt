package com.ensodai.shared.avalon.character.spec

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
enum class CardLayout {
    @SerialName("FULL_BLEED")
    FULL_BLEED,

    @SerialName("MONOLITH_FRAME")
    MONOLITH_FRAME,

    @SerialName("HEX_CORE")
    HEX_CORE
}

@Serializable
enum class CardAccentToken(val hexColor: String, val displayName: String) {
    @SerialName("CYAN")
    CYAN("#00E5FF", "Cyan"),

    @SerialName("INDIGO")
    INDIGO("#818CF8", "Indigo"),

    @SerialName("EMERALD")
    EMERALD("#10B981", "Emerald"),

    @SerialName("AMBER")
    AMBER("#F59E0B", "Amber"),

    @SerialName("SILVER")
    SILVER("#94A3B8", "Silver"),

    @SerialName("CRIMSON")
    CRIMSON("#F43F5E", "Crimson")
}

@Serializable
enum class CardBgPattern(val displayName: String) {
    @SerialName("MATTE_OBSIDIAN")
    MATTE_OBSIDIAN("Matte Obsidian"),

    @SerialName("HEX_GRID")
    HEX_GRID("Hex Grid"),

    @SerialName("SCANLINES")
    SCANLINES("Scanlines")
}

@Serializable
enum class CardFallbackGlyph(val glyphId: String, val displayName: String) {
    @SerialName("AI_CORE")
    AI_CORE("ai_core", "AI Core"),

    @SerialName("TERMINAL")
    TERMINAL("terminal", "Terminal"),

    @SerialName("BOOK")
    BOOK("book", "Lorebook"),

    @SerialName("SWORD")
    SWORD("sword", "Blade"),

    @SerialName("INITIALS")
    INITIALS("initials", "Initials")
}

@Serializable
data class AvalonCardCustomization(
    @SerialName("card_layout")
    val layout: CardLayout = CardLayout.MONOLITH_FRAME,
    @SerialName("accent_token")
    val accentToken: CardAccentToken = CardAccentToken.CYAN,
    @SerialName("bg_pattern")
    val bgPattern: CardBgPattern = CardBgPattern.MATTE_OBSIDIAN,
    @SerialName("fallback_glyph")
    val fallbackGlyph: CardFallbackGlyph = CardFallbackGlyph.AI_CORE,
    @SerialName("branding_text")
    val brandingText: String = "AVALON",
    @SerialName("show_telemetry")
    val showTelemetry: Boolean = true
) {
    companion object {
        const val EXTENSION_KEY = "avalon_avatars"

        fun fromExtensions(extensions: JsonObject?): AvalonCardCustomization {
            val avalonObj = extensions?.get(EXTENSION_KEY)?.jsonObject ?: return AvalonCardCustomization()
            return runCatching {
                characterCardJson.decodeFromJsonElement<AvalonCardCustomization>(avalonObj)
            }.getOrDefault(AvalonCardCustomization())
        }
    }

    fun toExtensionJson(): JsonObject {
        return characterCardJson.encodeToJsonElement(this).jsonObject
    }
}
