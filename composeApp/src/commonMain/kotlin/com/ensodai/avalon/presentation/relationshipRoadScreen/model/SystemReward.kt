package com.ensodai.avalon.presentation.relationshipRoadScreen.model

import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.StringResource

data class SystemReward(
    val title: StringResource,
    val description: StringResource,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun getSystemRewardForLevel(level: Int): SystemReward? {
    return when (level) {
        5 -> SystemReward(
            title = Res.string.reward_level_5_title,
            description = Res.string.reward_level_5_desc,
            icon = IconsManager.Chat
        )
        7 -> SystemReward(
            title = Res.string.reward_level_7_title,
            description = Res.string.reward_level_7_desc,
            icon = IconsManager.Memory
        )
        10 -> SystemReward(
            title = Res.string.reward_level_10_title,
            description = Res.string.reward_level_10_desc,
            icon = IconsManager.Bolt
        )
        15 -> SystemReward(
            title = Res.string.reward_level_15_title,
            description = Res.string.reward_level_15_desc,
            icon = IconsManager.Psychology
        )
        25 -> SystemReward(
            title = Res.string.reward_level_25_title,
            description = Res.string.reward_level_25_desc,
            icon = IconsManager.Bolt
        )
        30 -> SystemReward(
            title = Res.string.reward_level_30_title,
            description = Res.string.reward_level_30_desc,
            icon = IconsManager.Psychology
        )
        40 -> SystemReward(
            title = Res.string.reward_level_40_title,
            description = Res.string.reward_level_40_desc,
            icon = IconsManager.Memory
        )
        50 -> SystemReward(
            title = Res.string.reward_level_50_title,
            description = Res.string.reward_level_50_desc,
            icon = IconsManager.Bolt
        )
        55 -> SystemReward(
            title = Res.string.reward_level_55_title,
            description = Res.string.reward_level_55_desc,
            icon = IconsManager.Memory
        )
        65 -> SystemReward(
            title = Res.string.reward_level_65_title,
            description = Res.string.reward_level_65_desc,
            icon = IconsManager.Memory
        )
        else -> null
    }
}
