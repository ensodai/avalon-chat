package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.set_up_a_welcome_card
import com.ensodai.avalon.generated.resources.welcome_card
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeCardSection(
    modifier: Modifier = Modifier,
    onEditWelcomeCard: () -> Unit
) {
    EditFieldCard(
        modifier = modifier,
        title = stringResource(Res.string.welcome_card),
        value = stringResource(Res.string.set_up_a_welcome_card),
        icon = IconsManager.DashboardCustomize,
        actionType = EditActionType.NAVIGATION,
        onClick = onEditWelcomeCard,
        isUpdating = false
    )
}
