package com.ensodai.avalon.presentation.commonComponents.welcomCard

import androidx.compose.ui.graphics.Color

/**
 * Enum для хранения предопределенных цветов приложения.
 * Каждый цвет имеет строковый ключ для хранения на бэкенде и значение Color для UI.
 */
enum class CardColor(val key: String, val value: Color) {
    DEFAULT("default", Color.Gray),
    RED("red", Color(0xFFF44336)),
    PINK("pink", Color(0xFFE91E63)),
    PURPLE("purple", Color(0xFF9C27B0)),
    DEEP_PURPLE("deep_purple", Color(0xFF673AB7)),
    INDIGO("indigo", Color(0xFF3F51B5)),
    BLUE("blue", Color(0xFF2196F3)),
    LIGHT_BLUE("light_blue", Color(0xFF03A9F4)),
    CYAN("cyan", Color(0xFF00BCD4)),
    GREEN("green", Color(0xFF4CAF50)),
    YELLOW("yellow", Color(0xFFFFEB3B)),
    ORANGE("orange", Color(0xFFFF9800));

    companion object {
        fun fromKey(key: String?): CardColor {
            return entries.find { it.key == key } ?: DEFAULT
        }

        fun getSelectableColors(): List<CardColor> {
            return entries
        }
    }
}
