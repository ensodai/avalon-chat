package com.ensodai.avalon.presentation.customizationScreen.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import org.jetbrains.compose.resources.stringResource

@Composable
fun PresetHeader(
    preset: ChatRoleplayPreset,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = getPresetTitle(preset),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = colors.textMain
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = getPresetDescription(preset),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun getPresetTitle(preset: ChatRoleplayPreset): String = when (preset) {
    ChatRoleplayPreset.STANDARD_BUBBLE -> stringResource(Res.string.chat_preset_standard_title)
    ChatRoleplayPreset.ROLEPLAY_CLASSIC -> stringResource(Res.string.chat_preset_classic_title)
    ChatRoleplayPreset.VISUAL_NOVEL -> stringResource(Res.string.chat_preset_novel_title)
    ChatRoleplayPreset.CYBERPUNK_LOG -> stringResource(Res.string.chat_preset_cyberpunk_title)
    ChatRoleplayPreset.CINEMATIC_SCRIPT -> stringResource(Res.string.chat_preset_cinematic_title)
    ChatRoleplayPreset.RPG_BOARD_GAME -> stringResource(Res.string.chat_preset_rpg_title)
    ChatRoleplayPreset.MINIMAL_CARD -> stringResource(Res.string.chat_preset_minimal_title)
    ChatRoleplayPreset.INTIMATE_GLOW -> stringResource(Res.string.chat_preset_intimate_title)
}

@Composable
private fun getPresetDescription(preset: ChatRoleplayPreset): String = when (preset) {
    ChatRoleplayPreset.STANDARD_BUBBLE -> stringResource(Res.string.chat_preset_standard_desc)
    ChatRoleplayPreset.ROLEPLAY_CLASSIC -> stringResource(Res.string.chat_preset_classic_desc)
    ChatRoleplayPreset.VISUAL_NOVEL -> stringResource(Res.string.chat_preset_novel_desc)
    ChatRoleplayPreset.CYBERPUNK_LOG -> stringResource(Res.string.chat_preset_cyberpunk_desc)
    ChatRoleplayPreset.CINEMATIC_SCRIPT -> stringResource(Res.string.chat_preset_cinematic_desc)
    ChatRoleplayPreset.RPG_BOARD_GAME -> stringResource(Res.string.chat_preset_rpg_desc)
    ChatRoleplayPreset.MINIMAL_CARD -> stringResource(Res.string.chat_preset_minimal_desc)
    ChatRoleplayPreset.INTIMATE_GLOW -> stringResource(Res.string.chat_preset_intimate_desc)
}
