package com.ensodai.avalon.theme.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.AvalonThemePresets

/**
 * Предустановленная цветовая схема исходящего баббла.
 * Входящий баббл всегда берётся из активной темы.
 */
@Immutable
data class BubbleColorScheme(
    val id: String,
    val label: String,
    val outgoingColor: Color
)

/**
 * Наборы предустановленных цветовых схем исходящих бабблов.
 * У каждой темы свой список, чтобы светлые и тёмные пресеты выглядели уместно.
 */
object BubbleColorSchemes {

    private val darkPresets = listOf(
        BubbleColorScheme(id = "electric_blue", label = "Electric", outgoingColor = Color(0xFF3D8BFF)),
        BubbleColorScheme(id = "cyber_teal", label = "Cyber", outgoingColor = Color(0xFF00A99D)),
        BubbleColorScheme(id = "emerald_glow", label = "Emerald", outgoingColor = Color(0xFF2FBF71)),
        BubbleColorScheme(id = "ember", label = "Ember", outgoingColor = Color(0xFFFF7043)),
        BubbleColorScheme(id = "berry", label = "Berry", outgoingColor = Color(0xFFE6528F)),
        BubbleColorScheme(id = "violet", label = "Violet", outgoingColor = Color(0xFF8B5CF6))
    )

    private val lightPresets = listOf(
        BubbleColorScheme(id = "sky", label = "Sky", outgoingColor = Color(0xFF2F80ED)),
        BubbleColorScheme(id = "mint", label = "Mint", outgoingColor = Color(0xFF1F9D8B)),
        BubbleColorScheme(id = "meadow", label = "Meadow", outgoingColor = Color(0xFF43A047)),
        BubbleColorScheme(id = "coral", label = "Coral", outgoingColor = Color(0xFFF06C5B)),
        BubbleColorScheme(id = "rose", label = "Rose", outgoingColor = Color(0xFFD95B85)),
        BubbleColorScheme(id = "gold", label = "Gold", outgoingColor = Color(0xFFC7921E))
    )

    private val presetsByTheme: Map<AvalonThemePreset, List<BubbleColorScheme>> = mapOf(
        AvalonThemePreset.TechNoir to darkPresets,
        AvalonThemePreset.OceanDepth to listOf(
            BubbleColorScheme(id = "neon_wave", label = "Wave", outgoingColor = Color(0xFF1E88E5)),
            BubbleColorScheme(id = "reef", label = "Reef", outgoingColor = Color(0xFF009688)),
            BubbleColorScheme(id = "sea_glass", label = "Glass", outgoingColor = Color(0xFF00ACC1)),
            BubbleColorScheme(id = "kelp", label = "Kelp", outgoingColor = Color(0xFF43A047)),
            BubbleColorScheme(id = "sunset_tide", label = "Tide", outgoingColor = Color(0xFFFF7043)),
            BubbleColorScheme(id = "orchid_current", label = "Orchid", outgoingColor = Color(0xFF7E57C2))
        ),
        AvalonThemePreset.Gruvbox to listOf(
            BubbleColorScheme(id = "gruv_aqua", label = "Aqua", outgoingColor = Color(0xFF458588)),
            BubbleColorScheme(id = "gruv_green", label = "Forest", outgoingColor = Color(0xFF689D6A)),
            BubbleColorScheme(id = "gruv_yellow", label = "Honey", outgoingColor = Color(0xFFD79921)),
            BubbleColorScheme(id = "gruv_orange", label = "Spice", outgoingColor = Color(0xFFFE8019)),
            BubbleColorScheme(id = "gruv_red", label = "Brick", outgoingColor = Color(0xFFCC241D)),
            BubbleColorScheme(id = "gruv_purple", label = "Plum", outgoingColor = Color(0xFFB16286))
        ),
        AvalonThemePreset.MidnightPurple to listOf(
            BubbleColorScheme(id = "iris", label = "Iris", outgoingColor = Color(0xFF7C4DFF)),
            BubbleColorScheme(id = "ultraviolet", label = "Ultra", outgoingColor = Color(0xFF5E35B1)),
            BubbleColorScheme(id = "magenta", label = "Magenta", outgoingColor = Color(0xFFD81B60)),
            BubbleColorScheme(id = "indigo", label = "Indigo", outgoingColor = Color(0xFF3949AB)),
            BubbleColorScheme(id = "amethyst", label = "Amethyst", outgoingColor = Color(0xFF8E24AA)),
            BubbleColorScheme(id = "plasma", label = "Plasma", outgoingColor = Color(0xFFFF7043))
        ),
        AvalonThemePreset.CrimsonNight to listOf(
            BubbleColorScheme(id = "crimson", label = "Crimson", outgoingColor = Color(0xFFC62846)),
            BubbleColorScheme(id = "scarlet", label = "Scarlet", outgoingColor = Color(0xFFE53935)),
            BubbleColorScheme(id = "ruby", label = "Ruby", outgoingColor = Color(0xFFD81B60)),
            BubbleColorScheme(id = "copper", label = "Copper", outgoingColor = Color(0xFFFB8C00)),
            BubbleColorScheme(id = "wine", label = "Wine", outgoingColor = Color(0xFFAD1457)),
            BubbleColorScheme(id = "ember_red", label = "Ember", outgoingColor = Color(0xFFFF7043))
        ),
        AvalonThemePreset.PaperLight to lightPresets,
        AvalonThemePreset.RoseDawn to listOf(
            BubbleColorScheme(id = "blush", label = "Blush", outgoingColor = Color(0xFFE57399)),
            BubbleColorScheme(id = "peach", label = "Peach", outgoingColor = Color(0xFFF08A6C)),
            BubbleColorScheme(id = "lilac", label = "Lilac", outgoingColor = Color(0xFF9C6ADE)),
            BubbleColorScheme(id = "sage", label = "Sage", outgoingColor = Color(0xFF6AA67C)),
            BubbleColorScheme(id = "dawn_blue", label = "Dawn", outgoingColor = Color(0xFF5C8DF6)),
            BubbleColorScheme(id = "champagne", label = "Champagne", outgoingColor = Color(0xFFD9A441))
        ),
        AvalonThemePreset.SketchPad to listOf(
            BubbleColorScheme(id = "ink", label = "Ink", outgoingColor = Color(0xFF546E7A)),
            BubbleColorScheme(id = "marker_blue", label = "Marker", outgoingColor = Color(0xFF4F6EF7)),
            BubbleColorScheme(id = "pencil_green", label = "Pine", outgoingColor = Color(0xFF5A8F63)),
            BubbleColorScheme(id = "charcoal_rose", label = "Rose", outgoingColor = Color(0xFFC85C7A)),
            BubbleColorScheme(id = "ochre", label = "Ochre", outgoingColor = Color(0xFFB98A2E)),
            BubbleColorScheme(id = "terracotta", label = "Clay", outgoingColor = Color(0xFFD97757))
        )
    )

    fun presetsForTheme(themePreset: AvalonThemePreset): List<BubbleColorScheme> {
        return presetsByTheme[themePreset].orEmpty()
    }

    fun matchSelectedScheme(
        themePreset: AvalonThemePreset,
        colors: AvalonColors
    ): BubbleColorScheme? {
        val baseOutgoingColor = AvalonThemePresets.resolve(themePreset).userBubble.primaryColor
        val currentOutgoingColor = colors.userBubble.primaryColor

        if (currentOutgoingColor == baseOutgoingColor) {
            return null
        }

        return presetsForTheme(themePreset).firstOrNull { it.outgoingColor == currentOutgoingColor }
    }
}
