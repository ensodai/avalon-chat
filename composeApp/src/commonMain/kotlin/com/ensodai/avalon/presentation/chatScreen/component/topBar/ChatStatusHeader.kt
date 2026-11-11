package com.ensodai.avalon.presentation.chatScreen.component.topBar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.offline
import com.ensodai.avalon.generated.resources.online
import com.ensodai.avalon.generated.resources.typing
import com.ensodai.avalon.generated.resources.updating
import com.ensodai.avalon.presentation.core.ChatStatus
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatStatusHeader(
    status: ChatStatus,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    AnimatedContent(
        targetState = status,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith
                    fadeOut(animationSpec = tween(400))
        },
        label = "ChatStatusAnimation",
        modifier = modifier
    ) { currentStatus ->

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .height(20.dp)
        ) {
            when (currentStatus) {
                // 🟢 TYPING
                is ChatStatus.Typing -> {

                    TypingIndicator(
                        modifier = Modifier
                            .offset(y = 1.dp)
                            .padding(end = 6.dp)
//                            .padding(end = 4.dp)
//                            .size(12.dp)
                    )

                    Text(
                        text = stringResource(Res.string.typing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 🔵 ONLINE
                ChatStatus.Online -> {
                    Text(
                        text = stringResource(Res.string.online),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onlineStatus,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 🟡 UPDATING
                ChatStatus.Updating -> {
                    AnimatedDotsText(
                        baseText = stringResource(Res.string.updating),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }

                // 🔴 CONNECTING
                ChatStatus.Connecting -> {
                    AnimatedDotsText(
                        baseText = stringResource(Res.string.offline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.connectingStatus
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedDotsText(
    baseText: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color
) {
    // 0 -> 1 -> 2 -> 3 -> 0
    val dotCount by produceState(initialValue = 0) {
        while (true) {
            delay(400)
            value = (value + 1) % 4
        }
    }

    val dots = when (dotCount) {
        1 -> "."
        2 -> ".."
        3 -> "..."
        else -> ""
    }

    Text(
        text = "$baseText$dots",
        style = style,
        color = color,
        maxLines = 1
    )
}
