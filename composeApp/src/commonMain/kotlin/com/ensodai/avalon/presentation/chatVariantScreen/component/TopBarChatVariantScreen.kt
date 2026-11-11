package com.ensodai.avalon.presentation.chatVariantScreen.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.stories
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopBarChatVariantScreen(
    modifier: Modifier = Modifier,
    onBackIconClick: () -> Unit,
) {
    GemIslandTopBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(Res.string.stories),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            NavigationBackButton(
                onBackButtonClick = {
                    onBackIconClick()
                }
            )
        }
    )
}

