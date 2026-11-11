package com.ensodai.avalon.presentation.customizationScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.GetAvalonColorsFlowUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.GetChatRoleplayPresetFlowUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.UpdateChatRoleplayPresetUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.UpdateBubbleStyleUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.UpdateBubbleColorSchemeUseCase
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.theme.models.ChatBackgroundConfig
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import com.ensodai.avalon.theme.models.BubbleColorScheme
import com.ensodai.avalon.theme.models.BubbleColorSchemes
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class ChatPresetsViewState(
    val selectedPreset: ChatRoleplayPreset = ChatRoleplayPreset.STANDARD_BUBBLE,
    val bubbleFontSize: Int = 16,
    val bubbleShapeType: BubbleShapeType = BubbleShapeType.Tail,
    val bubbleCornerRadius: Int = 12,
    val selectedThemePreset: AvalonThemePreset? = null,
    val chatBackgroundImagePath: String? = null,
    val chatBackgroundBlur: Int = 0,
    val availableBubbleColorSchemes: List<BubbleColorScheme> = emptyList(),
    val selectedBubbleColorSchemeId: String? = null,
    val isPremium: Boolean = false
)

@KoinViewModel
class ChatPresetsViewModel(
    getChatRoleplayPresetFlowUseCase: GetChatRoleplayPresetFlowUseCase,
    private val updateChatRoleplayPresetUseCase: UpdateChatRoleplayPresetUseCase,
    private val getAvalonColorsFlowUseCase: GetAvalonColorsFlowUseCase,
    private val updateBubbleStyleUseCase: UpdateBubbleStyleUseCase,
    private val updateBubbleColorSchemeUseCase: UpdateBubbleColorSchemeUseCase,
    private val localUserRepository: LocalUserRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow(ChatPresetsViewState())
    val viewState: StateFlow<ChatPresetsViewState> = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            getChatRoleplayPresetFlowUseCase().collectLatest { preset ->
                _viewState.update { it.copy(selectedPreset = preset) }
            }
        }
        viewModelScope.launch {
            localUserRepository.getCurrentUserFlow().collectLatest { user ->
                val isPremium = user?.premiumStatus != PremiumStatus.FREE
                _viewState.update { it.copy(isPremium = isPremium) }
            }
        }
        viewModelScope.launch {
            getAvalonColorsFlowUseCase().collectLatest { colors ->
                val matchedScheme = BubbleColorSchemes.matchSelectedScheme(
                    themePreset = colors.preset,
                    colors = colors
                )
                _viewState.update {
                    it.copy(
                        bubbleFontSize = colors.bubbleFontSize,
                        bubbleShapeType = colors.bubbleShapeType,
                        bubbleCornerRadius = colors.bubbleCornerRadius,
                        selectedThemePreset = colors.preset,
                        chatBackgroundImagePath = (colors.chatBackground as? ChatBackgroundConfig.Image)?.url,
                        chatBackgroundBlur = (colors.chatBackground as? ChatBackgroundConfig.Image)?.blurRadius ?: 0,
                        availableBubbleColorSchemes = BubbleColorSchemes.presetsForTheme(colors.preset),
                        selectedBubbleColorSchemeId = matchedScheme?.id
                    )
                }
            }
        }
    }

    fun selectPreset(preset: ChatRoleplayPreset) {
        viewModelScope.launch {
            updateChatRoleplayPresetUseCase(preset)
        }
    }

    fun selectBubbleColorScheme(scheme: BubbleColorScheme?) {
        viewModelScope.launch {
            updateBubbleColorSchemeUseCase(scheme)
        }
    }

    fun updateBubbleFontSize(fontSize: Int) {
        _viewState.update { it.copy(bubbleFontSize = fontSize) }
        viewModelScope.launch(IODispatcher) {
            val state = _viewState.value
            updateBubbleStyleUseCase(
                fontSize = fontSize,
                shapeType = state.bubbleShapeType,
                cornerRadius = state.bubbleCornerRadius
            )
        }
    }

    fun updateBubbleShapeType(shapeType: BubbleShapeType) {
        _viewState.update { it.copy(bubbleShapeType = shapeType) }
        viewModelScope.launch(IODispatcher) {
            val state = _viewState.value
            updateBubbleStyleUseCase(
                fontSize = state.bubbleFontSize,
                shapeType = shapeType,
                cornerRadius = state.bubbleCornerRadius
            )
        }
    }

    fun updateBubbleCornerRadius(cornerRadius: Int) {
        _viewState.update { it.copy(bubbleCornerRadius = cornerRadius) }
        viewModelScope.launch(IODispatcher) {
            val state = _viewState.value
            updateBubbleStyleUseCase(
                fontSize = state.bubbleFontSize,
                shapeType = state.bubbleShapeType,
                cornerRadius = cornerRadius
            )
        }
    }
}
