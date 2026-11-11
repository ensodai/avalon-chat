package com.ensodai.avalon.presentation.customizationScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.customizationScreen.component.BubbleStyleSettings
import com.ensodai.avalon.presentation.customizationScreen.component.ChatPresetPreview
import com.ensodai.avalon.presentation.customizationScreen.component.PresetHeader
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.models.BubbleColorScheme
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPresetsScreenContent(
    viewState: ChatPresetsViewState,
    navigation: Navigation,
    onPresetSelected: (ChatRoleplayPreset) -> Unit,
    onFontSizeChanged: (Int) -> Unit,
    onShapeTypeChanged: (BubbleShapeType) -> Unit,
    onCornerRadiusChanged: (Int) -> Unit,
    onBubbleColorSchemeSelected: (BubbleColorScheme?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    var localFontSize by remember(viewState.bubbleFontSize) { mutableStateOf(viewState.bubbleFontSize) }
    var localShapeType by remember(viewState.bubbleShapeType) { mutableStateOf(viewState.bubbleShapeType) }
    var localCornerRadius by remember(viewState.bubbleCornerRadius) { mutableStateOf(viewState.bubbleCornerRadius) }

    val presets = ChatRoleplayPreset.entries

    val pagerState = rememberPagerState(
        initialPage = presets.indexOf(viewState.selectedPreset).coerceAtLeast(0)
    ) {
        presets.size
    }

    LaunchedEffect(viewState.selectedPreset) {
        val index = presets.indexOf(viewState.selectedPreset)
        if (index >= 0 && pagerState.currentPage != index) {
            pagerState.scrollToPage(index)
        }
    }

    Scaffold(
        topBar = {
            GemIslandTopBar(
                title = {
                    Text(
                        text = stringResource(Res.string.chat_presets_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    NavigationBackButton(onBackButtonClick = { navigation.navigateBack() })
                }
            )
        },
        containerColor = colors.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val preset = presets[page]

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Название и описание стиля
                    PresetHeader(preset = preset)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Предпросмотр баббла на фоне чата (во всю ширину экрана, без скруглений)
                    ChatPresetPreview(
                        preset = preset,
                        viewState = viewState,
                        localFontSize = localFontSize,
                        localShapeType = localShapeType,
                        localCornerRadius = localCornerRadius,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Настройки стиля (размер шрифта, форма, скругления)
                    if (preset == ChatRoleplayPreset.STANDARD_BUBBLE) {
                        BubbleStyleSettings(
                            fontSize = localFontSize,
                            shapeType = localShapeType,
                            cornerRadius = localCornerRadius,
                            onFontSizeChanged = onFontSizeChanged,
                            onFontSizeDragging = { size -> localFontSize = size },
                            onShapeTypeChanged = { shape ->
                                localShapeType = shape
                                onShapeTypeChanged(shape)
                            },
                            onCornerRadiusChanged = onCornerRadiusChanged,
                            onCornerRadiusDragging = { radius -> localCornerRadius = radius },
                            availableBubbleColorSchemes = viewState.availableBubbleColorSchemes,
                            selectedBubbleColorSchemeId = viewState.selectedBubbleColorSchemeId,
                            onBubbleColorSchemeSelected = onBubbleColorSchemeSelected,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    } else {
                        BubbleStyleSettings(
                            fontSize = localFontSize,
                            shapeType = null,
                            cornerRadius = null,
                            onFontSizeChanged = onFontSizeChanged,
                            onFontSizeDragging = { size -> localFontSize = size },
                            onShapeTypeChanged = null,
                            onCornerRadiusChanged = null,
                            onCornerRadiusDragging = null,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Кнопка применить или Premium-only (отключено ограничение — все пресеты открыты)
                    val isSelected = preset == viewState.selectedPreset
                    // val isPremiumPreset = preset != ChatRoleplayPreset.STANDARD_BUBBLE
                    // val isLocked = isPremiumPreset && !viewState.isPremium
                    val isLocked = false

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        if (isLocked) {
                            Button(
                                onClick = { navigation.navigateToPaymentScreen() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.neonAccent,
                                    contentColor = colors.background
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = IconsManager.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(Res.string.chat_presets_premium_only),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { if (!isSelected) onPresetSelected(preset) },
                                enabled = !isSelected,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.neonAccent,
                                    contentColor = colors.background,
                                    disabledContainerColor = colors.neonAccent.copy(alpha = 0.15f),
                                    disabledContentColor = colors.textSecondary.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSelected) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = IconsManager.Done,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(Res.string.chat_presets_applied),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = stringResource(Res.string.chat_presets_apply),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Точки-индикаторы
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .height(10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(presets.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        colors.neonAccent
                    } else {
                        colors.strokeActive.copy(alpha = 0.3f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}
