package com.ensodai.avalon.presentation.mainScreen.component.chatSection

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.archive
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArchiveItemChat(
    modifier: Modifier = Modifier,
    archivedCharacters: List<String>,
    onClick: () -> Unit,
) {

    val interactionSource = remember { MutableInteractionSource() }
    val customRipple = ripple(
        bounded = true
    )
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { },
                    indication = customRipple,
                    interactionSource = interactionSource
                )
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )

        ) {

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = com.ensodai.avalon.theme.AvalonTheme.colors.surfaceHigh,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.Center),
                    imageVector = IconsManager.Archive,
                    contentDescription = "Delete"
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = stringResource(Res.string.archive),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = com.ensodai.avalon.theme.AvalonTheme.colors.textMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = archivedCharacters.joinToString(", "),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = com.ensodai.avalon.theme.AvalonTheme.colors.textMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )


            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 76.dp),
            color = com.ensodai.avalon.theme.AvalonTheme.colors.strokeSubtle
        )
    }
}

//                Spacer(modifier = Modifier.height(6.dp))
//
//                Text(
//                    text = stringResource(Res.string.no_messages_yet),
//                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
//                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
