package com.ensodai.avalon.presentation.userAiSettingsScreen.component

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.user.models.AiProviderResponse
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.user_ai_provider_desc_anthropic
import com.ensodai.avalon.generated.resources.user_ai_provider_desc_custom
import com.ensodai.avalon.generated.resources.user_ai_provider_desc_deepseek
import com.ensodai.avalon.generated.resources.user_ai_provider_desc_google
import com.ensodai.avalon.generated.resources.user_ai_provider_desc_groq
import com.ensodai.avalon.generated.resources.user_ai_provider_desc_openai
import com.ensodai.avalon.generated.resources.user_ai_provider_desc_openrouter
import com.ensodai.avalon.generated.resources.user_ai_provider_picker_desc
import com.ensodai.avalon.generated.resources.user_ai_provider_picker_title
import com.ensodai.avalon.generated.resources.user_ai_provider_recommended
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderPickerBottomSheet(
    availableProviders: List<AiProviderResponse>,
    selectedProviderId: String,
    onProviderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(Res.string.user_ai_provider_picker_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = stringResource(Res.string.user_ai_provider_picker_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                availableProviders.forEach { provider ->
                    val isSelected = provider.id.equals(selectedProviderId, ignoreCase = true)
                    ProviderSelectionCard(
                        provider = provider,
                        isSelected = isSelected,
                        onClick = { onProviderSelected(provider.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderSelectionCard(
    provider: AiProviderResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) colors.textMain else colors.strokeSubtle,
        animationSpec = tween(durationMillis = 200)
    )

    val animatedTitleColor by animateColorAsState(
        targetValue = if (isSelected) colors.textMain else colors.textSecondary,
        animationSpec = tween(durationMillis = 200)
    )

    val backgroundColor = colors.overlaySurface.copy(alpha = if (isSelected) 0.14f else 0.04f)

    val description = when (provider.id.lowercase()) {
        "openrouter" -> stringResource(Res.string.user_ai_provider_desc_openrouter)
        "openai" -> stringResource(Res.string.user_ai_provider_desc_openai)
        "anthropic" -> stringResource(Res.string.user_ai_provider_desc_anthropic)
        "google" -> stringResource(Res.string.user_ai_provider_desc_google)
        "deepseek" -> stringResource(Res.string.user_ai_provider_desc_deepseek)
        "groq" -> stringResource(Res.string.user_ai_provider_desc_groq)
        else -> stringResource(Res.string.user_ai_provider_desc_custom)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = provider.displayName,
                        color = animatedTitleColor,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    if (provider.isDefault) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = colors.neonAccent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.user_ai_provider_recommended),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = colors.neonAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) colors.textMain else colors.strokeSubtle,
                        shape = CircleShape
                    )
                    .background(if (isSelected) colors.textMain else colors.cardBackground),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = IconsManager.Check,
                        contentDescription = null,
                        tint = colors.background,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
