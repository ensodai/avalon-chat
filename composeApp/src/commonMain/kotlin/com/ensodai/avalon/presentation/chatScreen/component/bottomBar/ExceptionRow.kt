package com.ensodai.avalon.presentation.chatScreen.component.bottomBar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.api_key_replaced
import com.ensodai.avalon.generated.resources.generation_error
import com.ensodai.avalon.generated.resources.view
import com.ensodai.avalon.presentation.commonComponents.BlueClickableText
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExceptionRow(
    isShowExceptionRow: Boolean,
    isApiKeyReplaced: Boolean,
    onCloseExceptionRow: () -> Unit,
    onShowExceptionDialog: () -> Unit,
) {

    val boxHeight by animateDpAsState(
        targetValue = if (isShowExceptionRow) 30.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "boxHeight"
    )

    if (boxHeight > 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(boxHeight)
                .padding(
                    start = 8.dp,
                    end = 8.dp
                )
                .padding(vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isApiKeyReplaced) {
                    Text(
                        text = stringResource(Res.string.api_key_replaced),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Row(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(Res.string.generation_error))
                        BlueClickableText(
                            text = stringResource(Res.string.view),
                            onClick = { onShowExceptionDialog() }
                        )
                    }
                }

                IconButton(
                    onClick = { onCloseExceptionRow() },
                    modifier = Modifier
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = IconsManager.Close,
                        contentDescription = "Close Exception Row",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}



