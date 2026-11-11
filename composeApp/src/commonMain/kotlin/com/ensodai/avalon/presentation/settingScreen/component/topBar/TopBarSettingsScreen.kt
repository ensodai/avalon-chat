package com.ensodai.avalon.presentation.settingScreen.component.topBar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.settings
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopBarSettingsScreen(
    onBackButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GemIslandTopBar(
        modifier = modifier,
        title = {
            Text(
                stringResource(Res.string.settings),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            NavigationBackButton(onBackButtonClick = { onBackButtonClick() })
        }
    )
}

