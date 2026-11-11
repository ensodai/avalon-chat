package com.ensodai.avalon.presentation.editCharacterScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.navigation.avalonCustomSwipe
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.editCharacterScreen.action.EditCharacterActions
import com.ensodai.avalon.presentation.editCharacterScreen.component.BottomSheetEditCharacterScreen
import com.ensodai.avalon.presentation.editCharacterScreen.component.CharacterImagesSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.CharacterNameSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.CharacterPrivatePhotosSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.FirstCharacterMessageSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.FullDescriptionSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.InteractionFormatSettingSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.LorebookSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.MesExampleSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.OutreachSettingSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.PrivacySettingSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.ScenarioSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.ShortDescriptionSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.TemperatureSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.VoiceSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.WelcomeCardSection
import com.ensodai.avalon.presentation.editCharacterScreen.component.CharacterEventsSection
import com.ensodai.avalon.presentation.editCharacterTextScreen.model.CharacterTextType
import com.ensodai.avalon.presentation.liveCallScreen.component.VoiceModelPickerSheet
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.avalon.presentation.editCharacterScreen.component.topBar.TopBarEditCharacterScreen
import com.ensodai.avalon.presentation.editCharacterScreen.viewState.CurrentUpdatingSection
import com.ensodai.avalon.presentation.editCharacterScreen.viewState.EditCharacterViewState
import com.ensodai.avalon.presentation.navigation.Navigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChatConfigScreenContent(
    actions: EditCharacterActions,
    viewState: EditCharacterViewState,
    navigation: Navigation
) {

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(BenchmarkTags.EditCharacterScreen)
            .avalonCustomSwipe { dragDeltaX ->
                dragDeltaX > 0
            },
        topBar = {
            TopBarEditCharacterScreen(
                modifier = Modifier
                    .fillMaxWidth(),
                onBackButtonClick = { navigation.navigateBack() }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(top = 16.dp)
                .padding(innerPadding)
        ) {
            CharacterImagesSection(
                modifier = Modifier.testTag(BenchmarkTags.EditCharacterImagesCard),
                images = viewState.images.filter { !it.isPrivate },
                onEditImages = {
                    if (viewState.characterId != null) {
                        navigation.navigateToEditCharacterAvatarScreen(viewState.characterId)
                    }
                }
            )
//            Spacer(modifier = Modifier.height(16.dp))
//            CharacterPrivatePhotosSection(
//                images = viewState.images.filter { it.isPrivate },
//                onEditPrivatePhotos = {
//                    if (viewState.characterId != null) {
//                        navigation.navigateToEditCharacterPrivatePhotosScreen(viewState.characterId)
//                    }
//                }
//            )
            Spacer(modifier = Modifier.height(16.dp))
            CharacterNameSection(
                modifier = Modifier.testTag(BenchmarkTags.EditCharacterNameCard),
                characterName = viewState.characterName,
                isUpdating = viewState.currentUpdatingSection is CurrentUpdatingSection.Name,
                onEditName = {
                    actions.bottomSheetActions.onOpenEditNameBottomSheet()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ShortDescriptionSection(
                modifier = Modifier.testTag(BenchmarkTags.EditCharacterShortDescriptionCard),
                shortDescription = viewState.shortDescription,
                isUpdating = viewState.currentUpdatingSection is CurrentUpdatingSection.Description,
                onEditShortDescription = {
                    actions.bottomSheetActions.onOpenEditDescriptionBottomSheet()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            FullDescriptionSection(
                modifier = Modifier.testTag(BenchmarkTags.EditCharacterFullDescriptionCard),
                fullDescription = viewState.fullDescription,
                onEditFullDescription = {
                    if (viewState.characterId != null) {
                        navigation.navigateToEditCharacterTextScreen(
                            characterId = viewState.characterId,
                            textType = CharacterTextType.PERSONALITY
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ScenarioSection(
                modifier = Modifier.testTag(BenchmarkTags.EditCharacterScenarioCard),
                scenario = viewState.scenario,
                isUpdating = viewState.currentUpdatingSection is CurrentUpdatingSection.Scenario,
                onEditScenario = {
                    if (viewState.characterId != null) {
                        navigation.navigateToEditCharacterTextScreen(
                            characterId = viewState.characterId,
                            textType = CharacterTextType.SCENARIO
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            MesExampleSection(
                modifier = Modifier.testTag(BenchmarkTags.EditCharacterMesExampleCard),
                mesExample = viewState.mesExample,
                isUpdating = viewState.currentUpdatingSection is CurrentUpdatingSection.MesExample,
                onEditMesExample = {
                    if (viewState.characterId != null) {
                        navigation.navigateToEditCharacterTextScreen(
                            characterId = viewState.characterId,
                            textType = CharacterTextType.MES_EXAMPLE
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TemperatureSection(
                modifier = Modifier.testTag(BenchmarkTags.EditCharacterTemperatureCard),
                temperature = viewState.temperature,
                onEditTemperature = {
                    if (viewState.characterId == null) return@TemperatureSection
                    navigation.navigateToEditTemperatureScreen(viewState.characterId)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            FirstCharacterMessageSection(
                modifier = Modifier.testTag(BenchmarkTags.EditCharacterFirstMessageCard),
                firstMessage = viewState.firstMessage,
                isUpdating = viewState.currentUpdatingSection is CurrentUpdatingSection.FirstMessage,
                onEditFirstMessage = {
                    actions.bottomSheetActions.onOpenEditFirstMessageBottomSheet()
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            WelcomeCardSection(
                modifier = Modifier.testTag(BenchmarkTags.EditCharacterWelcomeCard),
                onEditWelcomeCard = {
                    if (viewState.characterId != null) {
                        navigation.navigateToEditWelcomeCardScreen(characterId = viewState.characterId)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            LorebookSection(
                modifier = Modifier.testTag(BenchmarkTags.EditCharacterLorebookCard),
                onEditLorebook = {
                    if (viewState.characterId != null) {
                        navigation.navigateToCharacterLorebooksScreen(characterId = viewState.characterId)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PrivacySettingSection(
                modifier = Modifier.testTag(BenchmarkTags.EditCharacterPrivacyCard),
                privacyOption = viewState.visibility,
                isUpdating = viewState.currentUpdatingSection is CurrentUpdatingSection.Visibility,
                onEditPrivacy = {
                    actions.bottomSheetActions.onOpenEditVisibilityBottomSheet()
                }
            )
//            Spacer(modifier = Modifier.height(16.dp))
//            OutreachSettingSection(
//                isOutreachEnabled = viewState.isOutreachEnabled,
//                isUpdating = viewState.currentUpdatingSection is CurrentUpdatingSection.Outreach,
//                onEditOutreach = {
//                    actions.bottomSheetActions.onOpenEditOutreachBottomSheet()
//                }
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            VoiceSection(
//                voice = viewState.voice,
//                onEditVoice = {
//                    actions.bottomSheetActions.onOpenVoicePicker()
//                },
//                isUpdating = viewState.currentUpdatingSection is CurrentUpdatingSection.Voice
//            )
            Spacer(modifier = Modifier.height(16.dp))
            InteractionFormatSettingSection(
                interactionFormat = viewState.interactionFormat,
                onEditInteractionFormat = {
                    actions.bottomSheetActions.onOpenEditInteractionFormatBottomSheet()
                },
                isUpdating = viewState.currentUpdatingSection is CurrentUpdatingSection.InteractionFormat
            )
//            Spacer(modifier = Modifier.height(16.dp))
//            CharacterEventsSection(
//                onEditEvents = {
//                    if (viewState.characterId != null) {
//                        navigation.navigateToCharacterEventsScreen(characterId = viewState.characterId)
//                    }
//                }
//            )
        }
    }
    BottomSheetEditCharacterScreen(
        state = viewState.bottomSheet,
        bottomSheetActions = actions.bottomSheetActions,
        bottomSheetEditActions = actions.bottomSheetEditActions,
        loadingState = viewState.loadingState,
        characterName = viewState.characterName,
        characterDescription = viewState.shortDescription,
        characterVisibility = viewState.visibility,
        firstMessage = viewState.firstMessage,
        isOutreachEnabled = viewState.isOutreachEnabled,
        interactionFormat = viewState.interactionFormat,
        scenario = viewState.scenario,
        mesExample = viewState.mesExample
    )
    if (viewState.isShowVoicePicker) {
        VoiceModelPickerSheet(
            selectedModel = viewState.voice,
            onSelect = { actions.bottomSheetEditActions.onUpdateCharacterVoice(it) },
            onDismiss = { actions.bottomSheetActions.onCloseVoicePicker() }
        )
    }
}
