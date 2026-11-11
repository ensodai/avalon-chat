package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar.actionButtonsSection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.CharacterCardExportFormat
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.presentationUtils.statusBarPaddingDp
import com.ensodai.avalon.theme.IconsManager

@Composable
fun ActionButtonsRow(
    isEnableEditProfileMode: Boolean,
    isEnableDropDownButton: Boolean,
    isCardFlipped: Boolean = false,
    isExpanded: Boolean = true,
    canExportCard: Boolean = false,
    onBackButtonClick: () -> Unit,
    onEnableEditProfileMode: () -> Unit,
    onReportCharacter: () -> Unit,
    onExportCharacterCard: (CharacterCardExportFormat) -> Unit = {},
    onToggleCardFlip: () -> Unit = {}
) {

    Row(
        modifier = Modifier.padding(
            start = 6.dp,
            top = 6.dp + statusBarPaddingDp(),
            end = 6.dp
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        NavigationBackButton(onBackButtonClick = onBackButtonClick)

        Spacer(modifier = Modifier.weight(1f))

        if (isExpanded) {
            IconButton(
                onClick = onToggleCardFlip
            ) {
                Icon(
                    imageVector = if (isCardFlipped) IconsManager.Image else IconsManager.Badge,
                    contentDescription = if (isCardFlipped) "Показать фото" else "Показать карточку Monolith",
                    tint = if (isCardFlipped) Color(0xFF00E5FF) else Color.White
                )
            }
        }

        DropDownButtonCharacterProfile(
            isEnableEditButton = isEnableEditProfileMode,
            isEnableDropDownButton = isEnableDropDownButton,
            canExportCard = canExportCard,
            onEnableEditProfileMode = onEnableEditProfileMode,
            onReportCharacter = onReportCharacter,
            onExportCharacterCard = onExportCharacterCard
        )
    }
}
