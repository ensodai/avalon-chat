package com.ensodai.avalon.presentation.summaryExplanationScreen.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.summary_screen_top_bar_title
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopBarSummaryExplanationScreen(
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit
) {
    GemIslandTopBar(
        modifier = modifier,
        title = {
            Text(text = stringResource(Res.string.summary_screen_top_bar_title))
        },
        navigationIcon = {
            NavigationBackButton(
                onBackButtonClick = onBackButtonClick
            )
        }
    )
}
