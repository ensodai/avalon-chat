package com.ensodai.avalon.presentation.chatVariantScreen.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.data.db.dto.ChatVariant
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.messages_count
import com.ensodai.avalon.generated.resources.untitled_story
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.presentation.commonComponents.shimmer.technoShimmer
import com.ensodai.avalon.presentation.core.LoadingState
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

//@Composable
//fun StoryCardItem1(
//    modifier: Modifier = Modifier,
//    story: ChatVariant,
//    onClickStory: (ChatVariant) -> Unit,
//    onLongClickStory: (ChatVariant) -> Unit,
//    loadingState: LoadingState
//) {
//    val borderColor = if (story.isActiveVariant) {
//        MaterialTheme.colorScheme.primary
//    } else {
//        Color.Transparent
//    }
//    Box(
//        modifier = modifier.let{
//            if (loadingState == LoadingState.Loading) {
//                it.shimmer()
//            } else {
//                it
//            }
//        }
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(0.75f),
//            shape = RoundedCornerShape(16.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//            border = BorderStroke(2.dp, borderColor)
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .combinedClickable(
//                        onClick = {
//                            if (loadingState != LoadingState.Loading) {
//                                onClickStory(story)
//                            }
//                        },
//                        onLongClick = {
//                            if (loadingState != LoadingState.Loading) {
//                                onLongClickStory(story)
//                            }
//                        }
//                    )
//            ) {
//                val image = story.image?.url ?: story.defaultImage?.url
//                ShimmerAsyncImage(
//                    model = image,
//                    contentDescription = story.name,
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.fillMaxSize()
//                )
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(
//                            Brush.verticalGradient(
//                                colors = listOf(Color.Transparent, Color.Black),
//                                startY = 400f
//                            )
//                        )
//                )
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(
//                            Brush.verticalGradient(
//                                colors = listOf(Color.Transparent, Color.Black),
//                                startY = 400f
//                            )
//                        )
//                )
//
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .align(Alignment.BottomStart)
//                        .padding(16.dp)
//                ) {
//                    Text(
//                        text = story.name ?: "История без названия",
//                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
//                        color = Color.White,
//                        maxLines = 2,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
////                    story.messageCount?.let { count ->
////                        Text(
////                            text = "$count сообщений",
////                            style = MaterialTheme.typography.bodySmall,
////                            color = Color.White.copy(alpha = 0.8f),
////                            maxLines = 1
////                        )
////                    }
//                }
//            }
//        }
//    }
//}

//@Composable
//fun StoryCardItem2(
//    modifier: Modifier = Modifier,
//    story: ChatVariant,
//    onClickStory: (ChatVariant) -> Unit,
//    onLongClickStory: (ChatVariant) -> Unit,
//    loadingState: LoadingState
//) {
//    // --- Получаем цвета из темы ЗАРАНЕЕ ---
//    // Это необходимо, так как MaterialTheme.colorScheme является @Composable,
//    // а мы будем использовать эти цвета в non-composable блоке drawWithContent.
//    val activeGlowColor = MaterialTheme.colorScheme.primary
//    val glassBackgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
//
//    // Анимация для тени при активации
//    val elevation by animateDpAsState(
//        targetValue = if (story.isActiveVariant) 10.dp else 4.dp,
//        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
//        label = "elevationAnimation"
//    )
//    val cornerRadius = 20.dp
//
//    Card(
//        modifier = modifier
//            .fillMaxWidth()
//            .aspectRatio(0.7f),
//        shape = RoundedCornerShape(cornerRadius),
//        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .clip(RoundedCornerShape(cornerRadius))
//                .combinedClickable(
//                    onClick = {
//                        if (loadingState != LoadingState.Loading) onClickStory(story)
//                    },
//                    onLongClick = {
//                        if (loadingState != LoadingState.Loading) onLongClickStory(story)
//                    }
//                )
//        ) {
//            // --- Фоновое изображение и градиенты ---
//            val image = story.image?.url ?: story.defaultImage?.url
//            AsyncImage(
//                model = image,
//                contentDescription = story.name,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .drawWithContent {
//                        // Сначала рисуем само изображение
//                        drawContent()
//
//                        // Затем рисуем поверх него градиент для затемнения текста
//                        drawRect(
//                            brush = Brush.verticalGradient(
//                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
//                                startY = size.height * 0.4f, // size доступна здесь
//                                endY = size.height
//                            )
//                        )
//
//                        // И если карточка активна, рисуем свечение
//                        if (story.isActiveVariant) {
//                            drawRect(
//                                brush = Brush.verticalGradient(
//                                    colors = listOf(
//                                        activeGlowColor.copy(alpha = 0.5f),
//                                        Color.Transparent
//                                    ),
//                                    startY = 0f,
//                                    endY = size.height * 0.8f
//                                ),
//                                blendMode = BlendMode.Plus // Режим наложения для эффекта "свечения"
//                            )
//                        }
//                    }
//            )
//
//            // --- Блок с информацией (имя, сообщения) ---
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(12.dp))
//                        // --- ИСПРАВЛЕННЫЙ ВЫЗОВ CLOUDY ---
//
//                        .background(glassBackgroundColor) // Фон для эффекта матового стекла
//                        .padding(horizontal = 12.dp, vertical = 8.dp)
//                ) {
//                    Column {
//                        Text(
//                            text = story.name ?: "История без названия",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White,
//                            maxLines = 2,
//                            overflow = TextOverflow.Ellipsis
//                        )
////                        story.messageCount?.let { count ->
////                            Spacer(modifier = Modifier.height(4.dp))
////                            Text(
////                                text = "$count сообщений",
////                                style = MaterialTheme.typography.bodyMedium,
////                                color = Color.White.copy(alpha = 0.9f),
////                                maxLines = 1
////                            )
////                        }
//                    }
//                }
//            }
//
//            // --- Шиммер загрузки ---
//            if (loadingState == LoadingState.Loading) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
//                )
//            }
//        }
//    }
//}

@Composable
fun StoryCardItem(
    modifier: Modifier = Modifier,
    story: ChatVariant,
    isActiveVariant: Boolean,
    onClickStory: (ChatVariant) -> Unit,
    onLongClickStory: (ChatVariant) -> Unit,
    loadingState: LoadingState
) {
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isActiveVariant) AvalonTheme.colors.strokeSubtle else Color.Transparent,
        animationSpec = tween(durationMillis = 10),
        label = "BorderColorAnimation"
    )
    Box(
        modifier = modifier.let {
            if (loadingState == LoadingState.Loading) {
                it.technoShimmer(
                    visible = true,
                    shape = RoundedCornerShape(16.dp)
                )
            } else {
                it
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(2.dp, animatedBorderColor),
            colors = CardDefaults.cardColors(containerColor = AvalonTheme.colors.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = {
                            if (loadingState != LoadingState.Loading) {
                                onClickStory(story)
                            }
                        },
                        onLongClick = {
                            if (loadingState != LoadingState.Loading) {
                                onLongClickStory(story)
                            }
                        }
                    )
            ) {
                val image = story.image?.url ?: story.defaultImage?.url
                val thumbHash = story.image?.thumbHash ?: story.defaultImage?.thumbHash
                ShimmerAsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = image,
                    seed = story.id.toString(),
                    contentDescription = story.name,
                    emojiSize = 78.dp,
                    blurhash = thumbHash
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.4f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.9f)
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = story.name ?: stringResource(Res.string.untitled_story),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AvalonTheme.colors.textMain,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
//                    story.messageCount?.let { count ->
//                        Text(
//                            text = getMessagesCountString(count),
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
//                            maxLines = 1
//                        )
//                    }
                }
            }
        }
    }
}

@Composable
private fun getMessagesCountString(count: Int): String {
    return pluralStringResource(Res.plurals.messages_count, count, count)
}
