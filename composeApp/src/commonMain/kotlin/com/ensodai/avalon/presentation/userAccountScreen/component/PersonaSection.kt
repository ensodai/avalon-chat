package com.ensodai.avalon.presentation.userAccountScreen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.domain.models.PersonaInfo
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.create_new_person
import com.ensodai.avalon.generated.resources.edit
import com.ensodai.avalon.generated.resources.person
import com.ensodai.avalon.generated.resources.person_off
import com.ensodai.avalon.generated.resources.persona_free_limit_reached
import com.ensodai.avalon.generated.resources.persona_limit_description
import com.ensodai.avalon.generated.resources.persona_remove_limit_button
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.presentation.paymentScreen.viewState.primaryTint
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun PersonaSection(
    modifier: Modifier = Modifier,
    loadingState: LoadingState,
    onPersonaClick: (PersonaInfo) -> Unit,
    onCreateNewPersonaClick: () -> Unit,
    onEditPersonaClick: (PersonaInfo) -> Unit,
    onPremiumClick: () -> Unit,
    selectedPersona: PersonaInfo?,
    personaInfoList: List<PersonaInfo>,
    premiumStatus: UserTariff,
) {
    val colors = AvalonTheme.colors
    var personasExpanded by remember { mutableStateOf(false) }

    // Ограничение персон отключено до лучших времен — доступно для всех юзеров
    // val maxFreePersonas = 5
    // val isFreeUser = premiumStatus == UserTariff.Free
    // val isLimitReached = isFreeUser && personaInfoList.size >= maxFreePersonas
    val isLimitReached = false

    val rotationState by animateFloatAsState(
        targetValue = if (personasExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    GemCard(
        modifier = modifier,
        onClick = { personasExpanded = !personasExpanded },
        enabled = loadingState == LoadingState.Success,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = IconsManager.SupervisorAccount,
                        contentDescription = null,
                        tint = if (loadingState == LoadingState.Loading) {
                            colors.neonAccent
                        } else if (premiumStatus != UserTariff.Free) {
                            premiumStatus.primaryTint()
                        } else {
                            colors.textSecondary
                        },
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(Res.string.person),
                            color = colors.textMain,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        AnimatedVisibility(
                            visible = !personasExpanded && loadingState == LoadingState.Success,
                            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                        ) {
                            val activeName =
                                selectedPersona?.name ?: stringResource(Res.string.person_off)
                            Text(
                                text = activeName,
                                color = colors.neonAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Icon(
                    modifier = Modifier
                        .rotate(rotationState)
                        .size(24.dp),
                    imageVector = IconsManager.ExpandMore,
                    contentDescription = null,
                    tint = colors.textSecondary
                )
            }

            AnimatedVisibility(
                visible = personasExpanded,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = colors.textMain.copy(alpha = 0.08f)
                    )

                    personaInfoList.forEach { persona ->
                        TechnoPersonaItem(
                            personaInfo = persona,
                            isSelected = persona.id == selectedPersona?.id,
                            onPersonaClick = onPersonaClick,
                            onEditPersonaClick = onEditPersonaClick
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        if (isLimitReached) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = IconsManager.Info,
                                        contentDescription = null,
                                        tint = colors.textSecondary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(Res.string.persona_free_limit_reached),
                                        color = colors.textSecondary.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = stringResource(Res.string.persona_limit_description),
                                    color = colors.textMain.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(start = 20.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                TechnoButton(
                                    text = stringResource(Res.string.persona_remove_limit_button),
                                    onClick = onPremiumClick,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    isPrimary = false,
                                    customBorderColor = colors.premiumAccent.copy(alpha = 0.6f),
                                    onSecondaryColor = colors.premiumAccent
                                )
                            }
                        } else {
                            CreatePersonaSlot(onClick = onCreateNewPersonaClick)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TechnoPersonaItem(
    personaInfo: PersonaInfo,
    isSelected: Boolean,
    onPersonaClick: (PersonaInfo) -> Unit,
    onEditPersonaClick: (PersonaInfo) -> Unit
) {
    val colors = AvalonTheme.colors
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) colors.neonAccent.copy(alpha = 0.08f) else Color.Transparent,
        animationSpec = tween(300),
        label = "BgAnim"
    )

    val barHeightPercent by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "BarAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onPersonaClick(personaInfo) }
            .height(IntrinsicSize.Min)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(barHeightPercent)
                    .width(3.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(colors.neonAccent, colors.accentSecondary)
                        )
                    )
                    .align(Alignment.CenterStart)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = personaInfo.name,
                    color = if (isSelected) colors.textMain else colors.textSecondary,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                if (personaInfo.description.isNotEmpty()) {
                    Text(
                        text = personaInfo.description,
                        color = if (isSelected) colors.textMain.copy(alpha = 0.7f) else colors.textSecondary.copy(
                            alpha = 0.5f
                        ),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }
            }

            IconButton(
                onClick = { onEditPersonaClick(personaInfo) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = IconsManager.Edit,
                    contentDescription = stringResource(Res.string.edit),
                    tint = if (isSelected) colors.neonAccent.copy(alpha = 0.8f) else colors.textSecondary.copy(
                        alpha = 0.3f
                    ),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CreatePersonaSlot(onClick: () -> Unit) {
    val colors = AvalonTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(colors.textMain.copy(alpha = 0.03f)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = IconsManager.Add,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.create_new_person).uppercase(),
                color = colors.textSecondary,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
