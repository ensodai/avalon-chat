package com.ensodai.avalon.presentation.characterProfileScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.generated.resources.*
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.presentation.paymentScreen.viewState.primaryTint
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterModelSection(
    modifier: Modifier = Modifier,
    selectedConfig: AvailableLlmConfigs?,
    availableLlmConfigs: List<AvailableLlmConfigs>,
    userTariff: UserTariff = UserTariff.Free,
    onModelSelected: (AvailableLlmConfigs) -> Unit,
    onNavigateToPayment: () -> Unit = {}
) {
    val colors = AvalonTheme.colors
    var showBottomSheet by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    GemCard(
        modifier = modifier.height(110.dp),
        onClick = { showBottomSheet = true },
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = IconsManager.Memory,
                    contentDescription = null,
                    tint = colors.neonAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.model),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            // Название модели по центру
            val modelName = selectedConfig?.displayName ?: "..."
            Text(
                text = modelName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = colors.textMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Стоимость в энергии снизу
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${selectedConfig?.price ?: 0}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.successAccent
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = IconsManager.Bolt,
                    contentDescription = null,
                    tint = colors.successAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.cardBackground,
            dragHandle = {
                BottomSheetDefaults.DragHandle(color = colors.textSecondary.copy(alpha = 0.3f))
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Выбор модели ИИ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = colors.textMain,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                availableLlmConfigs.forEach { config ->
                    val isSelected = config.codeName == selectedConfig?.codeName
                    val lowerName = config.displayName.lowercase()
                    val requiredTariff = when {
                        lowerName.contains("3.5") -> UserTariff.Crystal
                        lowerName.contains("3.1") -> UserTariff.Premium
                        else -> UserTariff.Free
                    }
                    val isLocked = userTariff.ordinal < requiredTariff.ordinal && userTariff != UserTariff.Special

                    val modelDescription = when {
                        config.codeName.contains("flash", ignoreCase = true) -> {
                            "Быстрая и экономичная модель. Идеально подходит для простых диалогов и быстрых ответов."
                        }
                        config.codeName.contains("pro", ignoreCase = true) -> {
                            "Умная и креативная модель. Глубокое понимание контекста и сложные ролевые сценарии."
                        }
                        else -> {
                            "Универсальная модель для общения и решения повседневных задач."
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) colors.overlaySurface.copy(alpha = 0.2f)
                                else if (isLocked) colors.background.copy(alpha = 0.1f)
                                else colors.background.copy(alpha = 0.4f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) colors.neonAccent else colors.strokeSubtle.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (isLocked) {
                                    onNavigateToPayment()
                                } else {
                                    onModelSelected(config)
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                }
                                showBottomSheet = false
                            }
                            .padding(16.dp)
                            .alpha(if (isLocked) 0.6f else 1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = config.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) colors.neonAccent else colors.textMain
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = modelDescription,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                                lineHeight = 14.sp
                            )
                            if (isLocked) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Доступно на тарифе ${requiredTariff.title}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = requiredTariff.primaryTint()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        if (isLocked) {
                            Icon(
                                imageVector = IconsManager.Lock,
                                contentDescription = "Locked",
                                tint = requiredTariff.primaryTint(),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "${config.price}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.successAccent
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = IconsManager.Bolt,
                                    contentDescription = null,
                                    tint = colors.successAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
