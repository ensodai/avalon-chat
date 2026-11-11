package com.ensodai.avalon.presentation.mainScreen.component.topBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.presentation.commonComponents.GemPill
import com.ensodai.avalon.presentation.mainScreen.component.chatSection.ChatsScreens
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun ScreenTabRow(
    modifier: Modifier = Modifier,
    isEnable: Boolean,
    pagerState: PagerState,
    items: List<ChatsScreens>
) {

    val coroutineScope = rememberCoroutineScope()
    val colors = AvalonTheme.colors
    val pageProgress by remember(pagerState, items.size) {
        derivedStateOf {
            val maxPage = (items.size - 1).coerceAtLeast(0)
            (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(
                0f,
                maxPage.toFloat()
            )
        }
    }
    BaseItemAlphaAnimation(
        modifier = modifier,
        isVisible = isEnable
    ) {
        GemPill(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp),
            fillContentSize = true
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val tabCount = items.size.coerceAtLeast(1)
                val tabWidth = maxWidth / tabCount

                Box(
                    modifier = Modifier
                        .offset(x = tabWidth * pageProgress)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .background(
                            color = colors.neonAccent.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(999.dp)
                        )
                )

                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items.forEachIndexed { index, tab ->
                        val selectionProgress = 1f - abs(pageProgress - index).coerceIn(0f, 1f)
                        val textColor = lerp(
                            colors.textMain.copy(alpha = 0.72f),
                            colors.userBubbleTextColor,
                            selectionProgress
                        )
                        Box(
                            modifier = Modifier
                                .width(tabWidth)
                                .fillMaxHeight()
                                .testTag(
                                    when (tab) {
                                        ChatsScreens.AllCharactersChatsScreen -> BenchmarkTags.MainAllChatsTab
                                        ChatsScreens.UserChatsScreen -> BenchmarkTags.MainUserChatsTab
                                        ChatsScreens.GlobalLorebooksScreen -> "main_global_lorebooks_tab"
                                    }
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getTabEmoji(tab),
                                fontSize = 18.sp,
                                fontWeight = if (selectionProgress > 0.5f) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Medium
                                },
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getTabEmoji(tab: ChatsScreens): String {
    return when (tab) {
        ChatsScreens.AllCharactersChatsScreen -> "\uD83E\uDD16"
        ChatsScreens.UserChatsScreen -> "\uD83D\uDCAC"
        ChatsScreens.GlobalLorebooksScreen -> "\uD83D\uDCDA"
    }
}
