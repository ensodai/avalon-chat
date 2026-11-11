package com.ensodai.avalon.presentation.mainScreen.component.chatSection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.telegram_reminder_text
import com.ensodai.avalon.presentation.commonComponents.rememberTelegramOpener
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun TelegramReminderItem(onClose: () -> Unit) {
    val telegramOpener = rememberTelegramOpener()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(
                color = com.ensodai.avalon.theme.AvalonTheme.colors.surfaceHigh,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(Res.string.telegram_reminder_text),
                style = MaterialTheme.typography.bodyMedium,
                color = com.ensodai.avalon.theme.AvalonTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = IconsManager.Message,
                    contentDescription = null,
                    tint = com.ensodai.avalon.theme.AvalonTheme.colors.infoAccent,
                    modifier = Modifier.size(20.dp),
                )

                Spacer(modifier = Modifier.width(4.dp))

                TextButton(
                    onClick = { telegramOpener.openChannel() },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = "@avalon_avatars",
                        style = MaterialTheme.typography.labelLarge,
                        color = com.ensodai.avalon.theme.AvalonTheme.colors.infoAccent
                    )
                }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = IconsManager.Close,
                contentDescription = "Р—Р°РєСЂС‹С‚СЊ",
                tint = com.ensodai.avalon.theme.AvalonTheme.colors.textSecondary
            )
        }
    }
}
