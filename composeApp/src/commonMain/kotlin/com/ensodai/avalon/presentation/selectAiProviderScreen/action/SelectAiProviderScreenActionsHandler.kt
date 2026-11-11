package com.ensodai.avalon.presentation.selectAiProviderScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.user.models.UpdateUserAiSettingsRequest
import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.GetUserAiSettingsUseCase
import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.UpdateUserAiSettingsUseCase
import com.ensodai.avalon.presentation.selectAiProviderScreen.SelectAiProviderScreenViewModel
import com.ensodai.avalon.presentation.selectAiProviderScreen.viewState.AiProviderItemUi
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch

fun SelectAiProviderScreenViewModel.createViewState(
    getUserAiSettings: GetUserAiSettingsUseCase
) {
    viewModelScope.launch(IODispatcher) {
        updateViewState { it.copy(isLoading = true, errorMessage = null) }
        try {
            val settings = getUserAiSettings.execute()
            val providersUi = settings.availableProviders.map { provider ->
                when (provider.id.lowercase()) {
                    "google" -> AiProviderItemUi(
                        id = provider.id,
                        displayName = provider.displayName,
                        description = "Бесплатный доступ к лучшим моделям Gemini 2.5 Flash и Pro. До 15 запросов в минуту без привязки банковской карты.",
                        keyUrl = "https://aistudio.google.com/app/apikey",
                        isDefault = provider.isDefault,
                        badgeText = "Бесплатный тариф",
                        isFree = true
                    )
                    "openrouter" -> AiProviderItemUi(
                        id = provider.id,
                        displayName = provider.displayName,
                        description = "Единый шлюз ко всем мировым нейросетям: Claude 3.7, GPT-4.5, Gemini 2.5, DeepSeek R1, Flux и 200+ других.",
                        keyUrl = "https://openrouter.ai/keys",
                        isDefault = provider.isDefault,
                        badgeText = "Рекомендуется",
                        isFree = false
                    )
                    "openai" -> AiProviderItemUi(
                        id = provider.id,
                        displayName = provider.displayName,
                        description = "Прямой доступ к API OpenAI: модели GPT-4o, GPT-4.5, o1/o3-mini, генерация картинок DALL-E и озвучка TTS.",
                        keyUrl = "https://platform.openai.com/api-keys",
                        isDefault = provider.isDefault,
                        badgeText = "Официальный API",
                        isFree = false
                    )
                    "deepseek" -> AiProviderItemUi(
                        id = provider.id,
                        displayName = provider.displayName,
                        description = "Официальный API DeepSeek: передовая рассуждающая модель DeepSeek R1 и универсальная DeepSeek V3.",
                        keyUrl = "https://platform.deepseek.com/api_keys",
                        isDefault = provider.isDefault,
                        badgeText = "Рассуждения R1",
                        isFree = false
                    )
                    "groq" -> AiProviderItemUi(
                        id = provider.id,
                        displayName = provider.displayName,
                        description = "Сверхбыстрый инференс открытых моделей на чипах LPU: Llama 3.3 70B, Qwen и Whisper со скоростью до 500 токенов/сек.",
                        keyUrl = "https://console.groq.com/keys",
                        isDefault = provider.isDefault,
                        badgeText = "Сверхскорость",
                        isFree = false
                    )
                    else -> AiProviderItemUi(
                        id = provider.id,
                        displayName = provider.displayName,
                        description = "Пользовательский OpenAI-совместимый эндпоинт.",
                        keyUrl = "",
                        isDefault = provider.isDefault,
                        badgeText = null,
                        isFree = false
                    )
                }
            }

            updateViewState {
                it.copy(
                    isLoading = false,
                    selectedProviderId = settings.providerId,
                    providers = providersUi
                )
            }
        } catch (e: Exception) {
            updateViewState { it.copy(isLoading = false, errorMessage = e.message) }
        }
    }
}

fun SelectAiProviderScreenViewModel.selectProvider(
    providerId: String,
    updateUserAiSettings: UpdateUserAiSettingsUseCase,
    getAvailableAiModels: com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.GetAvailableAiModelsUseCase
) {
    viewModelScope.launch(IODispatcher) {
        updateViewState { it.copy(updatingProviderId = providerId) }
        try {
            // 1. Сохраняем выбранного провайдера на бэкенде
            updateUserAiSettings.execute(
                UpdateUserAiSettingsRequest(
                    providerId = providerId
                )
            )

            // 2. Прогреваем кэш каталога моделей для всех категорий выбранного провайдера
            val providerEnum = com.ensodai.shared.avalon.user.models.AiProviderType.fromId(providerId)
            com.ensodai.shared.avalon.user.models.AiModelCategory.entries.forEach { category ->
                try {
                    getAvailableAiModels.execute(
                        provider = providerEnum,
                        category = category
                    )
                } catch (_: Exception) {}
            }

            updateViewState {
                it.copy(
                    selectedProviderId = providerId,
                    updatingProviderId = null,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            updateViewState { it.copy(updatingProviderId = null, errorMessage = e.message) }
        }
    }
}
