package com.ensodai.avalon.presentation.addCharacterScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.create
import com.ensodai.avalon.generated.resources.interaction_format_dialog_title
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.addCharacterScreen.actions.AddCharacterScreenActions
import com.ensodai.avalon.presentation.addCharacterScreen.component.CategoriesStageScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.FirstMessageStageScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.FullDescriptionStagesScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.ScenarioStageScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.MesExampleStageScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.AddCharacterLorebookStageScreen
import com.ensodai.avalon.presentation.characterLorebooksScreen.component.AttachLorebookBottomSheet
import com.ensodai.avalon.presentation.characterLorebooksScreen.component.CreateLorebookDialog
import com.ensodai.avalon.presentation.addCharacterScreen.component.NameStageScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.OutreachStageScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.PublishCharacterStageScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.ShortDescriptionStageScreen
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.presentation.liveCallScreen.component.VoiceModelPickerSheet
import com.ensodai.avalon.presentation.addCharacterScreen.component.TemperatureStageScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.VisibleStageScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.alertDialogs.AlertDialogsAddCharacterScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.avatarStageScreen.AddCharacterAvatarStageScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.bottomBar.BottomBarAddCharacterScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.helloCardStageScreen.AddCharacterHelloCardStageScreen
import com.ensodai.avalon.presentation.addCharacterScreen.component.loader.TechnoSuccessState
import com.ensodai.avalon.presentation.addCharacterScreen.component.loader.TesseractLoader
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.AddCharacterLoadingState
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.AddCharacterScreenViewState
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.CreateStages
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.hasPendingImageUploads
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.imagesWithAvatarOnTop
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.isEnableBackButton
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.isEnableBackHandler
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.isEnableSegmentProgressBar
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.isShowAdvancesButton
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.isShowBackButton
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.isShowBottomButtons
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.isShowContinueButton
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.isShowPreviewCard
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.isShowPublishButton
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.presentation.commonComponents.SegmentedProgressBar
import com.ensodai.avalon.presentation.commonComponents.privacyCards.InteractionFormatCardColumn
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCharacterScreenContent(
    viewState: AddCharacterScreenViewState,
    actions: AddCharacterScreenActions,
    navigation: Navigation,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    AvalonBackHandler(viewState.isEnableBackHandler) {
        if (viewState.addCharacterLoadingState == AddCharacterLoadingState.Success) {
            navigation.navigateBack()
        }
        if (viewState.addCharacterLoadingState == AddCharacterLoadingState.Loading) {
            return@AvalonBackHandler
        }
        if (viewState.currentStages != CreateStages.NameStage) {
            actions.goToPreviousStage()
        }
    }

//    LaunchedEffect(viewState.characterHasSaved) {
//        if (viewState.characterHasSaved) {
//            delay(333)
//            navigation.navigateBack()
//        }
//    }

    LaunchedEffect(viewState.addCharacterLoadingState) {
        if (viewState.addCharacterLoadingState is AddCharacterLoadingState.Loading) {
            onDisableSwipeable()
        } else {
            onEnableSwipeable()
        }
    }

    AlertDialogsAddCharacterScreen(
        isShowErrorDialog = viewState.isShowErrorAlertDialog,
        isShowPermissionDialog = viewState.isShowPermissionDialog,
        errorMessage = viewState.errorMessage,
        onCloseAlertDialog = { actions.onCloseAlertDialog() },
        onClosePermissionAlertDialog = { actions.onClosePermissionDialog() }
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            Column {
                GemIslandTopBar(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = {
                        Text(
                            text = stringResource(Res.string.create),
                        )
                    },
                    navigationIcon = {
                        BaseItemAlphaAnimation(isVisible = viewState.isEnableBackButton) {
                            NavigationBackButton(
                                onBackButtonClick = { navigation.navigateBack() }
                            )
                        }
                    }
                )
                BaseItemAlphaAnimation(isVisible = viewState.isEnableSegmentProgressBar) {
                    SegmentedProgressBar(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        totalSteps = viewState.stagesList.size,
                        currentStep = viewState.stagesList.indexOf(viewState.currentStages) + 1,
                    )
                }
            }
        },
        bottomBar = {
            BaseItemAlphaAnimation(isVisible = viewState.isShowBottomButtons) {
                BottomBarAddCharacterScreen(
                    isShowContinueButton = viewState.isShowContinueButton,
                    isShowBackButton = viewState.isShowBackButton,
                    isShowPublishButton = viewState.isShowPublishButton,
                    isMainActionEnabled = !viewState.hasPendingImageUploads &&
                            viewState.addCharacterLoadingState !is AddCharacterLoadingState.Loading,
                    pendingUploadsMessage = if (viewState.hasPendingImageUploads) "Загружаем изображения..." else null,
                    goToNextStage = { actions.goToNextStage() },
                    goToPreviousStage = { actions.goToPreviousStage() },
                    publishCharacter = { actions.publishCharacter() },
                )
            }
        },
    ) { innerPadding ->

        Box {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
            ) {

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                when (viewState.currentStages) {
                    CreateStages.NameStage -> {
                        item {
                            NameStageScreen(
                                characterName = viewState.characterConfigState.characterName,
                                isErrorName = viewState.isErrorName,
                                onChangeName = {
                                    actions.onChangeName(it)
                                }
                            )
                        }
                    }

                    CreateStages.VisibleStage -> {
                        item {
                            VisibleStageScreen(
                                selectedOption = viewState.characterConfigState.characterPrivacy,
                                onOptionSelected = {
                                    actions.onChangeAvailabilityOption(it)
                                }
                            )
                        }
                    }

                    CreateStages.AvatarStage -> {
                        item {
                            AddCharacterAvatarStageScreen(
                                characterAvatar = viewState.characterAvatar,
                                characterImages = viewState.characterImages,
                                pendingUploads = viewState.pendingCharacterImageUploads,
                                isScrollableContent = true,
                                onAddImageClick = { },
                                onShowPermissionDialog = { actions.onShowPermissionDialog() },
                                onAttachImages = { actions.onAttachCharacterImages(it) },
                                onStartAttachingFiles = { },
                                onSelectImageAvatar = { actions.onSelectImageAvatar(it) },
                                onDeleteImage = { actions.onDeleteImage(it) },
                                onDeletePendingImage = { actions.onDeletePendingCharacterImage(it) }
                            )
                        }
                    }

                    CreateStages.ShortDescriptionStage -> {
                        item {
                            ShortDescriptionStageScreen(
                                shortDescription = viewState.characterConfigState.shortDescription,
                                isErrorShortDescription = viewState.isErrorShortDescription,
                                onChangeShortDescription = { actions.onChangeShortDescription(it) }
                            )
                        }
                    }

                    CreateStages.TemperatureStage -> {
                        item {
                            TemperatureStageScreen(
                                sliderPosition = viewState.characterConfigState.temperature,
                                onChangeTemperature = { actions.onChangeTemperature(it) }
                            )
                        }
                    }

                    CreateStages.CategoriesStage -> {
                        item {
                            CategoriesStageScreen(
                                selectedCategory = viewState.selectedCategory,
                                onCategorySelected = { actions.onCategorySelected(it) },
                                categories = viewState.categories,
                            )
                        }
                    }

                    CreateStages.FullDescriptionStage -> {
                        item {
                            FullDescriptionStagesScreen(
                                fullDescription = viewState.characterConfigState.fullDescription,
                                fullDescriptionPrivacy = viewState.characterConfigState.fullDescriptionPrivacy,
                                onChangeFullDescription = { actions.onChangeFullDescription(it) },
                                onChangeFullDescriptionPrivacy = {
                                    actions.onChangeFullDescriptionPrivacy(
                                        it
                                    )
                                }
                            )
                        }
                    }

                    CreateStages.ScenarioStage -> {
                        item {
                            ScenarioStageScreen(
                                scenario = viewState.characterConfigState.scenario,
                                onChangeScenario = { actions.onChangeScenario(it) }
                            )
                        }
                    }

                    CreateStages.MesExampleStage -> {
                        item {
                            MesExampleStageScreen(
                                mesExample = viewState.characterConfigState.mesExample,
                                onChangeMesExample = { actions.onChangeMesExample(it) }
                            )
                        }
                    }

                    CreateStages.LorebookStage -> {
                        item {
                            AddCharacterLorebookStageScreen(
                                selectedLorebook = viewState.selectedLorebook,
                                onOpenAttachBottomSheet = { actions.onOpenAttachLorebook() },
                                onOpenCreateDialog = { actions.onOpenCreateLorebook() },
                                onDetachLorebook = { actions.onDetachLorebook() }
                            )
                        }
                    }

                    CreateStages.PublishCharacterStage -> {
                        item {
                            PublishCharacterStageScreen(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                avatar = viewState.characterAvatar,
                                characterName = viewState.characterConfigState.characterName,
                                shortDescription = viewState.characterConfigState.shortDescription,
                                fullDescription = viewState.characterConfigState.fullDescription,
                                scenario = viewState.characterConfigState.scenario,
                                mesExample = viewState.characterConfigState.mesExample,
                                characterImages = viewState.imagesWithAvatarOnTop,
                                addCharacterLoadingState = viewState.addCharacterLoadingState,
                                firstMessage = viewState.characterConfigState.firstMessage,
                                welcomeCardDescription = viewState.characterConfigState.welcomeCardState.welcomeCardDescription,
                                welcomeCardImages = viewState.characterConfigState.welcomeCardState.welcomeCardImages,
                                isEnableLight = viewState.characterConfigState.welcomeCardState.isEnableLight,
                                welcomeCardStartColor = viewState.characterConfigState.welcomeCardState.startColor,
                                welcomeCardEndColor = viewState.characterConfigState.welcomeCardState.endColor,
                                isPublicFullDescription = viewState.characterConfigState.fullDescriptionPrivacy == CharacterVisibility.PUBLIC,
                                isShowAdvancedBlock = viewState.isShowAdvancesButton,
                                onClickAdvancedBlock = { actions.onShowAdvancedStages() },
                                selectedVoice = viewState.characterConfigState.voice,
                                isShowVoiceSection = viewState.advancedSettingsHaveBeenOpened,
                                onVoiceClick = { actions.onOpenVoicePicker() },
                                selectedInteractionFormat = viewState.characterConfigState.interactionFormat,
                                onInteractionFormatClick = { actions.onOpenInteractionFormatPicker() }
                            )
                        }
                    }

                    CreateStages.FirstMessageStage -> {
                        item {
                            FirstMessageStageScreen(
                                firstMessage = viewState.characterConfigState.firstMessage,
                                onChangeFirstMessage = { actions.onChangeFirstMessage(it) }
                            )
                        }
                    }

                    CreateStages.HelloCardStage -> {
                        item {
                            AddCharacterHelloCardStageScreen(
                                text = viewState.characterConfigState.welcomeCardState.welcomeCardDescription,
                                images = viewState.characterConfigState.welcomeCardState.welcomeCardImages,
                                pendingUploads = viewState.pendingWelcomeCardImageUploads,
                                isShowPreviewCard = viewState.isShowPreviewCard,
                                isEnableLight = viewState.characterConfigState.welcomeCardState.isEnableLight,
                                onChangeDescription = { actions.onChangeWelcomeCardDescription(it) },
                                onAttachImages = { actions.onAttachWelcomeCardImages(it) },
                                onShowPermissionDialog = { actions.onShowPermissionDialog() },
                                onDeleteImage = { actions.onDeleteWelcomeImage(it) },
                                onDeletePendingImage = { actions.onDeletePendingWelcomeImage(it) },
                                startColor = viewState.characterConfigState.welcomeCardState.startColor,
                                endColor = viewState.characterConfigState.welcomeCardState.endColor,
                                allowedColors = viewState.characterConfigState.welcomeCardState.allowedColors,
                                onSelectStartTone = { actions.onChangeWelcomeCardStartTone(it) },
                                onSelectEndTone = { actions.onChangeWelcomeCardEndTone(it) },
                                onToggleLight = { actions.onToggleLight() }
                            )
                        }
                    }

                    CreateStages.OutreachStage -> {
                        item {
                            OutreachStageScreen(
                                isOutreachEnabled = viewState.characterConfigState.isOutreachEnabled,
                                onOptionSelected = { actions.onChangeOutreachOption(it) }
                            )
                        }
                    }
                }
            }

            Box {
                BaseItemAlphaAnimation(isVisible = viewState.addCharacterLoadingState is AddCharacterLoadingState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        TesseractLoader()
                    }
                }
                BaseItemAlphaAnimation(isVisible = viewState.addCharacterLoadingState is AddCharacterLoadingState.Success) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        TechnoSuccessState(
                            avatarUrl = viewState.characterAvatar?.url,
                            characterName = viewState.characterConfigState.characterName,
                            onDoneClick = { navigation.navigateBack() }
                        )
                    }
                }
            }

            if (viewState.isShowVoicePicker) {
                VoiceModelPickerSheet(
                    selectedModel = viewState.characterConfigState.voice,
                    onSelect = { actions.onSelectVoice(it) },
                    onDismiss = { actions.onCloseVoicePicker() }
                )
            }

            if (viewState.isShowInteractionFormatPicker) {
                val colors = AvalonTheme.colors
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                )
                ModalBottomSheet(
                    sheetState = sheetState,
                    onDismissRequest = { actions.onCloseInteractionFormatPicker() },
                    containerColor = colors.background,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) },
                    contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .navigationBarsPadding()
                            .padding(bottom = 20.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.interaction_format_dialog_title),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.textMain,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        InteractionFormatCardColumn(
                            selectedFormat = viewState.characterConfigState.interactionFormat,
                            onOptionSelected = { actions.onChangeInteractionFormat(it) }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            AttachLorebookBottomSheet(
                isVisible = viewState.isShowAttachLorebookBottomSheet,
                onDismissRequest = { actions.onCloseAttachLorebook() },
                library = viewState.userLorebooksLibrary,
                attachedLorebookIds = viewState.characterConfigState.attachedLorebookIds.toSet(),
                isLoading = viewState.isLorebookActionLoading,
                onAttach = { actions.onSelectLorebook(it) },
                onImportLorebook = { actions.onImportLorebook(it) }
            )

            CreateLorebookDialog(
                isVisible = viewState.isShowCreateLorebookDialog,
                onDismissRequest = { actions.onCloseCreateLorebook() },
                name = viewState.createLorebookName,
                description = viewState.createLorebookDesc,
                isPublic = viewState.createLorebookIsPublic,
                isLoading = viewState.isLorebookActionLoading,
                onNameChanged = { actions.onChangeCreateLorebookName(it) },
                onDescriptionChanged = { actions.onChangeCreateLorebookDesc(it) },
                onIsPublicChanged = { actions.onToggleCreateLorebookPublic(it) },
                onCreateAndAttach = { actions.onCreateLorebookSubmit() }
            )
        }
    }
}
