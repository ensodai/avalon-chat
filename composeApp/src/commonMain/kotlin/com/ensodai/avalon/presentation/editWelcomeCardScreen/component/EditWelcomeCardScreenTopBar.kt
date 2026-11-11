package com.ensodai.avalon.presentation.editWelcomeCardScreen.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.edit_chat
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditWelcomeCardScreenTopBar(
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit,
) {
    GemIslandTopBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(Res.string.edit_chat)
            )
        },
        navigationIcon = {
            NavigationBackButton(
                onBackButtonClick = { onBackButtonClick() }
            )
        }
    )
}

