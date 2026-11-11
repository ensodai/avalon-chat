@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package com.ensodai.avalon.presentation.importCharacterScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.spec.AvalonCardCustomization
import com.ensodai.shared.avalon.character.spec.CardLayout
import com.ensodai.shared.avalon.character.spec.CharacterCardData
import com.ensodai.shared.avalon.character.spec.CharacterCardEnvelope
import com.ensodai.shared.avalon.character.spec.CharacterCardParser
import com.ensodai.shared.avalon.character.spec.PngCharacterCardCodec
import com.ensodai.shared.avalon.character.spec.characterCardJson
import com.ensodai.avalon.presentation.importCharacterScreen.ImportCharacterScreenViewModel
import com.ensodai.avalon.utils.IODispatcher
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.io.encoding.Base64

fun ImportCharacterScreenViewModel.onFilePicked(file: PlatformFile?) {
    if (file == null) return
    viewModelScope.launch(IODispatcher) {
        updateViewState { it.copy(isLoading = true, errorMessage = null) }
        runCatching {
            val bytes = file.readBytes()
            val fileName = file.name
            val isPng = PngCharacterCardCodec.isPng(bytes)

            val parsed = if (isPng) {
                PngCharacterCardCodec.extractCard(bytes)
            } else {
                val jsonString = bytes.decodeToString()
                CharacterCardParser.parse(jsonString)
            }

            if (parsed == null) {
                throw IllegalArgumentException("Файл не содержит данных спецификации Character Card (ccv3/chara)")
            }

            val existingCustomization = AvalonCardCustomization.fromExtensions(parsed.data.extensions)
            val initialCustomization = if (parsed.data.extensions.containsKey(AvalonCardCustomization.EXTENSION_KEY)) {
                existingCustomization
            } else {
                if (isPng) {
                    existingCustomization.copy(layout = CardLayout.FULL_BLEED)
                } else {
                    existingCustomization.copy(layout = CardLayout.HEX_CORE)
                }
            }

            updateViewState {
                it.copy(
                    isLoading = false,
                    selectedFileName = fileName,
                    fileBytes = bytes,
                    isPngFile = isPng,
                    parsedCardData = parsed.data,
                    specVersion = parsed.specVersion,
                    customization = initialCustomization,
                    errorMessage = null
                )
            }
        }.onFailure { e ->
            updateViewState {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Ошибка при чтении файла"
                )
            }
        }
    }
}

fun ImportCharacterScreenViewModel.onChangeVisibility(visibility: CharacterVisibility) {
    updateViewState { it.copy(selectedVisibility = visibility) }
}

fun ImportCharacterScreenViewModel.onUpdateCustomization(customization: AvalonCardCustomization) {
    updateViewState { it.copy(customization = customization) }
}

fun ImportCharacterScreenViewModel.onClearError() {
    updateViewState { it.copy(errorMessage = null) }
}

fun ImportCharacterScreenViewModel.onResetFile() {
    updateViewState {
        it.copy(
            selectedFileName = null,
            fileBytes = null,
            isPngFile = false,
            parsedCardData = null,
            specVersion = null,
            customization = AvalonCardCustomization(),
            errorMessage = null,
            isImportSuccess = false,
            importedCharacterId = null
        )
    }
}

fun ImportCharacterScreenViewModel.onImportClick() {
    val state = viewState.value
    val bytes = state.fileBytes ?: return
    val cardData = state.parsedCardData ?: return

    viewModelScope.launch(IODispatcher) {
        updateViewState { it.copy(isLoading = true, errorMessage = null) }
        runCatching {
            // Встраиваем кастомизацию Avalon Dark Monolith в extensions карточки
            val currentExtensions = cardData.extensions.toMutableMap()
            currentExtensions[AvalonCardCustomization.EXTENSION_KEY] = state.customization.toExtensionJson()
            val updatedCardData = cardData.copy(extensions = JsonObject(currentExtensions))

            val dataJson = characterCardJson.encodeToJsonElement(CharacterCardData.serializer(), updatedCardData).jsonObject
            val envelope = CharacterCardEnvelope(
                spec = "chara_card_v3",
                specVersion = "3.0",
                data = dataJson
            )
            val jsonPayload = characterCardJson.encodeToString(CharacterCardEnvelope.serializer(), envelope)

            val finalBytes: ByteArray = if (state.isPngFile) {
                PngCharacterCardCodec.embedCardJson(
                    originalPngBytes = bytes,
                    cardJson = jsonPayload,
                    keyword = "ccv3"
                )
            } else {
                jsonPayload.encodeToByteArray()
            }

            val base64String = Base64.Default.encode(finalBytes)

            val result = importCharacterCardUseCase.execute(
                fileBase64 = base64String,
                visibility = state.selectedVisibility
            )

            val characterId = result.getOrThrow()

            updateViewState {
                it.copy(
                    isLoading = false,
                    isImportSuccess = true,
                    importedCharacterId = characterId
                )
            }
        }.onFailure { e ->
            updateViewState {
                it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Не удалось импортировать персонажа"
                )
            }
        }
    }
}
