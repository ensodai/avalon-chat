package com.ensodai.avalon.presentation.userAiSettingsScreen.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.user_ai_settings_title
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopBarUserAiSettingsScreen(
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit
) {
    GemIslandTopBar(
        modifier = modifier,
        title = {
            Text(text = stringResource(Res.string.user_ai_settings_title))
        },
        navigationIcon = {
            NavigationBackButton(
                onBackButtonClick = onBackButtonClick
            )
        }
    )
}
