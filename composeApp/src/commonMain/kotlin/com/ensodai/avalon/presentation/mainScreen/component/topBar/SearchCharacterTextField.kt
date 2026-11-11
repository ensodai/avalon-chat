package com.ensodai.avalon.presentation.mainScreen.component.topBar

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.search
import com.ensodai.avalon.presentation.mainScreen.viewState.SearchLoadingState
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.utils.EMPTY_STRING
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchCharacterTextField(
    modifier: Modifier,
    inputText: String,
    searchLoadingState: SearchLoadingState,
    onChangeSearchText: (String) -> Unit
) {
    val colors = AvalonTheme.colors

    val scrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val textStyle = TextStyle(
        color = colors.textMain,
        fontSize = 16.sp,
        fontFamily = FontFamily.Default,
        lineHeight = 23.45.sp,
        textAlign = TextAlign.Start,
    )
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }
    BasicTextField(
        value = inputText,
        onValueChange = { newText ->
            if (!newText.contains("\n") && newText.length <= 20) {
                onChangeSearchText(newText)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .focusRequester(focusRequester),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        textStyle = textStyle,
        cursorBrush = Brush.linearGradient(listOf(colors.textMain, colors.textMain)),
        decorationBox = { innerTextField ->
            Row(
//                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .bringIntoViewRequester(bringIntoViewRequester),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.search),
                            color = colors.placeholderText,
                            style = textStyle,
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }


                Crossfade(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    targetState = searchLoadingState,
                    label = "SearchStateCrossfade"
                ) { state ->
                    when (state) {
                        SearchLoadingState.Loading -> {
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = colors.strokeSubtle,
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        else -> {
                            if (!inputText.isEmpty()) {
                                IconButton(
                                    modifier = Modifier.size(48.dp),
                                    onClick = { onChangeSearchText(EMPTY_STRING) }
                                ) {
                                    Icon(
                                        imageVector = IconsManager.Close,
                                        contentDescription = "Clear input text"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
