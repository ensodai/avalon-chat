package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.link_only_setting
import com.ensodai.avalon.generated.resources.privacy
import com.ensodai.avalon.generated.resources.private_setting
import com.ensodai.avalon.generated.resources.public_setting
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrivacySettingSection(
    modifier: Modifier = Modifier,
    privacyOption: CharacterVisibility,
    onEditPrivacy: () -> Unit,
    isUpdating: Boolean
) {
    val privacyText = when (privacyOption) {
        CharacterVisibility.PUBLIC -> stringResource(Res.string.public_setting)
        CharacterVisibility.PRIVATE -> stringResource(Res.string.private_setting)
        CharacterVisibility.LINK_ONLY -> stringResource(Res.string.link_only_setting)
    }

    val privacyIcon = when (privacyOption) {
        CharacterVisibility.PUBLIC -> IconsManager.Public
        CharacterVisibility.PRIVATE -> IconsManager.Lock
        CharacterVisibility.LINK_ONLY -> IconsManager.Link
    }

    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.privacy),
        value = privacyText,
        icon = privacyIcon,
        actionType = EditActionType.SHEET,
        onClick = onEditPrivacy,
        isUpdating = isUpdating
    )
}
