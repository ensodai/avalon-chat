package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.CharacterCardExportFormat
import com.ensodai.shared.avalon.character.spec.AvalonCardCustomization
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.ToolbarConfig
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.calculateImageLayoutState
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar.actionButtonsSection.ActionButtonsRow
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar.imageSection.ImageAvatarCharacterSection
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.CharacterAvatarWithSeed
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.presentation.core.ChatStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsibleTopBar(
    status: ChatStatus,
    toolbarConfig: ToolbarConfig,
    currentHeight: Dp,
    characterImages: List<CharacterAvatarWithSeed>,
    defaultImageSeed: String,
    characterName: String,
    ownerNick: String = "",
    shortDescription: String = "",
    isCardFlipped: Boolean = false,
    cardCustomization: AvalonCardCustomization = AvalonCardCustomization(),
    isEnableDropDownButton: Boolean,
    isEnableEditProfileMode: Boolean,
    canExportCard: Boolean = false,
    onBackButtonClick: () -> Unit,
    onEnableEditMode: () -> Unit,
    onClickChatButton: () -> Unit,
    onReportCharacter: () -> Unit,
    onExportCharacterCard: (CharacterCardExportFormat) -> Unit = {},
    onToggleCardFlip: () -> Unit = {},
    onSetCardFlipped: (Boolean) -> Unit = {}
) {

    var parentWidth by remember { mutableStateOf(0.dp) }
    var parentHeight by remember { mutableStateOf(0.dp) }
    val imageLayoutState =
        calculateImageLayoutState(currentHeight, toolbarConfig, parentWidth, parentHeight)

    LaunchedEffect(imageLayoutState.isExpanded) {
        if (!imageLayoutState.isExpanded && isCardFlipped) {
            onSetCardFlipped(false)
        }
    }

    Box {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(currentHeight)
                .align(Alignment.TopCenter),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                parentWidth = maxWidth
                parentHeight = maxHeight

                Box(modifier = Modifier.fillMaxSize()) {

                    ImageAvatarCharacterSection(
                        imageLayoutState = imageLayoutState,
                        characterAvatars = characterImages,
                        defaultSeed = defaultImageSeed,
                        isCardFlipped = isCardFlipped,
                        cardCustomization = cardCustomization,
                        characterName = characterName,
                        creatorNick = ownerNick,
                        shortDescription = shortDescription,
                        onToggleCardFlip = onToggleCardFlip
                    )

                    ActionButtonsRow(
                        isEnableEditProfileMode = isEnableEditProfileMode,
                        isEnableDropDownButton = isEnableDropDownButton,
                        isCardFlipped = isCardFlipped,
                        isExpanded = imageLayoutState.isExpanded,
                        canExportCard = canExportCard,
                        onBackButtonClick = onBackButtonClick,
                        onEnableEditProfileMode = onEnableEditMode,
                        onReportCharacter = onReportCharacter,
                        onExportCharacterCard = onExportCharacterCard,
                        onToggleCardFlip = onToggleCardFlip
                    )

                    BaseItemAlphaAnimation(isVisible = !isCardFlipped) {
                        CharacterNameSection(
                            currentHeight = currentHeight,
                            imageLayoutState = imageLayoutState,
                            toolbarConfig = toolbarConfig,
                            characterName = characterName,
                            status = status
                        )
                    }
                }
            }
        }

//        ChatButton(
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .offset(y = (30).dp, x = (-30).dp)
//                .size(60.dp),
//            isEnableChatButton = imageLayoutState.isEnableChatButton,
//            onClick = { onClickChatButton() }
//        )
    }
}
