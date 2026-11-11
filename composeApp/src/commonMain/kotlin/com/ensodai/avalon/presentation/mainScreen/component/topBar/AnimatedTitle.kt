package com.ensodai.avalon.presentation.mainScreen.component.topBar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.app_name
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import org.jetbrains.compose.resources.stringResource

@Composable
fun AnimatedTitle(
    isEnableSelectionChats: Boolean
) {

    BaseItemAlphaAnimation(isVisible = !isEnableSelectionChats) {
        Text(
            text = stringResource(Res.string.app_name),
        )
    }
}
