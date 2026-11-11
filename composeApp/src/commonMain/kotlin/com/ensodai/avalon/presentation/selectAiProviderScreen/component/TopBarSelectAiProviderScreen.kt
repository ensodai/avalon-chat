package com.ensodai.avalon.presentation.selectAiProviderScreen.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.select_ai_provider_screen_title
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopBarSelectAiProviderScreen(
    modifier: Modifier = Modifier,
    isBackEnabled: Boolean = true,
    onBackButtonClick: () -> Unit
) {
    GemIslandTopBar(
        modifier = modifier,
        title = {
            Text(text = stringResource(Res.string.select_ai_provider_screen_title))
        },
        navigationIcon = {
            NavigationBackButton(
                enabled = isBackEnabled,
                onBackButtonClick = onBackButtonClick
            )
        }
    )
}
