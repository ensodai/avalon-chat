package com.ensodai.avalon.presentation.commonComponents.topBarIcons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ensodai.avalon.theme.IconsManager

@Composable
fun NavigationBackButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onBackButtonClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        enabled = enabled,
        onClick = { onBackButtonClick() }
    ) {
        Icon(
            imageVector = IconsManager.ArrowBack,
            contentDescription = "Navigate back"
        )
    }
}
