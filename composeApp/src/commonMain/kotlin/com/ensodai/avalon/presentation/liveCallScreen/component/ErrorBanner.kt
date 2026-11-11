package com.ensodai.avalon.presentation.liveCallScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.livecall_error
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorBanner(error: String) {
    val colors = AvalonTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.error.copy(alpha = 0.15f))
            .padding(12.dp)
    ) {
        Text(
            text = stringResource(Res.string.livecall_error),
            style = MaterialTheme.typography.labelMedium,
            color = colors.error
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMain.copy(alpha = 0.78f)
        )
    }
}
