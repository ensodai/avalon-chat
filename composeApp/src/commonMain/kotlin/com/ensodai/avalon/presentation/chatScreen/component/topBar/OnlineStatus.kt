package com.ensodai.avalon.presentation.chatScreen.component.topBar

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.offline
import com.ensodai.avalon.generated.resources.online
import com.ensodai.avalon.presentation.commonComponents.BaseItemScaleAnimation
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnlineStatus(onlineStatus: Boolean) {
    val colors = AvalonTheme.colors

    Box {
        BaseItemScaleAnimation(onlineStatus) {
            Text(
                text = stringResource(Res.string.online),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.onlineStatus
            )
        }
        BaseItemScaleAnimation(!onlineStatus) {
            Text(
                modifier = Modifier.alpha(0.6f),
                text = stringResource(Res.string.offline),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}
