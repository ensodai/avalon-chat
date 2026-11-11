package com.ensodai.avalon.presentation.customizationScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.GetAllThemePresetsUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.GetSelectedThemePresetUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.GetAvalonColorsFlowUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.GetShowAiWarningFlowUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.SelectThemeUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.UpdateShowAiWarningUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.UpdateBubbleColorSchemeUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.UpdateBubbleStyleUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.SetChatBackgroundImageUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.customizationScreen.action.CustomizationScreenActions
import com.ensodai.avalon.presentation.customizationScreen.action.selectTheme
import com.ensodai.avalon.presentation.customizationScreen.action.updateBubbleFontSize
import com.ensodai.avalon.presentation.customizationScreen.action.updateBubbleShapeType
import com.ensodai.avalon.presentation.customizationScreen.action.updateBubbleCornerRadius
import com.ensodai.avalon.presentation.customizationScreen.action.updateBubbleColorScheme
import com.ensodai.avalon.presentation.customizationScreen.action.setBackgroundImage
import com.ensodai.avalon.presentation.customizationScreen.action.selectBackgroundImage
import com.ensodai.avalon.presentation.customizationScreen.action.deleteBackgroundImage
import com.ensodai.avalon.presentation.customizationScreen.action.updateBackgroundBlur
import com.ensodai.avalon.presentation.customizationScreen.action.removeBackgroundImage
import com.ensodai.avalon.presentation.customizationScreen.action.changeShowAiWarning
import com.ensodai.avalon.presentation.customizationScreen.action.changeFlowMode
import com.ensodai.avalon.presentation.customizationScreen.viewState.CustomizationScreenViewState
import com.ensodai.avalon.domain.useCases.commonUseCase.GetCurrentUserUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.UpdateUserFlowModeUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.GetChatRoleplayPresetFlowUseCase
import com.ensodai.avalon.theme.models.BubbleColorSchemes
import com.ensodai.avalon.theme.models.ChatBackgroundConfig
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel


@KoinViewModel
class CustomizationScreenViewModel(
    private val getAllThemePresetsUseCase: GetAllThemePresetsUseCase,
    private val getSelectedThemePresetUseCase: GetSelectedThemePresetUseCase,
    private val selectThemeUseCase: SelectThemeUseCase,
    private val updateBubbleStyleUseCase: UpdateBubbleStyleUseCase,
    private val updateBubbleColorSchemeUseCase: UpdateBubbleColorSchemeUseCase,
    private val setChatBackgroundImageUseCase: SetChatBackgroundImageUseCase,
    private val getAvalonColorsFlowUseCase: GetAvalonColorsFlowUseCase,
    private val getShowAiWarningFlowUseCase: GetShowAiWarningFlowUseCase,
    private val updateShowAiWarningUseCase: UpdateShowAiWarningUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserFlowModeUseCase: UpdateUserFlowModeUseCase,
    private val getChatRoleplayPresetFlowUseCase: GetChatRoleplayPresetFlowUseCase
) : BaseViewModel<CustomizationScreenViewState, CustomizationScreenActions>(
    initialState = CustomizationScreenViewState(
        themePresets = getAllThemePresetsUseCase()
    )
) {

    override val actions = CustomizationScreenActions(
        onThemeSelected = { preset -> selectTheme(preset, selectThemeUseCase) },
        onBubbleFontSizeChanged = { fontSize -> updateBubbleFontSize(fontSize, updateBubbleStyleUseCase) },
        onBubbleShapeTypeChanged = { shapeType -> updateBubbleShapeType(shapeType, updateBubbleStyleUseCase) },
        onBubbleCornerRadiusChanged = { cornerRadius -> updateBubbleCornerRadius(cornerRadius, updateBubbleStyleUseCase) },
        onBubbleColorSchemeSelected = { scheme -> updateBubbleColorScheme(scheme, updateBubbleColorSchemeUseCase) },
        onBackgroundImageSelected = { file -> setBackgroundImage(file, setChatBackgroundImageUseCase) },
        onSelectBackgroundImage = { image -> selectBackgroundImage(image, setChatBackgroundImageUseCase) },
        onDeleteBackgroundImage = { image -> deleteBackgroundImage(image, setChatBackgroundImageUseCase) },
        onBackgroundBlurChanged = { blur -> updateBackgroundBlur(blur, setChatBackgroundImageUseCase) },
        onRemoveBackgroundImage = { removeBackgroundImage(setChatBackgroundImageUseCase) },
        onShowAiWarningChanged = { isShow -> changeShowAiWarning(isShow, updateShowAiWarningUseCase) },
        onFlowModeChanged = { isEnabled -> changeFlowMode(isEnabled, updateUserFlowModeUseCase) },
        onChatRoleplayPresetsClick = { /* Handled in CustomizationScreen.kt */ }
    )

    init {
        viewModelScope.launch {
            getSelectedThemePresetUseCase().collectLatest { preset ->
                updateViewState {
                    it.copy(
                        selectedThemePreset = preset,
                        availableBubbleColorSchemes = BubbleColorSchemes.presetsForTheme(preset)
                    )
                }
            }
        }
        // Подписка на текущие настройки пузыря
        viewModelScope.launch {
            getAvalonColorsFlowUseCase().collectLatest { colors ->
                val matchedScheme = BubbleColorSchemes.matchSelectedScheme(
                    themePreset = colors.preset,
                    colors = colors
                )
                updateViewState {
                    it.copy(
                        bubbleFontSize = colors.bubbleFontSize,
                        bubbleShapeType = colors.bubbleShapeType,
                        bubbleCornerRadius = colors.bubbleCornerRadius,
                        selectedBubbleColorSchemeId = matchedScheme?.id,
                        chatBackgroundImagePath = (colors.chatBackground as? ChatBackgroundConfig.Image)?.url,
                        chatBackgroundBlur = (colors.chatBackground as? ChatBackgroundConfig.Image)?.blurRadius ?: 0
                    )
                }
            }
        }
        viewModelScope.launch {
            setChatBackgroundImageUseCase.getImages().collectLatest { images ->
                updateViewState { it.copy(chatBackgroundImages = images) }
            }
        }
        viewModelScope.launch {
            getShowAiWarningFlowUseCase().collectLatest { show ->
                updateViewState { it.copy(showAiWarning = show) }
            }
        }
        viewModelScope.launch {
            getCurrentUserUseCase().collectLatest { user ->
                updateViewState { it.copy(isFlowModeEnabled = user?.isFlowModeEnabled ?: false) }
            }
        }
        viewModelScope.launch {
            getChatRoleplayPresetFlowUseCase().collectLatest { preset ->
                updateViewState { it.copy(chatRoleplayPreset = preset) }
            }
        }
    }
}
