package com.ensodai.avalon.presentation.customizationScreen.viewState

import com.ensodai.avalon.domain.models.ChatBackgroundImage
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleColorScheme
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.theme.models.ChatRoleplayPreset

data class CustomizationScreenViewState(
    val themePresets: List<AvalonThemePreset> = emptyList(),
    val selectedThemePreset: AvalonThemePreset? = null,
    val availableBubbleColorSchemes: List<BubbleColorScheme> = emptyList(),
    val bubbleFontSize: Int = 16,
    val bubbleShapeType: BubbleShapeType = BubbleShapeType.Tail,
    val bubbleCornerRadius: Int = 16,
    val selectedBubbleColorSchemeId: String? = null,
    /** Путь к фоновому изображению чата (null = дефолт от темы) */
    val chatBackgroundImagePath: String? = null,
    /** Уровень размытия фоновой картинки 0..25 */
    val chatBackgroundBlur: Int = 0,
    /** Список загруженных фоновых изображений */
    val chatBackgroundImages: List<ChatBackgroundImage> = emptyList(),
    val showAiWarning: Boolean = true,
    val isFlowModeEnabled: Boolean = false,
    val chatRoleplayPreset: ChatRoleplayPreset = ChatRoleplayPreset.STANDARD_BUBBLE
) : BaseViewState()
