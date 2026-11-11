package com.ensodai.avalon.presentation.chatScreen.component.bottomBar.gift

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.core.pagination.PaginatorState
import com.ensodai.shared.avalon.gifts.models.Gift
import com.ensodai.shared.avalon.gifts.models.GiftActionType
import com.ensodai.shared.avalon.gifts.models.GiftPack
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(FlowPreview::class)
@Composable
fun GiftPickerLayout(
    myGiftsState: PaginatorState<GiftPack>,
    onLoadMore: () -> Unit,
    onGiftClick: (Gift) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { it.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .debounce(100L)
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= myGiftsState.items.size - 5) {
                    onLoadMore()
                }
            }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (myGiftsState.isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AvalonTheme.colors.neonAccent)
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(
                        items = myGiftsState.items,
                        key = { pack -> pack.id }
                    ) { pack ->
                        GiftPackSection(
                            pack = pack,
                            onGiftClick = onGiftClick
                        )
                    }

                    if (myGiftsState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = AvalonTheme.colors.neonAccent,
                                strokeWidth = 2.dp
                            )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GiftPackSection(
    pack: GiftPack,
    onGiftClick: (Gift) -> Unit
) {
    val colors = AvalonTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Заголовок пака - делаем чуть компактнее, но ярче
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pack.name.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = AvalonTheme.colors.textMain, // Использовал акцентный цвет для названия
            )

            // Если есть описание пака, можно добавить количество или инфо
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${pack.gifts.size} items",
                style = MaterialTheme.typography.labelSmall,
                color = AvalonTheme.colors.textSecondary.copy(alpha = 0.7f)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Уменьшили отступ между стикерами
        ) {
            items(
                items = pack.gifts,
                key = { it.id }
            ) { gift ->
                GiftItem(
                    gift = gift,
                    onClick = { onGiftClick(gift) }
                )
            }
        }
    }
}

@Composable
fun GiftItem(
    gift: Gift,
    onClick: () -> Unit
) {
    // Контейнер элемента
    Box(
        modifier = Modifier
            .size(88.dp) // Чуть компактнее, чтобы влезало больше элементов при скролле
            .clip(RoundedCornerShape(16.dp)) // Клип нужен только для эффекта нажатия (ripple)
            .clickable(onClick = onClick)
    ) {
        // 1. Сама картинка стикера (занимает весь Box, прозрачный фон)
        ShimmerAsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp), // Небольшой внутренний отступ, чтобы стикеры не слипались
            model = gift.fileUrl,
            seed = gift.fileUrl,
            blurhash = null
        )

        // Заглушка
        if (gift.fileUrl.isEmpty()) {
            Icon(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center),
                imageVector = IconsManager.Gift,
                contentDescription = null,
                tint = AvalonTheme.colors.strokeSubtle
            )
        }

        // 2. Индикатор ЭКШЕНА (Правый верхний угол)
        // Перенес наверх, чтобы он не сливался с возможными деталями внизу стикера
        if (gift.actionType != GiftActionType.NONE) {
            ActionBadge(
                actionType = gift.actionType,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )
        }

        // 3. Индикатор ЦЕНЫ (Левый нижний угол или по центру внизу)
        // Размещаем внизу, так как это логично: сначала видим стикер -> потом экшен -> потом цену
        if (gift.price > 0) {
            PriceBadge(
                price = gift.price,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun ActionBadge(
    actionType: GiftActionType,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val (icon, tintColor) = when (actionType) {
        GiftActionType.CAMERA_SELFIE -> IconsManager.Selfie to AvalonTheme.colors.accentSecondary
        GiftActionType.CAMERA_PHOTO -> IconsManager.Image to AvalonTheme.colors.neonAccent
        GiftActionType.TEXT_EVENT -> IconsManager.GeminiIcon to AvalonTheme.colors.accentSecondary
        else -> IconsManager.Gift to AvalonTheme.colors.textMain
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(AvalonTheme.colors.overlaySurface.copy(alpha = 0.85f))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Action type",
            tint = tintColor,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun PriceBadge(
    price: Int,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AvalonTheme.colors.overlaySurface.copy(alpha = 0.85f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = IconsManager.Bolt, // Молния
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = AvalonTheme.colors.accentSecondary
        )
        Text(
            text = price.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = AvalonTheme.colors.textMain
        )
    }
}
