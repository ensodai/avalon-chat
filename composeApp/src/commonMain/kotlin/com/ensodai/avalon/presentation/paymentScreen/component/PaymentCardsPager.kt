package com.ensodai.avalon.presentation.paymentScreen.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.btn_activate
import com.ensodai.avalon.generated.resources.recomended_caps
import com.ensodai.avalon.generated.resources.early_access_desc
import com.ensodai.avalon.generated.resources.energy_accumulates_desc
import com.ensodai.avalon.generated.resources.energy_burns_desc
import com.ensodai.avalon.generated.resources.memory_crystal_desc
import com.ensodai.avalon.generated.resources.memory_elite_desc
import com.ensodai.avalon.generated.resources.memory_free_desc
import com.ensodai.avalon.generated.resources.memory_premium_desc
import com.ensodai.avalon.generated.resources.price_per_month_pattern
import com.ensodai.avalon.generated.resources.privacy_policy
import com.ensodai.avalon.generated.resources.purchase_agreement_hint
import com.ensodai.avalon.generated.resources.restore_purchase
import com.ensodai.avalon.generated.resources.terms_of_service
import com.ensodai.avalon.generated.resources.vip_glow_desc
import com.ensodai.avalon.generated.resources.voice_streaming_feature
import com.ensodai.avalon.generated.resources.voice_streaming_free_desc
import com.ensodai.avalon.generated.resources.voice_streaming_paid_desc
import com.ensodai.avalon.generated.resources.stat_daily_energy
import com.ensodai.avalon.generated.resources.stat_max_capacity
import com.ensodai.avalon.generated.resources.feature_extended_memory
import com.ensodai.avalon.generated.resources.feature_golden_glow
import com.ensodai.avalon.generated.resources.feature_early_access
import com.ensodai.avalon.generated.resources.persona_creation_feature
import com.ensodai.avalon.generated.resources.persona_creation_free_desc
import com.ensodai.avalon.generated.resources.persona_creation_paid_desc
import com.ensodai.avalon.presentation.paymentScreen.viewState.SubscriptionPlan
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.presentation.paymentScreen.viewState.icon
import com.ensodai.avalon.presentation.paymentScreen.viewState.accentTint
import com.ensodai.avalon.presentation.paymentScreen.viewState.primaryTint
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.roundToInt

@Composable
fun PaymentCardsPager(
    modifier: Modifier,
    products: List<SubscriptionPlan?>,
    selectedPlanIndex: Int,
    isEnablePaymentService: Boolean,
    onPurchase: (plan: SubscriptionPlan) -> Unit,
    onSelectPlan: (index: Int) -> Unit,
    onRestorePurchase: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenPrivacy: () -> Unit = {}
) {
    val colors = AvalonTheme.colors
    val selectedPlan = products.getOrNull(selectedPlanIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Горизонтальный выбор тарифов
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            products.forEachIndexed { index, plan ->
                if (plan != null) {
                    val isSelected = index == selectedPlanIndex
                    Box(modifier = Modifier.width(110.dp)) {
                        PremiumCardItem(
                            plan = plan,
                            isSelected = isSelected,
                            onClick = { onSelectPlan(index) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        )
                        if (plan.tariff == UserTariff.Premium) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(colors.upgradeGradientStart, colors.rarityEpicGlow)
                                        )
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.recomended_caps),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Интерактивная таблица сравнения
        if (selectedPlan != null) {
            val planColor = selectedPlan.tariff.primaryTint()
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Заголовки таблицы сравнения
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, start = 28.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Free",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(0.8f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = selectedPlan.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = planColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(color = colors.textMain.copy(alpha = 0.05f))

                // Фича 1: Ежедневная энергия
                FeatureComparisonRow(
                    title = stringResource(Res.string.stat_daily_energy),
                    icon = IconsManager.Bolt,
                    iconColor = colors.neonAccent,
                    freeValue = "50 ⚡",
                    selectedValue = "+${selectedPlan.dailyEnergy} ⚡",
                    selectedValueColor = planColor
                )

                HorizontalDivider(color = colors.textMain.copy(alpha = 0.05f))

                // Фича 2: Хранилище (Банк)
                FeatureComparisonRow(
                    title = stringResource(Res.string.stat_max_capacity),
                    icon = IconsManager.BatteryChargingFull,
                    iconColor = colors.successAccent,
                    freeValue = stringResource(Res.string.energy_burns_desc),
                    selectedValue = stringResource(Res.string.energy_accumulates_desc, selectedPlan.maxCapacity),
                    selectedValueColor = planColor
                )

                HorizontalDivider(color = colors.textMain.copy(alpha = 0.05f))

                // Фича 3: Озвучка (Голос ИИ)
                FeatureComparisonRow(
                    title = stringResource(Res.string.voice_streaming_feature),
                    icon = IconsManager.Mic,
                    iconColor = colors.infoAccent,
                    freeValue = stringResource(Res.string.voice_streaming_free_desc),
                    selectedValue = stringResource(Res.string.voice_streaming_paid_desc),
                    selectedValueColor = planColor
                )

                HorizontalDivider(color = colors.textMain.copy(alpha = 0.05f))

                // Фича 4: Создание Персон
                FeatureComparisonRow(
                    title = stringResource(Res.string.persona_creation_feature),
                    icon = IconsManager.Person,
                    iconColor = colors.infoAccent,
                    freeValue = stringResource(Res.string.persona_creation_free_desc),
                    selectedValue = stringResource(Res.string.persona_creation_paid_desc),
                    selectedValueColor = planColor
                )

                HorizontalDivider(color = colors.textMain.copy(alpha = 0.05f))

                // Фича 4: Память
                val memoryDesc = when (selectedPlan.tariff) {
                    UserTariff.Premium -> stringResource(Res.string.memory_premium_desc)
                    UserTariff.Crystal -> stringResource(Res.string.memory_crystal_desc)
                    UserTariff.Elite -> stringResource(Res.string.memory_elite_desc)
                    else -> stringResource(Res.string.memory_free_desc)
                }
                FeatureComparisonRow(
                    title = stringResource(Res.string.feature_extended_memory).substringBefore(" ("),
                    icon = IconsManager.Psychology,
                    iconColor = colors.rarityEpicGlow,
                    freeValue = stringResource(Res.string.memory_free_desc),
                    selectedValue = memoryDesc,
                    selectedValueColor = planColor
                )

                if (selectedPlan.tariff == UserTariff.Elite) {
                    HorizontalDivider(color = colors.textMain.copy(alpha = 0.05f))

                    // Фича 5: VIP Аура
                    FeatureComparisonRow(
                        title = stringResource(Res.string.feature_golden_glow),
                        icon = IconsManager.Star,
                        iconColor = colors.premiumAccent,
                        freeValue = "—",
                        selectedValue = stringResource(Res.string.vip_glow_desc),
                        selectedValueColor = planColor
                    )

                    HorizontalDivider(color = colors.textMain.copy(alpha = 0.05f))

                    // Фича 6: Ранний доступ
                    FeatureComparisonRow(
                        title = stringResource(Res.string.feature_early_access),
                        icon = IconsManager.SelfImprovement,
                        iconColor = colors.rarityRareGlow,
                        freeValue = "—",
                        selectedValue = stringResource(Res.string.early_access_desc),
                        selectedValueColor = planColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Динамическая кнопка активации подписки
            val buttonColor by animateColorAsState(targetValue = planColor, label = "button_color")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { onPurchase(selectedPlan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = "${stringResource(Res.string.btn_activate)} ${selectedPlan.title.uppercase()}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.background,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                val dailyPriceLabel = remember(selectedPlan.price) { calculateDailyPrice(selectedPlan.price) }
                if (dailyPriceLabel.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(buttonColor.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = dailyPriceLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = planColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FooterLegalLinks(
            onRestorePurchase = onRestorePurchase,
            onOpenTerms = onOpenTerms,
            onOpenPrivacy = onOpenPrivacy
        )
    }
}

@Composable
fun PremiumCardItem(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val primaryColor = plan.tariff.primaryTint()
    val secondaryColor = plan.tariff.accentTint()
    val borderBrush = if (isSelected) {
        Brush.verticalGradient(listOf(primaryColor, secondaryColor))
    } else {
        SolidColor(colors.strokeActive.copy(alpha = 0.5f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) colors.overlaySurface else colors.background)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PremiumEnergyIcon(
                tier = plan.tariff,
                primaryColor = primaryColor,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = plan.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) primaryColor else colors.textMain
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = plan.price.replace(".00", ""),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                ),
                textAlign = TextAlign.Center
            )
            val dailyPrice = remember(plan.price) { calculateDailyPrice(plan.price) }
            if (dailyPrice.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                val dailyClean = dailyPrice.replace("всего ", "").replace("only ", "")
                Text(
                    text = dailyClean,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun FeatureComparisonRow(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    freeValue: String,
    selectedValue: String,
    selectedValueColor: Color
) {
    val colors = AvalonTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textMain,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(1.3f)
        )
        Text(
            text = freeValue,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.weight(0.8f)
        )
        Text(
            text = selectedValue,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = selectedValueColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

fun calculateDailyPrice(priceLabel: String): String {
    val regex = """\d+([.,]\d+)?""".toRegex()
    val match = regex.find(priceLabel)
    if (match != null) {
        val numberString = match.value.replace(",", ".")
        val monthlyPrice = numberString.toDoubleOrNull() ?: return ""
        val dailyPrice = monthlyPrice / 30.0
        
        val formattedDaily = if (dailyPrice < 1.0) {
            val rounded = (dailyPrice * 100).toInt() / 100.0
            rounded.toString()
        } else {
            dailyPrice.roundToInt().toString()
        }
        
        return when {
            priceLabel.contains("₽") || priceLabel.contains("RUB") -> "всего $formattedDaily ₽/день"
            priceLabel.contains("$") || priceLabel.contains("USD") -> "only $$formattedDaily/day"
            priceLabel.contains("€") || priceLabel.contains("EUR") -> "only €$formattedDaily/day"
            else -> "$formattedDaily / day"
        }
    }
    return ""
}

@Composable
fun FooterLegalLinks(
    onRestorePurchase: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit
) {
    val colors = AvalonTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = onRestorePurchase,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                text = stringResource(Res.string.restore_purchase),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val termsOfService = stringResource(Res.string.terms_of_service)
        val privacyPolicy = stringResource(Res.string.privacy_policy)

        val linkStyle = SpanStyle(
            color = colors.infoAccent,
            textDecoration = TextDecoration.Underline
        )

        val annotatedString = buildAnnotatedString {
            val fullText =
                stringResource(Res.string.purchase_agreement_hint, termsOfService, privacyPolicy)
            val termsIndex = fullText.indexOf(termsOfService)
            val privacyIndex = fullText.indexOf(privacyPolicy)

            if (termsIndex != -1 && privacyIndex != -1) {
                val firstIndex = minOf(termsIndex, privacyIndex)
                append(fullText.substring(0, firstIndex))

                if (termsIndex < privacyIndex) {
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "TERMS",
                            styles = TextLinkStyles(style = linkStyle),
                            linkInteractionListener = { onOpenTerms() }
                        )
                    ) {
                        append(termsOfService)
                    }
                    append(fullText.substring(termsIndex + termsOfService.length, privacyIndex))
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "PRIVACY",
                            styles = TextLinkStyles(style = linkStyle),
                            linkInteractionListener = { onOpenPrivacy() }
                        )
                    ) {
                        append(privacyPolicy)
                    }
                } else {
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "PRIVACY",
                            styles = TextLinkStyles(style = linkStyle),
                            linkInteractionListener = { onOpenPrivacy() }
                        )
                    ) {
                        append(privacyPolicy)
                    }
                    append(fullText.substring(privacyIndex + privacyPolicy.length, termsIndex))
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "TERMS",
                            styles = TextLinkStyles(style = linkStyle),
                            linkInteractionListener = { onOpenTerms() }
                        )
                    ) {
                        append(termsOfService)
                    }
                }

                val lastIndex =
                    maxOf(termsIndex + termsOfService.length, privacyIndex + privacyPolicy.length)
                append(fullText.substring(lastIndex))
            } else {
                append(fullText)
            }
        }

        Text(
            text = annotatedString,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        )
    }
}

@Composable
fun PremiumEnergyIcon(
    tier: UserTariff,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "energy_anim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                delayMillis = 2500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(pulseScale)
        ) {
            val glowBrush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.5f),
                    primaryColor.copy(alpha = 0.2f),
                    Color.Transparent
                )
            )

            drawCircle(
                brush = glowBrush,
                radius = size.minDimension / 2,
                alpha = 1f
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(colors.overlaySurface)
                .border(2.dp, primaryColor.copy(alpha = 0.8f), CircleShape)
                .drawWithContent {
                    drawContent()

                    if (tier == UserTariff.Crystal || tier == UserTariff.Elite) {
                        val width = size.width
                        val height = size.height
                        val xOffset = (width * 4 * shimmerProgress) - (width * 1.5f)
                        val shimmerBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                colors.textMain.copy(alpha = 0.1f),
                                colors.textMain.copy(alpha = 0.3f),
                                colors.textMain.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            start = Offset(xOffset, 0f),
                            end = Offset(xOffset + width, height)
                        )

                        drawRect(brush = shimmerBrush)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tier.icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.fillMaxSize(0.5f)
            )
        }
    }
}
