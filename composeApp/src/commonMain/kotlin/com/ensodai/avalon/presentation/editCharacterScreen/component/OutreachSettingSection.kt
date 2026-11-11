package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.outreach_setting_title_short
import com.ensodai.avalon.generated.resources.outreach_setting_allowed_title
import com.ensodai.avalon.generated.resources.outreach_setting_blocked_title
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun OutreachSettingSection(
    modifier: Modifier = Modifier,
    isOutreachEnabled: Boolean,
    onEditOutreach: () -> Unit,
    isUpdating: Boolean
) {
    val outreachText = if (isOutreachEnabled) {
        stringResource(Res.string.outreach_setting_allowed_title)
    } else {
        stringResource(Res.string.outreach_setting_blocked_title)
    }

    val outreachIcon = IconsManager.Message

    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.outreach_setting_title_short),
        value = outreachText,
        icon = outreachIcon,
        actionType = EditActionType.SHEET,
        onClick = onEditOutreach,
        isUpdating = isUpdating
    )
}
