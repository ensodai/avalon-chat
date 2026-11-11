package com.ensodai.avalon.presentation.userAiSettingsScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.user.models.AiModelCategory
import com.ensodai.shared.avalon.user.models.AiProviderType
import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.GetAvailableAiModelsUseCase
import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.GetUserAiSettingsUseCase
import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.TestUserAiConnectionUseCase
import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.UpdateUserAiSettingsUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.UserAiSettingsScreenActions
import com.ensodai.avalon.utils.IODispatcher
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.closeBottomSheet
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.createViewState
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.openEditApiKey
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.openEditModel
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.openHelpGuide
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.openProviderPicker
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.updateApiKey
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.updateCatalogSearch
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.updateModel
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.updateProvider
import com.ensodai.avalon.presentation.userAiSettingsScreen.viewState.UserAiSettingsScreenViewState
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class UserAiSettingsScreenViewModel(
    val getUserAiSettings: GetUserAiSettingsUseCase,
    val updateUserAiSettings: UpdateUserAiSettingsUseCase,
    val testUserAiConnection: TestUserAiConnectionUseCase,
    val getAvailableAiModels: GetAvailableAiModelsUseCase
) : BaseViewModel<UserAiSettingsScreenViewState, UserAiSettingsScreenActions>(
    initialState = UserAiSettingsScreenViewState()
) {
    override val actions = UserAiSettingsScreenActions(
        onOpenHelpGuide = {
            openHelpGuide()
        },
        onOpenProviderPicker = {
            openProviderPicker()
        },
        onOpenEditApiKey = {
            openEditApiKey()
        },
        onOpenEditModel = { type, title, description, exampleHint, currentValue ->
            openEditModel(
                type = type,
                title = title,
                description = description,
                exampleHint = exampleHint,
                currentValue = currentValue,
                getAvailableAiModels = getAvailableAiModels
            )
        },
        onCloseBottomSheet = {
            closeBottomSheet()
        },
        onProviderSelected = { providerId ->
            updateProvider(
                providerId = providerId,
                updateUserAiSettings = updateUserAiSettings
            )
        },
        onUpdateApiKey = { apiKey ->
            updateApiKey(
                apiKey = apiKey,
                testUserAiConnection = testUserAiConnection,
                updateUserAiSettings = updateUserAiSettings
            )
        },
        onUpdateModel = { type, modelName ->
            updateModel(
                type = type,
                modelName = modelName,
                updateUserAiSettings = updateUserAiSettings
            )
        },
        onUpdateCatalogSearch = { query ->
            updateCatalogSearch(query)
        }
    )

    init {
        viewModelScope.launch(IODispatcher) {
            getUserAiSettings.observe().filterNotNull().collect { settings ->
                val provider = AiProviderType.fromId(settings.providerId)
                val categories = when (provider) {
                    AiProviderType.GOOGLE,
                    AiProviderType.OPENAI,
                    AiProviderType.OPENROUTER -> setOf(
                        AiModelCategory.TEXT,
                        AiModelCategory.TTS,
                        AiModelCategory.IMAGE,
                        AiModelCategory.IMAGE_PROMPT,
                        AiModelCategory.SUMMARY
                    )
                    AiProviderType.DEEPSEEK,
                    AiProviderType.GROQ,
                    AiProviderType.CUSTOM -> setOf(
                        AiModelCategory.TEXT,
                        AiModelCategory.SUMMARY
                    )
                }

                updateViewState { current ->
                    current.copy(
                        isCustomProviderEnabled = settings.isCustomProviderEnabled,
                        providerId = settings.providerId,
                        baseUrl = settings.baseUrl,
                        apiKey = settings.apiKey ?: "",
                        textModel = settings.textModel,
                        ttsModel = settings.ttsModel,
                        imageModel = settings.imageModel,
                        imagePromptModel = settings.imagePromptModel,
                        summaryModel = settings.summaryModel,
                        availableProviders = settings.availableProviders,
                        supportedCategories = categories,
                        isLoading = false
                    )
                }
            }
        }
        createViewState(getUserAiSettings = getUserAiSettings)
    }
}
