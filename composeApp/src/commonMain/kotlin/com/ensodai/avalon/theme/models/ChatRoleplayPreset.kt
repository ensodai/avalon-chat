package com.ensodai.avalon.theme.models

enum class ChatRoleplayPreset(val storageKey: String) {
    STANDARD_BUBBLE("standard_bubble"),
    ROLEPLAY_CLASSIC("roleplay_classic"),
    VISUAL_NOVEL("visual_novel"),
    CYBERPUNK_LOG("cyberpunk_log"),
    CINEMATIC_SCRIPT("cinematic_script"),
    RPG_BOARD_GAME("rpg_board_game"),
    MINIMAL_CARD("minimal_card"),
    INTIMATE_GLOW("intimate_glow");

    companion object {
        fun fromStorage(key: String): ChatRoleplayPreset {
            return entries.find { it.storageKey == key } ?: STANDARD_BUBBLE
        }
    }
}
