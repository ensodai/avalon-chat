package com.ensodai.avalon.presentation.importCharacterScreen.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton

@Composable
fun TopBarImportCharacterScreen(
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit
) {
    GemIslandTopBar(
        modifier = modifier,
        title = {
            Text(text = "Импорт персонажа")
        },
        navigationIcon = {
            NavigationBackButton(
                onBackButtonClick = onBackButtonClick
            )
        }
    )
}
