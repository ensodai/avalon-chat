package com.ensodai.avalon.presentation.mainScreen.component.chatSection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.pwa_install_app
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun PwaInstallBannerItem(
    modifier: Modifier = Modifier,
    onInstallClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AvalonTheme.colors.surfaceHigh)
            .clickable { onInstallClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.pwa_install_app),
                style = MaterialTheme.typography.bodyLarge,
                color = AvalonTheme.colors.textMain
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        IconButton(
            onClick = onDismissClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = IconsManager.Close,
                contentDescription = "Close",
                tint = AvalonTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
