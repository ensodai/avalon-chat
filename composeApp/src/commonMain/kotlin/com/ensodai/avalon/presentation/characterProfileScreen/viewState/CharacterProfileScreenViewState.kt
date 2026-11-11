package com.ensodai.avalon.presentation.characterProfileScreen.viewState

import com.ensodai.shared.avalon.character.spec.AvalonCardCustomization
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.shared.avalon.character.models.CharacterLorebookBindingDto
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import com.ensodai.avalon.presentation.commonComponents.welcomCard.CardColor
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.core.ChatStatus
import com.ensodai.avalon.utils.EMPTY_STRING
import kotlin.uuid.Uuid
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff

data class CharacterProfileScreenViewState(
    val characterId: Uuid? = null,
    val isEnableDropDownButton: Boolean = true,
    val reportCharacterState: ReportCharacterStateProfileScreen = ReportCharacterStateProfileScreen(),
    val characterName: String = EMPTY_STRING,
    val ownerNick: String = EMPTY_STRING,
    val images: List<CharacterAvatarWithSeed> = emptyList(),
    val premiumImages: List<CharacterAvatarWithSeed> = emptyList(),
    val isPublicFullDescription: Boolean = false,
    val shortDescription: String = EMPTY_STRING,
    val fullDescription: String? = null,
    val isEnableEditProfileMode: Boolean = false,
    val startWithFullScreenImages: Boolean,
    val characterHasAvatars: Boolean,
    val typingStatus: ChatStatus = ChatStatus.Online,
    val sharedMedia: List<String> = emptyList(),
    val relationshipLevel: Int = 1,
    val experiencePoints: Long = 0L,
    val availableLlmConfigs: List<AvailableLlmConfigs> = emptyList(),
    val selectedConfig: AvailableLlmConfigs? = null,
    val userTariff: UserTariff = UserTariff.Free,
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null,
    val exportErrorMessage: String? = null,
    val isCardFlipped: Boolean = false,
    val cardCustomization: AvalonCardCustomization = AvalonCardCustomization(),
    val canExportCard: Boolean = false,
    val scenario: String? = null,
    val mesExample: String? = null,
    val lorebooks: List<CharacterLorebookBindingDto> = emptyList(),
    val userLibraryLorebooks: List<UserLorebookSummaryDto> = emptyList()
) : BaseViewState()

data class UpdatedCharacterProfile(
    val characterImages: List<CharacterImageDTO>? = emptyList(),
    val characterAvatar: CharacterImageDTO? = null,
    val welcomeCard: WelcomeCardState? = WelcomeCardState(),
    val editCharacterConfigState: EditCharacterConfigState = EditCharacterConfigState(),
)

data class WelcomeCardState(
    val welcomeCardDescription: String? = null,
    val welcomeCardImages: List<WelcomeCardImageDto> = emptyList(),
    val startColor: CardColor = CardColor.DEFAULT,
    val endColor: CardColor = CardColor.DEFAULT,
    val colorLight: CardColor = CardColor.DEFAULT,
    val allowedColors: List<CardColor> = CardColor.getSelectableColors(),
    val isEnableLight: Boolean = true
)

sealed class SavingState {
    object Default : SavingState()
    object Loading : SavingState()
    object Success : SavingState()
    class Error(val message: String) : SavingState()
}

data class EditCharacterConfigState(
    val characterName: String = EMPTY_STRING,
    val author: String = EMPTY_STRING,
    val characterPrivacy: CharacterVisibility = CharacterVisibility.PUBLIC,
    val fullDescriptionPrivacy: CharacterVisibility = CharacterVisibility.PUBLIC,
    val shortDescription: String = EMPTY_STRING,
    val fullDescription: String? = null,
    val temperature: Float = 0.5f,
    val harassment: Int = 3,
    val hate: Int = 3,
    val sexuallyExplicit: Int = 3,
    val dangerousContent: Int = 3,
    val firstMessage: String? = null,
    val welcomeCard: WelcomeCardState? = WelcomeCardState(),
)

data class ReportCharacterStateProfileScreen(
    val isShowReportDialog: Boolean = false,
    val reportMessage: String = EMPTY_STRING
)
