package com.ensodai.avalon.theme.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
enum class AvalonThemePreset(val storageKey: String) {
    TechNoir("tech_noir"),
    PaperLight("paper_light"),
    OceanDepth("ocean_depth"),
    RoseDawn("rose_dawn"),
    Gruvbox("gruvbox"),
    MidnightPurple("midnight_purple"),
    CrimsonNight("crimson_night"),
    SketchPad("sketch_pad");

    companion object {
        fun fromStorage(value: String?): AvalonThemePreset {
            return entries.firstOrNull { it.storageKey == value } ?: TechNoir
        }
    }
}

@Immutable
enum class BubbleFillType(val storageKey: String) {
    Solid("solid"),
    Gradient("gradient");

    companion object {
        fun fromStorage(value: String?): BubbleFillType {
            return entries.firstOrNull { it.storageKey == value } ?: Solid
        }
    }
}

@Immutable
enum class ChatBackgroundType(val storageKey: String) {
    Default("default"),
    Solid("solid"),
    Image("image"),
    Pattern("pattern");

    companion object {
        fun fromStorage(value: String?): ChatBackgroundType {
            return entries.firstOrNull { it.storageKey == value } ?: Default
        }
    }
}

@Immutable
enum class ChatPatternPreset(val storageKey: String) {
    Aurora("aurora"),
    DiagonalGrid("diagonal_grid");

    companion object {
        fun fromStorage(value: String?): ChatPatternPreset {
            return entries.firstOrNull { it.storageKey == value } ?: Aurora
        }
    }
}

@Immutable
enum class BubbleShapeType(val storageKey: String) {
    Tail("tail"),
    Rounded("rounded"),
    SoftCorner("soft_corner");

    companion object {
        fun fromStorage(value: String?): BubbleShapeType {
            return entries.firstOrNull { it.storageKey == value || it.name == value } ?: Tail
        }
    }
}

@Immutable
sealed class BubbleFill {
    abstract val primaryColor: Color

    data class Solid(val color: Color) : BubbleFill() {
        override val primaryColor: Color = color
    }

    data class Gradient(
        val startColor: Color,
        val endColor: Color
    ) : BubbleFill() {
        override val primaryColor: Color = startColor
    }
}

@Immutable
sealed class ChatBackgroundConfig {
    data object Default : ChatBackgroundConfig()
    data class Solid(val color: Color) : ChatBackgroundConfig()
    data class Image(
        val url: String,
        val blurRadius: Int = 0,
        val dimAmount: Float = 0f
    ) : ChatBackgroundConfig()
    data class Pattern(
        val preset: ChatPatternPreset,
        val baseColor: Color,
        val tintColor: Color,
        val intensity: Float = 0.18f
    ) : ChatBackgroundConfig()
}
