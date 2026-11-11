package com.ensodai.avalon.presentation.commonComponents.textFields

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun TechnoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    maxLength: Int = 30,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    val colors = AvalonTheme.colors
    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = { onValueChange(it.take(maxLength)) },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isError) colors.error else colors.strokeActive,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceHigh,
                unfocusedContainerColor = colors.surfaceHigh,
                errorContainerColor = colors.surfaceHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = colors.neonAccent,
                focusedTextColor = colors.textMain,
                unfocusedTextColor = colors.textMain,
                focusedPlaceholderColor = colors.textSecondary,
                unfocusedPlaceholderColor = colors.textSecondary.copy(alpha = 0.5f)
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            isError = isError,
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${value.length}/$maxLength",
            modifier = Modifier.align(Alignment.End),
            color = if (isError) colors.error else colors.textSecondary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
