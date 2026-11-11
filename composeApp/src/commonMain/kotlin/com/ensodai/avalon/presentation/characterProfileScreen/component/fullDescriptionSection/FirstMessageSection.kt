package com.ensodai.avalon.presentation.characterProfileScreen.component.fullDescriptionSection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.first_message
import com.ensodai.avalon.generated.resources.first_message_stage
import com.ensodai.avalon.generated.resources.message
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.utils.EMPTY_STRING
import org.jetbrains.compose.resources.stringResource

@Composable
fun FirstMessageSection(
    firstMessage: String?,
    onChangeFirstMessage: (String) -> Unit,
) {
    val colors = AvalonTheme.colors

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(Res.string.first_message_stage),
            style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMain
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.first_message),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column {
            TextField(
                value = firstMessage ?: EMPTY_STRING,
                onValueChange = { newValue ->
                    onChangeFirstMessage(newValue)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                minLines = 6,
                maxLines = 6,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceHigh,
                    unfocusedContainerColor = colors.surfaceHigh,
                    cursorColor = colors.neonAccent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                placeholder = {
                    Text(
                        text = stringResource(Res.string.message),
                        color = colors.placeholderText,
                    )
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            val count = firstMessage?.length ?: 0
            Text(
                text = "${count}/600",
                modifier = Modifier.align(Alignment.End),
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
