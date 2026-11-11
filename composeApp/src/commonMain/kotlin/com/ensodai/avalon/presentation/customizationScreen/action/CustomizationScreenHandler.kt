package com.ensodai.avalon.presentation.customizationScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.models.ChatBackgroundImage
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.SelectThemeUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.UpdateBubbleColorSchemeUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.UpdateBubbleStyleUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.SetChatBackgroundImageUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.UpdateUserFlowModeUseCase
import com.ensodai.avalon.domain.useCases.customizationScreenUseCase.UpdateShowAiWarningUseCase
import com.ensodai.avalon.presentation.customizationScreen.CustomizationScreenViewModel
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleColorScheme
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.utils.IODispatcher
import com.ensodai.avalon.utils.saveImageToInternalStorage
import com.ensodai.avalon.utils.deleteFile
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun CustomizationScreenViewModel.selectTheme(
    preset: AvalonThemePreset,
    selectThemeUseCase: SelectThemeUseCase
) {
    // Сброс цветовой схемы бабблов при смене темы
    updateViewState { it.copy(selectedBubbleColorSchemeId = null) }
    viewModelScope.launch(IODispatcher) {
        selectThemeUseCase(preset)
    }
}

fun CustomizationScreenViewModel.updateBubbleFontSize(
    fontSize: Int,
    updateBubbleStyleUseCase: UpdateBubbleStyleUseCase
) {
    updateViewState { it.copy(bubbleFontSize = fontSize) }
    viewModelScope.launch(IODispatcher) {
        val state = viewState.value
        updateBubbleStyleUseCase(
            fontSize = fontSize,
            shapeType = state.bubbleShapeType,
            cornerRadius = state.bubbleCornerRadius
        )
    }
}

fun CustomizationScreenViewModel.updateBubbleShapeType(
    shapeType: BubbleShapeType,
    updateBubbleStyleUseCase: UpdateBubbleStyleUseCase
) {
    updateViewState { it.copy(bubbleShapeType = shapeType) }
    viewModelScope.launch(IODispatcher) {
        val state = viewState.value
        updateBubbleStyleUseCase(
            fontSize = state.bubbleFontSize,
            shapeType = shapeType,
            cornerRadius = state.bubbleCornerRadius
        )
    }
}

fun CustomizationScreenViewModel.updateBubbleCornerRadius(
    cornerRadius: Int,
    updateBubbleStyleUseCase: UpdateBubbleStyleUseCase
) {
    updateViewState { it.copy(bubbleCornerRadius = cornerRadius) }
    viewModelScope.launch(IODispatcher) {
        val state = viewState.value
        updateBubbleStyleUseCase(
            fontSize = state.bubbleFontSize,
            shapeType = state.bubbleShapeType,
            cornerRadius = cornerRadius
        )
    }
}

fun CustomizationScreenViewModel.updateBubbleColorScheme(
    scheme: BubbleColorScheme?,
    updateBubbleColorSchemeUseCase: UpdateBubbleColorSchemeUseCase
) {
    updateViewState { it.copy(selectedBubbleColorSchemeId = scheme?.id) }
    viewModelScope.launch(IODispatcher) {
        updateBubbleColorSchemeUseCase(scheme)
    }
}

fun CustomizationScreenViewModel.setBackgroundImage(
    file: PlatformFile,
    setChatBackgroundImageUseCase: SetChatBackgroundImageUseCase
) {
    viewModelScope.launch(IODispatcher) {
        val bytes = file.readBytes()
        val id = Uuid.random()
        val fileName = "chat_bg_${id}.jpg"
        val localPath = saveImageToInternalStorage(bytes, fileName)
        
        if (localPath != null) {
            setChatBackgroundImageUseCase.addImage(id, localPath)
            val blur = viewState.value.chatBackgroundBlur
            setChatBackgroundImageUseCase(url = localPath, blur = blur)
        }
    }
}

fun CustomizationScreenViewModel.selectBackgroundImage(
    image: ChatBackgroundImage,
    setChatBackgroundImageUseCase: SetChatBackgroundImageUseCase
) {
    viewModelScope.launch(IODispatcher) {
        val currentState = viewState.value
        if (currentState.chatBackgroundImagePath == image.url) {
            setChatBackgroundImageUseCase.reset()
        } else {
            val blur = currentState.chatBackgroundBlur
            setChatBackgroundImageUseCase(url = image.url, blur = blur)
        }
    }
}

fun CustomizationScreenViewModel.deleteBackgroundImage(
    image: ChatBackgroundImage,
    setChatBackgroundImageUseCase: SetChatBackgroundImageUseCase
) {
    viewModelScope.launch(IODispatcher) {
        // Сначала удаляем файл физически
        deleteFile(image.url)
        
        // Затем из базы
        setChatBackgroundImageUseCase.removeImage(image.id)

        if (viewState.value.chatBackgroundImagePath == image.url) {
            val remaining = viewState.value.chatBackgroundImages.filter { it.id != image.id }
            if (remaining.isNotEmpty()) {
                setChatBackgroundImageUseCase(url = remaining.first().url, blur = viewState.value.chatBackgroundBlur)
            } else {
                setChatBackgroundImageUseCase.reset()
            }
        }
    }
}

fun CustomizationScreenViewModel.updateBackgroundBlur(
    blur: Int,
    setChatBackgroundImageUseCase: SetChatBackgroundImageUseCase
) {
    updateViewState { it.copy(chatBackgroundBlur = blur) }
    viewModelScope.launch(IODispatcher) {
        val path = viewState.value.chatBackgroundImagePath ?: return@launch
        setChatBackgroundImageUseCase.updateBlur(url = path, blur = blur)
    }
}

fun CustomizationScreenViewModel.removeBackgroundImage(
    setChatBackgroundImageUseCase: SetChatBackgroundImageUseCase
) {
    updateViewState { it.copy(chatBackgroundImagePath = null, chatBackgroundBlur = 0) }
    viewModelScope.launch(IODispatcher) {
        setChatBackgroundImageUseCase.reset()
    }
}

fun CustomizationScreenViewModel.changeShowAiWarning(
    isShow: Boolean,
    updateShowAiWarningUseCase: UpdateShowAiWarningUseCase
) {
    updateViewState { it.copy(showAiWarning = isShow) }
    viewModelScope.launch(IODispatcher) {
        updateShowAiWarningUseCase(isShow)
    }
}

fun CustomizationScreenViewModel.changeFlowMode(
    isEnabled: Boolean,
    updateUserFlowModeUseCase: UpdateUserFlowModeUseCase
) {
    updateViewState { it.copy(isFlowModeEnabled = isEnabled) }
    viewModelScope.launch(IODispatcher) {
        try {
            updateUserFlowModeUseCase(isEnabled)
        } catch (e: Exception) {
            // Revert state if the API call fails
            updateViewState { it.copy(isFlowModeEnabled = !isEnabled) }
        }
    }
}
