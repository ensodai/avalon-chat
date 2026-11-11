package com.ensodai.avalon.presentation.userCharactersScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.data.db.dto.CharacterWithShortDetails
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.delete
import com.ensodai.avalon.generated.resources.edit
import com.ensodai.avalon.generated.resources.expand_menu
import com.ensodai.avalon.generated.resources.tap_to_chat
import com.ensodai.avalon.navigation.avalonCustomSwipe
import com.ensodai.avalon.presentation.commonComponents.ExpressiveMenuItem
import com.ensodai.avalon.presentation.commonComponents.ExpressivePopupMenu
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.userCharactersScreen.action.UserCharacterScreenActions
import com.ensodai.avalon.presentation.userCharactersScreen.component.UserCharacterScreenTopBar
import com.ensodai.avalon.presentation.userCharactersScreen.viewState.Particle
import com.ensodai.avalon.presentation.userCharactersScreen.viewState.UserCharacterScreenViewState
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme
import kotlin.math.cos
import kotlin.math.sin



@Composable
fun UserCharacterScreenContent(
    navigation: Navigation,
    viewState: UserCharacterScreenViewState,
    actions: UserCharacterScreenActions,
    charactersItem: List<CharacterWithShortDetails>,
) {
    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            modifier = Modifier
                .avalonCustomSwipe { dragDeltaX ->
                    dragDeltaX > 0
                },
            topBar = {
                UserCharacterScreenTopBar(
                    modifier = Modifier,
                    onBackButtonClick = { navigation.navigateBack() }
                )
            }
        ) { innerPadding ->

            LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    count = charactersItem.size,
                    key = { index -> charactersItem[index].id }
                ) { index ->
                    val item = charactersItem[index]

                    CharacterCardItem(
                        character = item,
                        viewState = viewState,
                        actions = actions,
                        onChatClick = { navigation.navigateToChatScreen(item.id) },
                        onEditClick = { navigation.navigateToEditCharacterScreen(item.id) },
                        onDeleteClick = { actions.onDeleteCharacter(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterCardItem(
    character: CharacterWithShortDetails,
    viewState: UserCharacterScreenViewState,
    actions: UserCharacterScreenActions,
    onChatClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val haptic = LocalHapticFeedback.current
    val particleAnim = remember { Animatable(0f) }
    val particles = remember { mutableStateListOf<Particle>() }

    val isSuccessForMe = viewState.claimSuccessEvent?.first == character.id
    LaunchedEffect(viewState.claimSuccessEvent) {
        if (isSuccessForMe) {
            particles.clear()
            repeat(45) {
                val angle = (0..360).random() * (kotlin.math.PI / 180f)
                val speed = (3..15).random().toFloat()
                val color = when ((0..2).random()) {
                    0 -> Color(0xFFE5A93B) // Premium Gold
                    1 -> Color(0xFFFFD700) // Bright Gold/Yellow
                    else -> Color(0xFFFFFFFF) // Sparkling White
                }
                particles.add(
                    Particle(
                        x = 0.5f,
                        y = 0.5f,
                        vx = cos(angle).toFloat() * speed * 0.02f,
                        vy = sin(angle).toFloat() * speed * 0.02f,
                        size = (4..10).random().toFloat(),
                        alpha = 1.0f,
                        color = color
                    )
                )
            }
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            particleAnim.snapTo(0f)
            particleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = LinearOutSlowInEasing
                )
            )
            actions.onClearClaimSuccess()
        }
    }

    GemCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onChatClick,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            ShimmerAsyncImage(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, colors.textMain.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                model = character.avatar?.url,
                seed = character.id.toString() + character.name,
                blurhash = character.avatar?.thumbHash
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = character.name,
                        color = colors.textMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    val unclaimed = character.unclaimedEnergy ?: 0.0
                    if (unclaimed > 0.0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE5A93B).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = IconsManager.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFE5A93B),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "+${if (unclaimed % 1.0 == 0.0) unclaimed.toInt().toString() else ((unclaimed * 10).toInt() / 10.0).toString()}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                    color = Color(0xFFE5A93B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(Res.string.tap_to_chat),
                    color = colors.textMain.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Кнопка сбора накопленных энергетических бонусов с локальным Canvas взрыва частиц
            val unclaimed = character.unclaimedEnergy ?: 0.0
            val isAnimating = particleAnim.value > 0f && particleAnim.value < 1f
            val shouldShowButton = unclaimed >= 1.0 || isAnimating

            if (shouldShowButton) {
                val isClaiming = viewState.claimingCharacterId == character.id
                Box(
                    modifier = Modifier.padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val buttonAlpha = if (unclaimed < 1.0) 1f - particleAnim.value else 1f
                    val buttonScale = if (unclaimed < 1.0) 1f - particleAnim.value * 0.3f else 1f

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = buttonAlpha
                                scaleX = buttonScale
                                scaleY = buttonScale
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isClaiming) Color.Transparent else colors.premiumAccent.copy(alpha = 0.15f))
                            .border(1.5.dp, if (isClaiming) colors.textMain.copy(alpha = 0.1f) else colors.premiumAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable(enabled = !isClaiming && unclaimed >= 1.0) {
                                actions.onClaimEnergy(character.id)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isClaiming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = colors.premiumAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = IconsManager.Bolt,
                                    contentDescription = null,
                                    tint = colors.premiumAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (unclaimed < 1.0) "Собрано" else "Собрать",
                                    color = colors.premiumAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Локальный Canvas частиц, центрированный по кнопке «Собрать» с unbounded=true
                    if (isAnimating) {
                        Canvas(
                            modifier = Modifier
                                .size(1.dp)
                                .wrapContentSize(unbounded = true)
                        ) {
                            val progress = particleAnim.value
                            particles.forEach { p ->
                                val currentX = (p.vx * progress) * 200f
                                val currentY = (p.vy * progress) * 200f
                                val currentAlpha = p.alpha * (1f - progress)
                                drawCircle(
                                    color = p.color.copy(alpha = currentAlpha),
                                    radius = p.size,
                                    center = Offset(currentX, currentY)
                                )
                            }
                        }
                    }
                }
            }

            Box {
                ExpressivePopupMenu(
                    modifier = Modifier,
                    triggerContent = { onClick ->
                        IconButton(onClick = onClick) {
                            Icon(
                                imageVector = IconsManager.MoreVert,
                                contentDescription = stringResource(Res.string.expand_menu),
                                tint = colors.textMain
                            )
                        }
                    },
                    menuContent = { onDismiss ->

                        ExpressiveMenuItem(
                            text = stringResource(Res.string.delete),
                            icon = IconsManager.Delete,
                            onClick = {
                                onDeleteClick()
                                onDismiss()
                            }
                        )

                        ExpressiveMenuItem(
                            text = stringResource(Res.string.edit),
                            icon = IconsManager.Edit,
                            onClick = {
                                onEditClick()
                            }
                        )
                    }
                )
            }
        }
    }
}
