package com.ensodai.avalon.presentation.mainScreen.component.chatSection

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.data.db.dto.CategoryItemDto
import com.ensodai.shared.avalon.character.models.CharacterPreviewResponse
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.categories_stage
import com.ensodai.avalon.generated.resources.character_screen_list_start_message
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.commonComponents.CategoryItem
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.presentation.commonComponents.rememberAvatarStyle
import com.ensodai.avalon.presentation.mainScreen.viewState.ScreenMode
import com.ensodai.avalon.presentation.presentationUtils.bottomSystemButtonsPadding
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCharactersChatsScreen(
    categories: List<CategoryItemDto>,
    currentCategory: CategoryItemDto?,
    innerPadding: PaddingValues,
    screenMode: ScreenMode,
    characters: List<CharacterPreviewResponse>,
    currentState: LazyGridState,
    onSelectCategory: (CategoryItemDto) -> Unit,
    onOpenChat: (CharacterPreviewResponse) -> Unit,
    onRefreshCategory: () -> Unit,
    isRefreshing: Boolean,
    isLoadingMore: Boolean
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val topPadding = if (screenMode == ScreenMode.SearchMode) 8.dp else 48.dp

    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }
    val headerVisibilityProgress = if (headerHeightPx > 0f) {
        1f - ((-headerOffsetPx / headerHeightPx).coerceIn(0f, 1f))
    } else {
        1f
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (pullToRefreshState.distanceFraction == 0.0f) {
                    val delta = available.y
                    val newOffset = headerOffsetPx + delta
                    headerOffsetPx = newOffset.coerceIn(-headerHeightPx, 0f)
                }
                return Offset.Zero
            }
        }
    }

    if (currentCategory != null) {
        Box(modifier = Modifier.nestedScroll(nestedScrollConnection)) {
            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                isRefreshing = isRefreshing,
                onRefresh = { onRefreshCategory() },
                state = pullToRefreshState,
                indicator = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        PullToRefreshDefaults.Indicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            isRefreshing = isRefreshing,
                            state = pullToRefreshState,
                            maxDistance = 88.dp
                        )
                    }
                }
            ) {
                if (characters.isEmpty() && !isLoadingMore && !isRefreshing) {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = pullToRefreshState.distanceFraction.dp * 40),
                        columns = GridCells.Fixed(1),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding() + 8.dp,
                            bottom = bottomSystemButtonsPadding()
                        )
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = topPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(Res.string.character_screen_list_start_message))
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(BenchmarkTags.MainCharactersGrid),
                        state = currentState,
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding() + topPadding + pullToRefreshState.distanceFraction.dp * 40,
                            start = 8.dp,
                            end = 8.dp,
                            bottom = bottomSystemButtonsPadding()
                        )
                    ) {
                        items(
                            count = characters.size,
                            key = { index -> characters[index].id }
                        ) { index ->
                            val characterItem = characters[index]
                            CharacterItem(
                                modifier = Modifier.testTag(BenchmarkTags.characterGridItem(index)),
                                character = characterItem,
                                onClick = { onOpenChat(it) }
                            )
                        }
                    }
                }
            }
            if (screenMode != ScreenMode.SearchMode) {
                LazyRow(
                    modifier = Modifier
                        .alpha(headerVisibilityProgress)
                        .padding(top = innerPadding.calculateTopPadding())
                        .offset { IntOffset(x = 0, y = headerOffsetPx.roundToInt()) }
                        .onGloballyPositioned { coordinates ->
                            headerHeightPx = coordinates.size.height.toFloat()
                        }
                ) {
                    items(items = categories, key = { it.categoryCode }) { categoryItem ->
                        CategoryItem(
                            isSelected = categoryItem == currentCategory,
                            item = categoryItem,
                            onClick = { onSelectCategory(categoryItem) },
                            backgroundColor = AvalonTheme.colors.background
                        )
                    }
                }
            }
        }

    } else {
        Box(modifier = Modifier, contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.categories_stage))
        }
    }
}

@Composable
fun CharacterItem(
    modifier: Modifier = Modifier,
    character: CharacterPreviewResponse,
    onClick: (CharacterPreviewResponse) -> Unit
) {
    val colors = AvalonTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "cardScale")
    val avatarStyle = rememberAvatarStyle(character.id.toString() + character.characterName)

    Card(
        modifier = modifier
            .scale(scale)
            .height(260.dp)
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick(character)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = avatarStyle.backgroundBrush)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            ShimmerAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = character.avatarUrl,
                seed = character.id.toString(),
                emojiSize = 72.dp,
                blurhash = character.thumbHash
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.95f)
                            ),
                            startY = 0f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = character.characterName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (character.shortDescription.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = character.shortDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = IconsManager.Message,
                            contentDescription = "Message count",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = character.messageCountRating.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "@${character.ownerNick}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onlineStatus,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
