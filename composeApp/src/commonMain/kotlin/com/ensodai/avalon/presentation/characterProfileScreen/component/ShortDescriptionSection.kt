package com.ensodai.avalon.presentation.characterProfileScreen.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.description
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun ShortDescriptionSection(
    copyToClipboard: () -> Unit,
    shortDescription: String
) {
    val colors = AvalonTheme.colors
    GemCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onLongClick = { copyToClipboard() },
        enabled = true,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = IconsManager.Info,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(Res.string.description).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = shortDescription,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMain,
                lineHeight = 22.sp
            )
        }
    }
}
