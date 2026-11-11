package com.ensodai.avalon.presentation.addCharacterScreen.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.AddCharacterLoadingState
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.presentation.commonComponents.welcomCard.CardColor
import com.ensodai.shared.avalon.character.models.InteractionFormat

@Composable
fun PublishCharacterStageScreen(
    modifier: Modifier,
    characterImages: List<CharacterImageDTO>,
    avatar: CharacterImageDTO?,
    characterName: String,
    shortDescription: String?,
    fullDescription: String?,
    scenario: String? = null,
    mesExample: String? = null,
    addCharacterLoadingState: AddCharacterLoadingState,
    firstMessage: String?,
    welcomeCardDescription: String?,
    welcomeCardImages: List<WelcomeCardImageDto>,
    isEnableLight: Boolean,
    welcomeCardStartColor: CardColor,
    welcomeCardEndColor: CardColor,
    isPublicFullDescription: Boolean,
    isShowAdvancedBlock: Boolean,
    onClickAdvancedBlock: () -> Unit,
    selectedVoice: VoiceModel,
    isShowVoiceSection: Boolean,
    onVoiceClick: () -> Unit,
    selectedInteractionFormat: InteractionFormat,
    onInteractionFormatClick: () -> Unit
) {

    Box(
        modifier = modifier
    ) {
        BaseItemAlphaAnimation(
            isVisible = addCharacterLoadingState !is AddCharacterLoadingState.Loading
                    && addCharacterLoadingState !is AddCharacterLoadingState.Success
        ) {
            CharacterPreview(
                avatar = avatar,
                characterName = characterName,
                shortDescription = shortDescription,
                fullDescription = fullDescription,
                scenario = scenario,
                mesExample = mesExample,
                characterImages = characterImages,
                isAvatarStagesScreen = false,
                firstMessage = firstMessage,
                welcomeCardDescription = welcomeCardDescription,
                welcomeCardImages = welcomeCardImages,
                isEnableLight = isEnableLight,
                welcomeCardStartColor = welcomeCardStartColor,
                welcomeCardEndColor = welcomeCardEndColor,
                isPublicFullDescription = isPublicFullDescription,
                isShowAdvancedBlock = isShowAdvancedBlock,
                onClickAdvancedBlock = onClickAdvancedBlock,
                selectedVoice = selectedVoice,
                isShowVoiceSection = isShowVoiceSection,
                onVoiceClick = onVoiceClick,
                selectedInteractionFormat = selectedInteractionFormat,
                onInteractionFormatClick = onInteractionFormatClick
            )
        }
    }
}
