package com.ensodai.avalon.presentation.characterProfileScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.character_profile_detach_lorebook_desc
import com.ensodai.avalon.generated.resources.character_profile_detach_lorebook_title
import com.ensodai.avalon.generated.resources.full_description_stage
import com.ensodai.avalon.generated.resources.mes_example_stage
import com.ensodai.avalon.generated.resources.scenario_stage
import com.ensodai.avalon.navigation.avalonCustomSwipe
import com.ensodai.avalon.presentation.characterLorebooksScreen.component.AttachLorebookBottomSheet
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogDeleteItem
import kotlin.uuid.Uuid
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.characterProfileScreen.action.CharacterProfileScreenActions
import com.ensodai.avalon.presentation.characterProfileScreen.component.CharacterAuthorSection
import com.ensodai.avalon.presentation.characterProfileScreen.component.FriendshipOutreachSection
import com.ensodai.avalon.presentation.characterProfileScreen.component.ProfileExpandableTextSection
import com.ensodai.avalon.presentation.characterProfileScreen.component.ProfileLorebooksSection
import com.ensodai.avalon.presentation.characterProfileScreen.component.ShortDescriptionSection
import com.ensodai.avalon.presentation.characterProfileScreen.component.SharedMediaSection
import com.ensodai.avalon.presentation.characterProfileScreen.component.FriendshipProgressSection
import com.ensodai.avalon.presentation.characterProfileScreen.component.PremiumGallerySection
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.presentation.characterProfileScreen.component.CharacterModelSection
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.ToolbarConfig
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.gestureExtention.collapsibleToolbarGestures
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar.CollapsibleTopBar
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterProfileScreenViewState
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogComplaint
import com.ensodai.avalon.presentation.commonComponents.bottomSheet.BuyEnergyBottomSheet
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ensodai.avalon.presentation.commonComponents.copyToClipboardAndShowToast
import com.ensodai.avalon.presentation.commonComponents.toastNotify
import com.ensodai.avalon.presentation.navigation.Navigation

@Composable
fun CharacterProfileScreenContent(
    viewState: CharacterProfileScreenViewState,
    actions: CharacterProfileScreenActions,
    navigation: Navigation,
    toolbarConfig: ToolbarConfig
) {
    val copyToClipboard = copyToClipboardAndShowToast()
    var showBuyEnergySheet by remember { mutableStateOf(false) }
    var showAttachLorebookSheet by remember { mutableStateOf(false) }
    var lorebookToDetach by remember { mutableStateOf<Uuid?>(null) }
    val toolbarHeightAnimatable = remember {
        Animatable(toolbarConfig.defaultOpenScreenHeight, Dp.VectorConverter)
    }
    val listState = rememberLazyListState()

    AlertDialogComplaint(
        isShow = viewState.reportCharacterState.isShowReportDialog,
        text = viewState.reportCharacterState.reportMessage,
        onChangeText = { actions.onChangeReportMessage(it) },
        onDismiss = { actions.onCloseReportDialog() },
        onConfirm = { actions.onReportCharacter() }
    )

    val notify = toastNotify()
    LaunchedEffect(viewState.exportSuccessMessage) {
        viewState.exportSuccessMessage?.let {
            notify(it)
            actions.onDismissExportMessage()
        }
    }
    LaunchedEffect(viewState.exportErrorMessage) {
        viewState.exportErrorMessage?.let {
            notify(it)
            actions.onDismissExportMessage()
        }
    }

    Box(
        modifier = Modifier
            .testTag(BenchmarkTags.CharacterProfileScreen)
            .collapsibleToolbarGestures(
                toolbarConfig = toolbarConfig,
                toolbarHeightAnimatable = toolbarHeightAnimatable,
                listState = listState,
                isEditMode = false,
                isOnlyHalfExpanded = viewState.images.isEmpty()
            )
    ) {
        Scaffold(
            topBar = {
                CollapsibleTopBar(
                    toolbarConfig = toolbarConfig,
                    currentHeight = toolbarHeightAnimatable.value,
                    characterImages = viewState.images,
                    defaultImageSeed = viewState.characterId.toString() + viewState.characterName,
                    characterName = viewState.characterName,
                    ownerNick = viewState.ownerNick,
                    shortDescription = viewState.shortDescription,
                    isCardFlipped = viewState.isCardFlipped,
                    cardCustomization = viewState.cardCustomization,
                    status = viewState.typingStatus,
                    isEnableDropDownButton = viewState.isEnableDropDownButton,
                    isEnableEditProfileMode = viewState.isEnableEditProfileMode,
                    canExportCard = viewState.canExportCard,
                    onBackButtonClick = { navigation.navigateBack() },
                    onEnableEditMode = {
                        if (viewState.characterId != null) {
                            navigation.navigateToEditCharacterScreen(viewState.characterId)
                        }
                    },
                    onClickChatButton = {
//                        navigation.navigateToChatScreenFromProfile(
//                            characterPublicId = characterPublicId
//                        )
                    },
                    onReportCharacter = { actions.onShowReportDialog() },
                    onExportCharacterCard = actions.onExportCharacterCard,
                    onToggleCardFlip = actions.onToggleCardFlip,
                    onSetCardFlipped = actions.onSetCardFlipped
                )
            }
        ) { innerPadding ->
            val innerPadding = innerPadding
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .avalonCustomSwipe { dragDeltaX ->
                        dragDeltaX > 0
                    },
                state = listState,
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 8.dp
                ),
                userScrollEnabled = false,
            ) {

//                item {
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//                item {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        FriendshipProgressSection(
//                            modifier = Modifier.weight(1f),
//                            relationshipLevel = viewState.relationshipLevel,
//                            experiencePoints = viewState.experiencePoints,
//                            onClick = {
//                                if (viewState.characterId != null) {
//                                    navigation.navigateToRelationshipRoadScreen(viewState.characterId)
//                                }
//                            }
//                        )
//                        FriendshipOutreachSection(
//                            modifier = Modifier.weight(1f),
//                            relationshipLevel = viewState.relationshipLevel,
//                            onClick = {
//                                if (viewState.characterId != null) {
//                                    navigation.navigateToCharacterOutreachSettings(viewState.characterId)
//                                }
//                            }
//                        )
//                        CharacterModelSection(
//                            modifier = Modifier.weight(1f),
//                            selectedConfig = viewState.selectedConfig,
//                            availableLlmConfigs = viewState.availableLlmConfigs,
//                            userTariff = viewState.userTariff,
//                            onModelSelected = { actions.onSelectActiveLlmConfig(it) },
//                            onNavigateToPayment = { navigation.navigateToPaymentScreen() }
//                        )
//                    }
//                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    CharacterAuthorSection(
                        copyToClipboard = { copyToClipboard("@${viewState.ownerNick}") },
                        nickName = viewState.ownerNick
                    )
                }

                if (viewState.shortDescription.isNotBlank()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        ShortDescriptionSection(
                            copyToClipboard = { copyToClipboard(viewState.shortDescription) },
                            shortDescription = viewState.shortDescription
                        )
                    }
                }

                if (!viewState.fullDescription.isNullOrBlank() && (viewState.isPublicFullDescription || viewState.isEnableEditProfileMode)) {
                    item(key = "profile_personality") {
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileExpandableTextSection(
                            title = stringResource(Res.string.full_description_stage),
                            icon = IconsManager.Description,
                            text = viewState.fullDescription,
                            copyToClipboard = { copyToClipboard(viewState.fullDescription) }
                        )
                    }
                }

                if (!viewState.scenario.isNullOrBlank()) {
                    item(key = "profile_scenario") {
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileExpandableTextSection(
                            title = stringResource(Res.string.scenario_stage),
                            icon = IconsManager.Tune,
                            text = viewState.scenario,
                            copyToClipboard = { copyToClipboard(viewState.scenario) }
                        )
                    }
                }

                if (!viewState.mesExample.isNullOrBlank()) {
                    item(key = "profile_mes_example") {
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileExpandableTextSection(
                            title = stringResource(Res.string.mes_example_stage),
                            icon = IconsManager.ChatBubbleOutline,
                            text = viewState.mesExample,
                            copyToClipboard = { copyToClipboard(viewState.mesExample) }
                        )
                    }
                }

                if (viewState.characterId != null) {
                    item(key = "profile_lorebooks") {
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileLorebooksSection(
                            lorebooks = viewState.lorebooks,
                            canManage = viewState.isEnableEditProfileMode,
                            onManageClick = { navigation.navigateToCharacterLorebooksScreen(viewState.characterId) },
                            onLorebookClick = { lorebookId -> navigation.navigateToLorebookDetailsScreen(lorebookId) },
                            onDetachLorebook = { lorebookId ->
                                lorebookToDetach = lorebookId
                            },
                            onAddLorebookClick = {
                                actions.onLoadUserLorebooksLibrary()
                                showAttachLorebookSheet = true
                            }
                        )
                    }
                }

                if (viewState.premiumImages.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        PremiumGallerySection(
                            premiumPhotos = viewState.premiumImages,
                            onUnlockClick = { actions.onUnlockImage(it) },
                            onNotEnoughEnergy = { showBuyEnergySheet = true }
                        )
                    }
                }

                if (viewState.sharedMedia.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SharedMediaSection(
                            sharedMedia = viewState.sharedMedia
                        )
                    }
                }
            }
        }

        if (showBuyEnergySheet) {
            BuyEnergyBottomSheet(
                onDismiss = { showBuyEnergySheet = false }
            )
        }

        AttachLorebookBottomSheet(
            isVisible = showAttachLorebookSheet,
            onDismissRequest = { showAttachLorebookSheet = false },
            library = viewState.userLibraryLorebooks,
            attachedLorebookIds = viewState.lorebooks.map { it.lorebookId }.toSet(),
            isLoading = false,
            onAttach = { lorebookId ->
                actions.onAttachUserLorebook(lorebookId)
                showAttachLorebookSheet = false
            },
            onImportLorebook = { file ->
                actions.onImportLorebook(file)
                showAttachLorebookSheet = false
            }
        )

        AlertDialogDeleteItem(
            title = stringResource(Res.string.character_profile_detach_lorebook_title),
            message = stringResource(Res.string.character_profile_detach_lorebook_desc),
            isShowDialog = lorebookToDetach != null,
            onDismiss = { lorebookToDetach = null },
            onConfirm = {
                lorebookToDetach?.let { actions.onDetachUserLorebook(it) }
                lorebookToDetach = null
            }
        )
    }
}
