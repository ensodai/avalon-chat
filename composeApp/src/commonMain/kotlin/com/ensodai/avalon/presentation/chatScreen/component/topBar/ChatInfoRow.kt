package com.ensodai.avalon.presentation.chatScreen.component.topBar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.edit
import com.ensodai.avalon.generated.resources.search
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.chatScreen.component.topBar.actionRowButtons.DropdownMenuButton
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.presentation.commonComponents.BaseItemScaleAnimation
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.core.ChatStatus
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import kotlin.uuid.Uuid
import com.ensodai.avalon.utils.EMPTY_STRING
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatInfoRow(
    chatName: String,
    chatImage: String?,
    avatarSeed: String,
    inputSearchMessage: String,
    isEnableChatInfoRow: Boolean,
    chatStatus: ChatStatus,
    isEnableSearchMessageMode: Boolean,
    focusRequester: FocusRequester,
    isEnableEditMessageMode: Boolean,
    onSearchClick: () -> Unit,
    onEnableSearchMessageMode: () -> Unit,
    onChangeInputSearchMessage: (String) -> Unit,
    onOpenChatProfileScreen: () -> Unit,
    onOpenChatProfileScreenFullImage: () -> Unit,
) {
    val colors = AvalonTheme.colors

    AnimatedContent(
        targetState = isEnableEditMessageMode,
        transitionSpec = {
            fadeIn(animationSpec = tween(120)) togetherWith fadeOut(animationSpec = tween(120)) using
                SizeTransform { _, _ ->
                    spring(
                        stiffness = 600f,
                        dampingRatio = 0.75f
                    )
                }
        },
        modifier = Modifier.height(42.dp),
        contentAlignment = Alignment.Center
    ) { editMode ->
        if (editMode) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.edit),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textMain,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            if (isEnableChatInfoRow) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerAsyncImage(
                        Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable {
                                onOpenChatProfileScreenFullImage()
                            },
                        model = chatImage,
                        seed = avatarSeed,
                        contentDescription = "avatar",
                        emojiSize = 22.dp,
                        blurhash = null
                    )

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.9f else 1f,
                        animationSpec = tween(durationMillis = 150)
                    )

                    Column(
                        modifier = Modifier
                            .testTag(BenchmarkTags.ChatProfileButton)
                            .padding(start = 8.dp)
                            .scale(scale)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    onOpenChatProfileScreen()
                                }
                            ),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = chatName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp)
                        )

                        Spacer(modifier = Modifier.size(1.dp))

                        ChatStatusHeader(
                            status = chatStatus,
                            modifier = Modifier.height(16.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(0.dp))
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchMessageTextField(
    modifier: Modifier,
    inputText: String,
    onChangeSearchText: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val scrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val textStyle = TextStyle(
        color = colors.textMain,
        fontSize = 16.sp,
        fontFamily = FontFamily.Default,
        lineHeight = 23.45.sp,
        textAlign = TextAlign.Start,
    )

    BasicTextField(
        value = inputText,
        onValueChange = { newText ->
            if (!newText.contains("\n")) {
                onChangeSearchText(newText)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchClick() }
        ),
        textStyle = textStyle,
        cursorBrush = Brush.linearGradient(listOf(colors.textMain, colors.textMain)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
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

                BaseItemAlphaAnimation(isVisible = inputText.isNotEmpty()) {
                    IconButton(
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
    )
}
