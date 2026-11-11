package com.ensodai.avalon.presentation.liveCallScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun SelectorButton(
    displayText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.cardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = displayText,
            color = colors.textMain,
            fontSize = 14.sp
        )
    }
}
