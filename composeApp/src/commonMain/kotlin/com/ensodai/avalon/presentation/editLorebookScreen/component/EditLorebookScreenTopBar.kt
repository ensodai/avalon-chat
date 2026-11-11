package com.ensodai.avalon.presentation.editLorebookScreen.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.lorebook_export_action
import com.ensodai.avalon.generated.resources.lorebook_title
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditLorebookScreenTopBar(
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit,
    onExportClick: (() -> Unit)? = null,
    isExporting: Boolean = false
) {
    val colors = AvalonTheme.colors
    GemIslandTopBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(Res.string.lorebook_title),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textMain
            )
        },
        navigationIcon = {
            NavigationBackButton(
                onBackButtonClick = onBackButtonClick
            )
        },
        actions = if (onExportClick != null) {
            {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.neonAccent
                    )
                } else {
                    IconButton(onClick = onExportClick) {
                        Icon(
                            imageVector = IconsManager.Download,
                            contentDescription = stringResource(Res.string.lorebook_export_action),
                            tint = colors.textMain
                        )
                    }
                }
            }
        } else null
    )
}
