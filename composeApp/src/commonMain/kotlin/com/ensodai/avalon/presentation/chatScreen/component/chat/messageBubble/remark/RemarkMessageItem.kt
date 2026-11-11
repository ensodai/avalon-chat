package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.remark

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.system_instruction_placeholder
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun RemarkMessageItem(
    isVisible: Boolean,
    selectedSystemInstructionText: String?,
    onChangeEditedSystemInstruction: (String) -> Unit
) {
    val colors = AvalonTheme.colors

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(animationSpec = tween(150)) + fadeIn(animationSpec = tween(150))
    ) {
        Column {
            Spacer(modifier = Modifier.height(8.dp))

            var textFieldValue by remember {
                mutableStateOf(
                    TextFieldValue(
                        selectedSystemInstructionText ?: ""
                    )
                )
            }

            LaunchedEffect(selectedSystemInstructionText) {
                if (selectedSystemInstructionText != null && textFieldValue.text != selectedSystemInstructionText) {
                    textFieldValue = textFieldValue.copy(
                        text = selectedSystemInstructionText,
                        selection = TextRange(selectedSystemInstructionText.length)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.strokeSubtle, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.overlaySurface)
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onChangeEditedSystemInstruction(newValue.text)
                    },
                    textStyle = TextStyle(
                        color = colors.textMain,
                        fontSize = 16.sp
                    ),
                    cursorBrush = Brush.linearGradient(listOf(colors.neonAccent, colors.neonAccent)),
                    modifier = Modifier.fillMaxWidth()
                )
                if (textFieldValue.text.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.system_instruction_placeholder),
                        style = TextStyle(
                            color = colors.placeholderText,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }

}
