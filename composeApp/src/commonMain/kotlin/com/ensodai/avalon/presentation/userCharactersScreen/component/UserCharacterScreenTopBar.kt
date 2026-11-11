package com.ensodai.avalon.presentation.userCharactersScreen.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.characters_screen_top_bar
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun UserCharacterScreenTopBar(
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit
) {
    GemIslandTopBar(
        modifier = modifier,
        title = {
            Text(
                stringResource(Res.string.characters_screen_top_bar),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            NavigationBackButton(
                onBackButtonClick = onBackButtonClick
            )
        }
    )
}

