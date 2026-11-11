package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun BlueClickableText(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    color: Color? = null
) {
    val resolvedColor = color ?: AvalonTheme.colors.infoAccent
    Text(
        modifier = modifier
            .clickable {
                onClick()
            }
            .padding(start = 1.dp),
        text = text,
        color = resolvedColor,
        style = style,
        textAlign = textAlign,
    )
}
