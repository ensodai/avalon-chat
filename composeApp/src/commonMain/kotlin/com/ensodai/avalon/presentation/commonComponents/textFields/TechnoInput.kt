package com.ensodai.avalon.presentation.commonComponents.textFields

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.utils.EMPTY_STRING

@Composable
fun TechnoInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = EMPTY_STRING,
    label: String? = null,
    minLines: Int = 1,
    maxLines: Int = 1,
    maxLength: Int? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val colors = AvalonTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val targetBorderColor = when {
        isError -> colors.error
        isFocused -> colors.textMain
        else -> colors.strokeSubtle
    }

    val borderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = colors.textMain,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
        }

        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                val finalValue = if (maxLength != null && newValue.length > maxLength) {
                    newValue.take(maxLength)
                } else {
                    newValue
                }
                onValueChange(finalValue)
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .background(
                    color = colors.surfaceHigh,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = if (isFocused) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                ),
            enabled = enabled,
            textStyle = TextStyle(
                color = colors.textMain,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 24.sp
            ),
            cursorBrush = Brush.linearGradient(listOf(colors.neonAccent, colors.neonAccent)),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = maxLines == 1,
            minLines = minLines,
            maxLines = maxLines,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->

                val contentAlignment =
                    if (minLines > 1) Alignment.TopStart else Alignment.CenterStart

                val verticalPadding = if (minLines > 1) 16.dp else 4.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(vertical = verticalPadding),
                    contentAlignment = contentAlignment
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                color = colors.placeholderText,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (maxLength != null || isError) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isError) {
                    Text(
                        text = "Incorrect input",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.error
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (maxLength != null) {
                    Text(
                        text = "${value.length}/$maxLength",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (value.length >= maxLength) colors.error
                        else colors.textSecondary
                    )
                }
            }
        }
    }
}
