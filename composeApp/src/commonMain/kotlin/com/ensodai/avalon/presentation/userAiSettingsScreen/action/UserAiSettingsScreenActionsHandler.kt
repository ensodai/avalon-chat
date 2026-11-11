package com.ensodai.avalon.presentation.userAiSettingsScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.user.models.TestUserAiConnectionRequest
import com.ensodai.shared.avalon.user.models.UpdateUserAiSettingsRequest
import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.GetUserAiSettingsUseCase
import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.TestUserAiConnectionUseCase
import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.UpdateUserAiSettingsUseCase
import com.ensodai.avalon.presentation.userAiSettingsScreen.UserAiSettingsScreenViewModel
import com.ensodai.avalon.presentation.userAiSettingsScreen.viewState.BottomSheetUserAiSettings
import com.ensodai.avalon.presentation.userAiSettingsScreen.viewState.ModelType
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun UserAiSettingsScreenViewModel.createViewState(
    getUserAiSettings: GetUserAiSettingsUseCase
) {
    viewModelScope.launch(IODispatcher) {
        updateViewState { it.copy(isLoading = true, errorMessage = null) }
        try {
            val settings = getUserAiSettings.execute()
            updateViewState {
                it.copy(
                    isLoading = false,
                    providerId = settings.providerId,
                    availableProviders = settings.availableProviders,
                    baseUrl = settings.baseUrl,
                    apiKey = settings.apiKey ?: "",
                    textModel = settings.textModel,
                    ttsModel = settings.ttsModel,
                    imageModel = settings.imageModel,
                    imagePromptModel = settings.imagePromptModel,
                    summaryModel = settings.summaryModel,
                    isCustomProviderEnabled = settings.isCustomProviderEnabled
                )
            }
        } catch (e: Exception) {
            updateViewState { it.copy(isLoading = false, errorMessage = e.message) }
        }
    }
}

fun UserAiSettingsScreenViewModel.openHelpGuide() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetUserAiSettings.HelpGuide)
    }
}

fun UserAiSettingsScreenViewModel.openProviderPicker() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetUserAiSettings.ProviderPicker)
    }
}

fun UserAiSettingsScreenViewModel.openEditApiKey() {
    updateViewState {
        it.copy(bottomSheet = BottomSheetUserAiSettings.EditApiKey)
    }
}

fun UserAiSettingsScreenViewModel.openEditModel(
    type: ModelType,
    title: String,
    description: String,
    exampleHint: String,
    currentValue: String,
    getAvailableAiModels: com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.GetAvailableAiModelsUseCase
) {
    val currentProvider = com.ensodai.shared.avalon.user.models.AiProviderType.fromId(viewState.value.providerId)
    updateViewState {
        it.copy(
            bottomSheet = BottomSheetUserAiSettings.EditModel(
                type = type,
                title = title,
                description = description,
                exampleHint = exampleHint,
                currentValue = currentValue
            ),
            sheetTestError = null,
            sheetTestSuccess = false,
            isSheetTesting = false,
            isCatalogLoading = true,
            catalogSearchQuery = "",
            catalogModels = emptyList()
        )
    }

    viewModelScope.launch(IODispatcher) {
        try {
            val models = getAvailableAiModels.execute(
                provider = currentProvider,
                category = type.toCategory()
            )
            updateViewState {
                it.copy(
                    catalogModels = models,
                    isCatalogLoading = false
                )
            }
        } catch (e: Exception) {
            updateViewState {
                it.copy(isCatalogLoading = false)
            }
        }
    }
}

fun UserAiSettingsScreenViewModel.updateCatalogSearch(query: String) {
    updateViewState { it.copy(catalogSearchQuery = query) }
}

fun UserAiSettingsScreenViewModel.closeBottomSheet() {
    updateViewState {
        it.copy(
            bottomSheet = BottomSheetUserAiSettings.None,
            sheetTestError = null,
            sheetTestSuccess = false,
            isSheetTesting = false
        )
    }
}



fun UserAiSettingsScreenViewModel.updateProvider(
    providerId: String,
    updateUserAiSettings: UpdateUserAiSettingsUseCase
) {
    viewModelScope.launch(IODispatcher) {
        updateViewState {
            it.copy(
                providerId = providerId,
                apiKey = "",
                isCustomProviderEnabled = false,
                bottomSheet = BottomSheetUserAiSettings.None,
                textModel = "",
                ttsModel = "",
                imageModel = "",
                imagePromptModel = "",
                summaryModel = ""
            )
        }
        persistSettings(updateUserAiSettings)
    }
}

fun UserAiSettingsScreenViewModel.updateApiKey(
    apiKey: String,
    testUserAiConnection: TestUserAiConnectionUseCase,
    updateUserAiSettings: UpdateUserAiSettingsUseCase
) {
    viewModelScope.launch(IODispatcher) {
        if (apiKey.isBlank()) {
            updateViewState {
                it.copy(
                    apiKey = "",
                    sheetTestError = null,
                    sheetTestSuccess = false,
                    isSheetTesting = false,
                    bottomSheet = BottomSheetUserAiSettings.None
                )
            }
            persistSettings(updateUserAiSettings)
            return@launch
        }

        updateViewState {
            it.copy(
                isSheetTesting = true,
                sheetTestError = null,
                sheetTestSuccess = false
            )
        }

        val state = viewState.value
        val modelToTest = state.textModel.ifBlank { "gemini-2.5-flash" }

        try {
            val req = TestUserAiConnectionRequest(
                providerId = state.providerId,
                baseUrl = state.baseUrl.takeIf { it.isNotBlank() },
                apiKey = apiKey,
                model = modelToTest
            )
            val res = testUserAiConnection.execute(req, ModelType.TEXT)

            if (res.success) {
                updateViewState {
                    it.copy(
                        apiKey = apiKey,
                        isSheetTesting = false,
                        sheetTestSuccess = true,
                        sheetTestError = null
                    )
                }
                persistSettings(updateUserAiSettings)
                delay(300)
                updateViewState {
                    it.copy(
                        bottomSheet = BottomSheetUserAiSettings.None,
                        sheetTestSuccess = false
                    )
                }
            } else {
                updateViewState {
                    it.copy(
                        isSheetTesting = false,
                        sheetTestError = res.message ?: "Ошибка проверки API-ключа"
                    )
                }
            }
        } catch (e: Exception) {
            updateViewState {
                it.copy(
                    isSheetTesting = false,
                    sheetTestError = e.message ?: "Не удалось проверить ключ"
                )
            }
        }
    }
}

fun UserAiSettingsScreenViewModel.updateModel(
    type: ModelType,
    modelName: String,
    updateUserAiSettings: UpdateUserAiSettingsUseCase
) {
    viewModelScope.launch(IODispatcher) {
        updateViewState { current ->
            val updatedSheet = when (val sheet = current.bottomSheet) {
                is BottomSheetUserAiSettings.EditModel -> sheet.copy(currentValue = modelName)
                else -> current.bottomSheet
            }
            current.copy(
                bottomSheet = updatedSheet,
                sheetTestError = null,
                sheetTestSuccess = false,
                isSheetTesting = false,
                textModel = if (type == ModelType.TEXT) modelName else current.textModel,
                ttsModel = if (type == ModelType.TTS) modelName else current.ttsModel,
                imageModel = if (type == ModelType.IMAGE) modelName else current.imageModel,
                imagePromptModel = if (type == ModelType.IMAGE_PROMPT) modelName else current.imagePromptModel,
                summaryModel = if (type == ModelType.SUMMARY) modelName else current.summaryModel
            )
        }
        persistSettings(updateUserAiSettings)
    }
}

private suspend fun UserAiSettingsScreenViewModel.persistSettings(
    updateUserAiSettings: UpdateUserAiSettingsUseCase
) {
    val state = viewState.value
    try {
        val req = UpdateUserAiSettingsRequest(
            providerId = state.providerId,
            baseUrl = state.baseUrl.takeIf { it.isNotBlank() },
            apiKey = state.apiKey.takeIf { it.isNotBlank() },
            textModel = state.textModel.takeIf { it.isNotBlank() },
            ttsModel = state.ttsModel.takeIf { it.isNotBlank() },
            imageModel = state.imageModel.takeIf { it.isNotBlank() },
            imagePromptModel = state.imagePromptModel.takeIf { it.isNotBlank() },
            summaryModel = state.summaryModel.takeIf { it.isNotBlank() },
            isCustomProviderEnabled = state.apiKey.isNotBlank()
        )
        val res = updateUserAiSettings.execute(req)
        updateViewState {
            it.copy(
                providerId = res.providerId,
                baseUrl = res.baseUrl,
                apiKey = res.apiKey ?: "",
                textModel = res.textModel,
                ttsModel = res.ttsModel,
                imageModel = res.imageModel,
                imagePromptModel = res.imagePromptModel,
                summaryModel = res.summaryModel,
                isCustomProviderEnabled = res.isCustomProviderEnabled
            )
        }
    } catch (e: Exception) {
        updateViewState { it.copy(errorMessage = e.message) }
    }
}
