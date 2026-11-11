package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun HighlightClickableText(
    modifier: Modifier = Modifier,
    text: String,
    highlightSegment: String,
    highlightColor: Color? = null,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    style: TextStyle = LocalTextStyle.current,
    clickPadding: Dp = 12.dp, // Увеличиваем кликабельную область вокруг выделенного текста
    onClick: () -> Unit
) {
    val resolvedHighlightColor = highlightColor ?: AvalonTheme.colors.infoAccent
    val annotatedString = buildAnnotatedString {
        val startIndex = text.indexOf(highlightSegment)
        if (startIndex != -1) {
            append(text.substring(0, startIndex))
            pushStringAnnotation(tag = "clickable", annotation = highlightSegment)
            withStyle(
                style = SpanStyle(
                    color = resolvedHighlightColor,
                    textDecoration = TextDecoration.None
                )
            ) {
                append(highlightSegment)
            }
            pop()
            append(text.substring(startIndex + highlightSegment.length))
        } else {
            append(text)
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotatedString,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    textLayoutResult?.let { layoutResult ->
                        val position = layoutResult.getOffsetForPosition(offset)

                        // Ищем аннотацию по позиции
                        annotatedString.getStringAnnotations(
                            tag = "clickable",
                            start = position,
                            end = position
                        )
                            .firstOrNull()?.let {
                                val bounds = layoutResult.getBoundingBox(position)

                                // Увеличиваем границы кликабельности
                                val expandedBounds = Rect(
                                    bounds.left - clickPadding.toPx(),
                                    bounds.top - clickPadding.toPx(),
                                    bounds.right + clickPadding.toPx(),
                                    bounds.bottom + clickPadding.toPx()
                                )

                                // Проверяем, попадает ли клик в расширенные границы
                                if (expandedBounds.contains(Offset(offset.x, offset.y))) {
                                    onClick()
                                }
                            }
                    }
                }
            },
        style = style,
        color = color,
        textAlign = textAlign,
        onTextLayout = { layoutResult ->
            textLayoutResult = layoutResult
        }
    )
}
