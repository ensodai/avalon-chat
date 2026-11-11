package com.ensodai.avalon.presentation.characterLorebooksScreen.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.character_lorebooks_title
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun CharacterLorebooksTopBar(
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    GemIslandTopBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(Res.string.character_lorebooks_title),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textMain
            )
        },
        navigationIcon = {
            NavigationBackButton(
                onBackButtonClick = onBackButtonClick
            )
        }
    )
}
