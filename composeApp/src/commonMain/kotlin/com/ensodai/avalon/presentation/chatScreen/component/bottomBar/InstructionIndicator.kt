package com.ensodai.avalon.presentation.chatScreen.component.bottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.IconsManager

@Composable
fun InstructionIndicator(
    modifier: Modifier = Modifier,
    onRemove: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(com.ensodai.avalon.theme.AvalonTheme.colors.surfaceHigh)
            .padding(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            // Instruction Icon
            Icon(
                modifier = Modifier
                    .size(20.dp)
                    .padding(2.dp),
                imageVector = IconsManager.SystemInstruction,
                contentDescription = "Instruction Active",
                tint = com.ensodai.avalon.theme.AvalonTheme.colors.textMain
            )

            // Remove Button (Cross)
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove)
                    .padding(2.dp)
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = IconsManager.Close,
                    contentDescription = "Remove Instruction",
                    tint = com.ensodai.avalon.theme.AvalonTheme.colors.textMain
                )
            }
        }
    }
}