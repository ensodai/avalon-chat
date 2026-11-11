package com.ensodai.avalon.presentation.customizationScreen.action

import com.ensodai.avalon.domain.models.ChatBackgroundImage
import com.ensodai.avalon.presentation.core.BaseActions
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleColorScheme
import com.ensodai.avalon.theme.models.BubbleShapeType

data class CustomizationScreenActions(
    val onThemeSelected: (AvalonThemePreset) -> Unit,
    val onBubbleFontSizeChanged: (Int) -> Unit,
    val onBubbleShapeTypeChanged: (BubbleShapeType) -> Unit,
    val onBubbleCornerRadiusChanged: (Int) -> Unit,
    /** null = сброс на дефолт темы */
    val onBubbleColorSchemeSelected: (BubbleColorScheme?) -> Unit,
    val onBackgroundImageSelected: (io.github.vinceglb.filekit.PlatformFile) -> Unit,
    val onSelectBackgroundImage: (ChatBackgroundImage) -> Unit,
    val onDeleteBackgroundImage: (ChatBackgroundImage) -> Unit,
    val onBackgroundBlurChanged: (Int) -> Unit,
    val onRemoveBackgroundImage: () -> Unit,
    val onShowAiWarningChanged: (Boolean) -> Unit,
    val onFlowModeChanged: (Boolean) -> Unit,
    val onChatRoleplayPresetsClick: () -> Unit
) : BaseActions()
