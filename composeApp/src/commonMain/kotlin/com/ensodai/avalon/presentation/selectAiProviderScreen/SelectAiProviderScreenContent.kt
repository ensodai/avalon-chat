package com.ensodai.avalon.presentation.selectAiProviderScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.select_ai_provider_how_to_desc
import com.ensodai.avalon.generated.resources.select_ai_provider_how_to_title
import com.ensodai.avalon.generated.resources.select_ai_provider_screen_subtitle
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.navigation.AvalonSwipeLockEffect
import com.ensodai.avalon.navigation.avalonCustomSwipe
import com.ensodai.avalon.presentation.selectAiProviderScreen.action.SelectAiProviderScreenActions
import com.ensodai.avalon.presentation.selectAiProviderScreen.component.AiProviderCard
import com.ensodai.avalon.presentation.selectAiProviderScreen.component.TopBarSelectAiProviderScreen
import com.ensodai.avalon.presentation.selectAiProviderScreen.viewState.SelectAiProviderScreenViewState
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun SelectAiProviderScreenContent(
    state: SelectAiProviderScreenViewState,
    actions: SelectAiProviderScreenActions,
    onBack: () -> Unit
) {
    val colors = AvalonTheme.colors

    // Блокировка системного свайпа назад и кнопки back во время обновления провайдера
    AvalonSwipeLockEffect(locked = state.isUpdating)
    AvalonBackHandler(enabled = state.isUpdating) {
        // Блокируем системный Back во время сохранения
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .avalonCustomSwipe(enabled = !state.isUpdating) { dragDeltaX -> dragDeltaX > 0 },
        topBar = {
            TopBarSelectAiProviderScreen(
                modifier = Modifier.fillMaxWidth(),
                isBackEnabled = !state.isUpdating,
                onBackButtonClick = {
                    if (!state.isUpdating) onBack()
                }
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.textMain,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    bottom = innerPadding.calculateBottomPadding() + 36.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    // Intro subtitle
                    Text(
                        text = stringResource(Res.string.select_ai_provider_screen_subtitle),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp
                        ),
                        color = colors.textSecondary
                    )
                }

                item {
                    // Minimalist Instruction / Tutorial Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.overlaySurface.copy(alpha = 0.04f))
                            .border(1.dp, colors.strokeSubtle, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = IconsManager.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = colors.textSecondary
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.select_ai_provider_how_to_title),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.textMain
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = stringResource(Res.string.select_ai_provider_how_to_desc),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        lineHeight = 18.sp
                                    ),
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }

                items(state.providers, key = { it.id }) { provider ->
                    val isSelected = provider.id.equals(state.selectedProviderId, ignoreCase = true)
                    val isThisCardUpdating = provider.id.equals(state.updatingProviderId, ignoreCase = true)
                    val isEnabled = !state.isUpdating || isThisCardUpdating

                    AiProviderCard(
                        provider = provider,
                        isSelected = isSelected,
                        isUpdating = isThisCardUpdating,
                        isEnabled = isEnabled,
                        onSelect = {
                            actions.onSelectProvider(provider.id)
                        }
                    )
                }
            }
        }
    }
}
