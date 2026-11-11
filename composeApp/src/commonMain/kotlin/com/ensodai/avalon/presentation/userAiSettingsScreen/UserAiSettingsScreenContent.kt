package com.ensodai.avalon.presentation.userAiSettingsScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.user_ai_api_key
import com.ensodai.avalon.generated.resources.user_ai_api_key_placeholder
import com.ensodai.avalon.generated.resources.user_ai_model_image_desc
import com.ensodai.avalon.generated.resources.user_ai_model_image_hint
import com.ensodai.avalon.generated.resources.user_ai_model_image_title
import com.ensodai.avalon.generated.resources.user_ai_model_prompt_desc
import com.ensodai.avalon.generated.resources.user_ai_model_prompt_hint
import com.ensodai.avalon.generated.resources.user_ai_model_prompt_title
import com.ensodai.avalon.generated.resources.user_ai_model_summary_desc
import com.ensodai.avalon.generated.resources.user_ai_model_summary_hint
import com.ensodai.avalon.generated.resources.user_ai_model_summary_title
import com.ensodai.avalon.generated.resources.user_ai_model_text_desc
import com.ensodai.avalon.generated.resources.user_ai_model_text_hint
import com.ensodai.avalon.generated.resources.user_ai_model_text_title
import com.ensodai.avalon.generated.resources.user_ai_model_tts_desc
import com.ensodai.avalon.generated.resources.user_ai_model_tts_hint
import com.ensodai.avalon.generated.resources.user_ai_model_tts_title
import com.ensodai.avalon.generated.resources.user_ai_provider
import com.ensodai.avalon.generated.resources.user_ai_section_connection
import com.ensodai.avalon.generated.resources.user_ai_section_models
import com.ensodai.avalon.navigation.avalonCustomSwipe
import com.ensodai.avalon.presentation.editCharacterScreen.component.EditActionType
import com.ensodai.avalon.presentation.editCharacterScreen.component.EditFieldCard
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.UserAiSettingsScreenActions
import com.ensodai.avalon.presentation.userAiSettingsScreen.component.BottomSheetUserAiSettingsScreen
import com.ensodai.avalon.presentation.userAiSettingsScreen.component.HelpGuideCard
import com.ensodai.avalon.presentation.userAiSettingsScreen.component.TopBarUserAiSettingsScreen
import com.ensodai.avalon.presentation.userAiSettingsScreen.viewState.ModelType
import com.ensodai.avalon.presentation.userAiSettingsScreen.viewState.UserAiSettingsScreenViewState
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun UserAiSettingsScreenContent(
    state: UserAiSettingsScreenViewState,
    actions: UserAiSettingsScreenActions,
    onBack: () -> Unit,
    onNavigateToSummaryExplanation: () -> Unit = {},
    onNavigateToSelectProvider: () -> Unit = {}
) {
    val colors = AvalonTheme.colors
    val scrollState = rememberScrollState()

    val textTitle = stringResource(Res.string.user_ai_model_text_title)
    val textDesc = stringResource(Res.string.user_ai_model_text_desc)
    val textHint = stringResource(Res.string.user_ai_model_text_hint)

    val ttsTitle = stringResource(Res.string.user_ai_model_tts_title)
    val ttsDesc = stringResource(Res.string.user_ai_model_tts_desc)
    val ttsHint = stringResource(Res.string.user_ai_model_tts_hint)

    val imgTitle = stringResource(Res.string.user_ai_model_image_title)
    val imgDesc = stringResource(Res.string.user_ai_model_image_desc)
    val imgHint = stringResource(Res.string.user_ai_model_image_hint)

    val promptTitle = stringResource(Res.string.user_ai_model_prompt_title)
    val promptDesc = stringResource(Res.string.user_ai_model_prompt_desc)
    val promptHint = stringResource(Res.string.user_ai_model_prompt_hint)

    val summaryTitle = stringResource(Res.string.user_ai_model_summary_title)
    val summaryDesc = stringResource(Res.string.user_ai_model_summary_desc)
    val summaryHint = stringResource(Res.string.user_ai_model_summary_hint)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .avalonCustomSwipe { dragDeltaX -> dragDeltaX > 0 },
        topBar = {
            TopBarUserAiSettingsScreen(
                modifier = Modifier.fillMaxWidth(),
                onBackButtonClick = onBack
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.neonAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        top = innerPadding.calculateTopPadding() + 16.dp,
                        bottom = innerPadding.calculateBottomPadding() + 32.dp
                    )
            ) {
                // Карточка-гайд в самом верху
                HelpGuideCard(
                    onClick = actions.onOpenHelpGuide
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Раздел подключения
                Text(
                    text = stringResource(Res.string.user_ai_section_connection),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp
                    ),
                    color = colors.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Карточка выбора провайдера
                val providerDisplayName = state.availableProviders.firstOrNull {
                    it.id.equals(state.providerId, ignoreCase = true)
                }?.displayName ?: state.providerId.replaceFirstChar { it.uppercase() }

                EditFieldCard(
                    title = stringResource(Res.string.user_ai_provider),
                    value = providerDisplayName,
                    icon = IconsManager.Hub,
                    actionType = EditActionType.NAVIGATION,
                    onClick = onNavigateToSelectProvider,
                    isUpdating = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Карточка API Ключа
                val maskedKey = if (state.apiKey.isNotBlank()) {
                    if (state.apiKey.length > 12) {
                        "${state.apiKey.take(7)}••••••••${state.apiKey.takeLast(4)}"
                    } else {
                        "••••••••"
                    }
                } else {
                    null
                }

                EditFieldCard(
                    title = stringResource(Res.string.user_ai_api_key),
                    value = maskedKey,
                    placeholder = stringResource(Res.string.user_ai_api_key_placeholder),
                    icon = IconsManager.Lock,
                    actionType = EditActionType.SHEET,
                    onClick = actions.onOpenEditApiKey,
                    isUpdating = false
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Раздел моделей
                Text(
                    text = stringResource(Res.string.user_ai_section_models),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp
                    ),
                    color = colors.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 1. Модель диалога
                if (state.supportedCategories.contains(com.ensodai.shared.avalon.user.models.AiModelCategory.TEXT)) {
                    EditFieldCard(
                        title = textTitle,
                        value = state.textModel,
                        placeholder = "gemini-2.5-flash",
                        icon = IconsManager.ChatBubbleOutline,
                        actionType = EditActionType.SHEET,
                        onClick = {
                            actions.onOpenEditModel(
                                ModelType.TEXT,
                                textTitle,
                                textDesc,
                                textHint,
                                state.textModel
                            )
                        },
                        isUpdating = false
                    )
                }

                // 2. Модель синтеза речи (TTS)
                if (state.supportedCategories.contains(com.ensodai.shared.avalon.user.models.AiModelCategory.TTS)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EditFieldCard(
                        title = ttsTitle,
                        value = state.ttsModel,
                        placeholder = "gemini-2.5-flash-preview-tts",
                        icon = IconsManager.Waves,
                        actionType = EditActionType.SHEET,
                        onClick = {
                            actions.onOpenEditModel(
                                ModelType.TTS,
                                ttsTitle,
                                ttsDesc,
                                ttsHint,
                                state.ttsModel
                            )
                        },
                        isUpdating = false
                    )
                }

                // 3. Модель генерации изображений
                if (state.supportedCategories.contains(com.ensodai.shared.avalon.user.models.AiModelCategory.IMAGE)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EditFieldCard(
                        title = imgTitle,
                        value = state.imageModel,
                        placeholder = "gemini-2.5-flash-image",
                        icon = IconsManager.Image,
                        actionType = EditActionType.SHEET,
                        onClick = {
                            actions.onOpenEditModel(
                                ModelType.IMAGE,
                                imgTitle,
                                imgDesc,
                                imgHint,
                                state.imageModel
                            )
                        },
                        isUpdating = false
                    )
                }

                // 4. Модель генерации промптов для фото
                if (state.supportedCategories.contains(com.ensodai.shared.avalon.user.models.AiModelCategory.IMAGE_PROMPT)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EditFieldCard(
                        title = promptTitle,
                        value = state.imagePromptModel,
                        placeholder = "gemini-2.5-flash",
                        icon = IconsManager.AutoMode,
                        actionType = EditActionType.SHEET,
                        onClick = {
                            actions.onOpenEditModel(
                                ModelType.IMAGE_PROMPT,
                                promptTitle,
                                promptDesc,
                                promptHint,
                                state.imagePromptModel
                            )
                        },
                        isUpdating = false
                    )
                }

                // 5. Модель суммаризации и памяти
                if (state.supportedCategories.contains(com.ensodai.shared.avalon.user.models.AiModelCategory.SUMMARY)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EditFieldCard(
                        title = summaryTitle,
                        value = state.summaryModel,
                        placeholder = "gemini-2.5-flash-lite",
                        icon = IconsManager.Psychology,
                        actionType = EditActionType.SHEET,
                        onClick = {
                            actions.onOpenEditModel(
                                ModelType.SUMMARY,
                                summaryTitle,
                                summaryDesc,
                                summaryHint,
                                state.summaryModel
                            )
                        },
                        isUpdating = false
                    )
                }
            }
        }
    }

    BottomSheetUserAiSettingsScreen(
        state = state,
        actions = actions,
        onNavigateToSummaryExplanation = onNavigateToSummaryExplanation
    )
}
