package com.ensodai.avalon.presentation.userAiSettingsScreen.component

import androidx.compose.runtime.Composable
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.UserAiSettingsScreenActions
import com.ensodai.avalon.presentation.userAiSettingsScreen.viewState.BottomSheetUserAiSettings
import com.ensodai.avalon.presentation.userAiSettingsScreen.viewState.ModelType
import com.ensodai.avalon.presentation.userAiSettingsScreen.viewState.UserAiSettingsScreenViewState

@Composable
fun BottomSheetUserAiSettingsScreen(
    state: UserAiSettingsScreenViewState,
    actions: UserAiSettingsScreenActions,
    onNavigateToSummaryExplanation: (() -> Unit)? = null
) {
    when (val sheet = state.bottomSheet) {
        BottomSheetUserAiSettings.None -> {}

        BottomSheetUserAiSettings.HelpGuide -> {
            HelpGuideBottomSheet(
                onDismiss = actions.onCloseBottomSheet
            )
        }

        BottomSheetUserAiSettings.ProviderPicker -> {
            ProviderPickerBottomSheet(
                availableProviders = state.availableProviders,
                selectedProviderId = state.providerId,
                onProviderSelected = actions.onProviderSelected,
                onDismiss = actions.onCloseBottomSheet
            )
        }

        BottomSheetUserAiSettings.EditApiKey -> {
            EditApiKeyBottomSheet(
                currentApiKey = state.apiKey,
                isTesting = state.isSheetTesting,
                sheetTestError = state.sheetTestError,
                onSave = actions.onUpdateApiKey,
                onDismiss = actions.onCloseBottomSheet
            )
        }

        is BottomSheetUserAiSettings.EditModel -> {
            val currentModelValue = when (sheet.type) {
                ModelType.TEXT -> state.textModel
                ModelType.TTS -> state.ttsModel
                ModelType.IMAGE -> state.imageModel
                ModelType.IMAGE_PROMPT -> state.imagePromptModel
                ModelType.SUMMARY -> state.summaryModel
            }.ifBlank { sheet.currentValue }

            EditModelBottomSheet(
                modelType = sheet.type,
                title = sheet.title,
                description = sheet.description,
                currentValue = currentModelValue,
                catalogModels = state.catalogModels,
                isCatalogLoading = state.isCatalogLoading,
                catalogSearchQuery = state.catalogSearchQuery,
                onUpdateCatalogSearch = actions.onUpdateCatalogSearch,
                onSelectModel = actions.onUpdateModel,
                onNavigateToSummaryExplanation = onNavigateToSummaryExplanation,
                onDismiss = actions.onCloseBottomSheet
            )
        }
    }
}
