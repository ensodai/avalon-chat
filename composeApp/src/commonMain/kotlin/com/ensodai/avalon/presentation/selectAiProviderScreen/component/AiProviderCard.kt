package com.ensodai.avalon.presentation.selectAiProviderScreen.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.select_ai_provider_get_key_btn
import com.ensodai.avalon.presentation.selectAiProviderScreen.viewState.AiProviderItemUi
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun AiProviderCard(
    provider: AiProviderItemUi,
    isSelected: Boolean,
    isUpdating: Boolean = false,
    isEnabled: Boolean = true,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val uriHandler = LocalUriHandler.current

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) colors.textMain else colors.strokeSubtle,
        animationSpec = tween(durationMillis = 200)
    )

    val animatedTitleColor by animateColorAsState(
        targetValue = if (isSelected) colors.textMain else colors.textSecondary,
        animationSpec = tween(durationMillis = 200)
    )

    val backgroundColor = colors.overlaySurface.copy(alpha = if (isSelected) 0.14f else 0.04f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isEnabled) 1f else 0.4f)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(backgroundColor)
            .then(
                if (isEnabled && !isUpdating) {
                    Modifier.clickable { onSelect() }
                } else {
                    Modifier
                }
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Title + Status/Badge/Spinner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = provider.displayName,
                        color = animatedTitleColor,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    if (provider.badgeText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = 1.dp,
                                    color = colors.strokeSubtle,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .background(colors.overlaySurface.copy(alpha = 0.08f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = provider.badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Trailing state: Spinner / Check / None
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.textMain,
                        strokeWidth = 2.dp
                    )
                } else if (isSelected) {
                    Icon(
                        imageVector = IconsManager.CheckCircle,
                        contentDescription = null,
                        tint = colors.textMain,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Description
            Text(
                text = provider.description,
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 20.sp
                )
            )

            // Link to get API Key (clean and understated)
            if (provider.keyUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                uriHandler.openUri(provider.keyUrl)
                            } catch (_: Exception) {}
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = IconsManager.Link,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.select_ai_provider_get_key_btn),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}
