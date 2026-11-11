package com.ensodai.avalon.presentation.addCharacterScreen.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoInput
import com.ensodai.avalon.utils.EMPTY_STRING
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun FirstMessageStageScreen(
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

        TechnoInput(
            value = firstMessage ?: EMPTY_STRING,
            onValueChange = { newValue ->
                if (newValue.length <= 600) {
                    onChangeFirstMessage(newValue)
                } else {
                    val truncatedText = newValue.take(600)
                    onChangeFirstMessage(truncatedText)
                }
            },
            placeholder = stringResource(Res.string.message),
            maxLines = 6,
            minLines = 6,
            maxLength = 600,
            isError = false
        )
    }
}