package com.ensodai.avalon.presentation.userAiSettingsScreen.viewState

import com.ensodai.shared.avalon.user.models.AiProviderResponse
import com.ensodai.avalon.presentation.core.BaseViewState

enum class ModelType(val key: String) {
    TEXT("text"),
    TTS("tts"),
    IMAGE("image"),
    IMAGE_PROMPT("imagePrompt"),
    SUMMARY("summary");

    fun toCategory(): com.ensodai.shared.avalon.user.models.AiModelCategory = when (this) {
        TEXT -> com.ensodai.shared.avalon.user.models.AiModelCategory.TEXT
        TTS -> com.ensodai.shared.avalon.user.models.AiModelCategory.TTS
        IMAGE -> com.ensodai.shared.avalon.user.models.AiModelCategory.IMAGE
        IMAGE_PROMPT -> com.ensodai.shared.avalon.user.models.AiModelCategory.IMAGE_PROMPT
        SUMMARY -> com.ensodai.shared.avalon.user.models.AiModelCategory.SUMMARY
    }
}

sealed interface BottomSheetUserAiSettings {
    object None : BottomSheetUserAiSettings
    object HelpGuide : BottomSheetUserAiSettings
    object ProviderPicker : BottomSheetUserAiSettings
    object EditApiKey : BottomSheetUserAiSettings
    data class EditModel(
        val type: ModelType,
        val title: String,
        val description: String,
        val exampleHint: String,
        val currentValue: String
    ) : BottomSheetUserAiSettings
}

data class UserAiSettingsScreenViewState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val providerId: String = "google",
    val availableProviders: List<AiProviderResponse> = emptyList(),
    val baseUrl: String = "",
    val apiKey: String = "",
    val textModel: String = "",
    val ttsModel: String = "",
    val imageModel: String = "",
    val imagePromptModel: String = "",
    val summaryModel: String = "",
    val isCustomProviderEnabled: Boolean = false,
    val bottomSheet: BottomSheetUserAiSettings = BottomSheetUserAiSettings.None,
    val isSheetTesting: Boolean = false,
    val sheetTestError: String? = null,
    val sheetTestSuccess: Boolean = false,
    val catalogModels: List<com.ensodai.shared.avalon.user.models.AiModelCatalogItem> = emptyList(),
    val isCatalogLoading: Boolean = false,
    val catalogSearchQuery: String = "",
    val supportedCategories: Set<com.ensodai.shared.avalon.user.models.AiModelCategory> = setOf(
        com.ensodai.shared.avalon.user.models.AiModelCategory.TEXT,
        com.ensodai.shared.avalon.user.models.AiModelCategory.TTS,
        com.ensodai.shared.avalon.user.models.AiModelCategory.IMAGE,
        com.ensodai.shared.avalon.user.models.AiModelCategory.IMAGE_PROMPT,
        com.ensodai.shared.avalon.user.models.AiModelCategory.SUMMARY
    )
) : BaseViewState()
