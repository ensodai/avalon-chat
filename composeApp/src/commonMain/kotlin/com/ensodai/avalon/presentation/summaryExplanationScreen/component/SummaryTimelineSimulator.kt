package com.ensodai.avalon.presentation.summaryExplanationScreen.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.summary_btn_auto_off
import com.ensodai.avalon.generated.resources.summary_btn_auto_on
import com.ensodai.avalon.generated.resources.summary_btn_next
import com.ensodai.avalon.generated.resources.summary_btn_prev
import com.ensodai.avalon.generated.resources.summary_msg_count_suffix
import com.ensodai.avalon.generated.resources.summary_step_1_desc
import com.ensodai.avalon.generated.resources.summary_step_1_gauge
import com.ensodai.avalon.generated.resources.summary_step_1_msg_1
import com.ensodai.avalon.generated.resources.summary_step_1_msg_1_sub
import com.ensodai.avalon.generated.resources.summary_step_1_msg_2
import com.ensodai.avalon.generated.resources.summary_step_1_msg_2_sub
import com.ensodai.avalon.generated.resources.summary_step_1_msg_3
import com.ensodai.avalon.generated.resources.summary_step_1_msg_3_sub
import com.ensodai.avalon.generated.resources.summary_step_1_msg_4
import com.ensodai.avalon.generated.resources.summary_step_1_msg_4_sub
import com.ensodai.avalon.generated.resources.summary_step_1_tab
import com.ensodai.avalon.generated.resources.summary_step_1_title
import com.ensodai.avalon.generated.resources.summary_step_2_desc
import com.ensodai.avalon.generated.resources.summary_step_2_gauge
import com.ensodai.avalon.generated.resources.summary_step_2_msg_highlight
import com.ensodai.avalon.generated.resources.summary_step_2_msg_highlight_sub
import com.ensodai.avalon.generated.resources.summary_step_2_msg_keep_sub
import com.ensodai.avalon.generated.resources.summary_step_2_msg_new
import com.ensodai.avalon.generated.resources.summary_step_2_msg_new_sub
import com.ensodai.avalon.generated.resources.summary_step_2_tab
import com.ensodai.avalon.generated.resources.summary_step_2_title
import com.ensodai.avalon.generated.resources.summary_step_3_crystal_desc
import com.ensodai.avalon.generated.resources.summary_step_3_crystal_title
import com.ensodai.avalon.generated.resources.summary_step_3_desc
import com.ensodai.avalon.generated.resources.summary_step_3_gauge
import com.ensodai.avalon.generated.resources.summary_step_3_msg_active_sub
import com.ensodai.avalon.generated.resources.summary_step_3_tab
import com.ensodai.avalon.generated.resources.summary_step_3_title
import com.ensodai.avalon.generated.resources.summary_step_4_crystal_desc
import com.ensodai.avalon.generated.resources.summary_step_4_crystal_title
import com.ensodai.avalon.generated.resources.summary_step_4_desc
import com.ensodai.avalon.generated.resources.summary_step_4_gauge
import com.ensodai.avalon.generated.resources.summary_step_4_msg_new
import com.ensodai.avalon.generated.resources.summary_step_4_tab
import com.ensodai.avalon.generated.resources.summary_step_4_title
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun SummaryTimelineSimulator(
    currentStep: Int,
    isAutoPlaying: Boolean,
    onStepSelected: (Int) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    LaunchedEffect(isAutoPlaying, currentStep) {
        if (isAutoPlaying) {
            delay(4000)
            onStepSelected((currentStep + 1) % 4)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = colors.neonAccent.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            )
            .background(colors.overlaySurface.copy(alpha = 0.06f))
            .padding(16.dp)
    ) {
        // Step Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val stepTitles = listOf(
                stringResource(Res.string.summary_step_1_tab),
                stringResource(Res.string.summary_step_2_tab),
                stringResource(Res.string.summary_step_3_tab),
                stringResource(Res.string.summary_step_4_tab)
            )
            stepTitles.forEachIndexed { index, title ->
                val isSelected = currentStep == index
                val chipBg by animateColorAsState(
                    targetValue = if (isSelected) colors.neonAccent.copy(alpha = 0.2f) else colors.overlaySurface.copy(alpha = 0.04f),
                    animationSpec = tween(300)
                )
                val chipBorder by animateColorAsState(
                    targetValue = if (isSelected) colors.neonAccent else colors.strokeSubtle.copy(alpha = 0.5f),
                    animationSpec = tween(300)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, chipBorder, RoundedCornerShape(10.dp))
                        .background(chipBg)
                        .clickable { onStepSelected(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        ),
                        color = if (isSelected) colors.neonAccent else colors.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual animation container
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(animationSpec = tween(250))
            },
            label = "SummaryStepAnimation"
        ) { step ->
            when (step) {
                0 -> StepAccumulationView()
                1 -> StepThresholdTriggerView()
                2 -> StepCompressionView()
                else -> StepLoopCycleView()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Controls: Step Navigation & Play/Pause
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.overlaySurface.copy(alpha = 0.08f))
                    .clickable { onPreviousStep() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(Res.string.summary_btn_prev),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (currentStep > 0) colors.textMain else colors.textSecondary.copy(alpha = 0.5f)
                )
            }

            // Auto Play Toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (isAutoPlaying) colors.neonAccent.copy(alpha = 0.6f) else colors.strokeSubtle,
                        RoundedCornerShape(12.dp)
                    )
                    .background(if (isAutoPlaying) colors.neonAccent.copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { onToggleAutoPlay() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAutoPlaying) IconsManager.Stop else IconsManager.AutoMode,
                        contentDescription = null,
                        tint = if (isAutoPlaying) colors.neonAccent else colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAutoPlaying) stringResource(Res.string.summary_btn_auto_on) else stringResource(Res.string.summary_btn_auto_off),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = if (isAutoPlaying) colors.neonAccent else colors.textSecondary
                    )
                }
            }

            // Next
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.overlaySurface.copy(alpha = 0.08f))
                    .clickable { onNextStep() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(Res.string.summary_btn_next),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (currentStep < 3) colors.textMain else colors.textSecondary.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun StepAccumulationView() {
    val colors = AvalonTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.summary_step_1_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.textMain
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.summary_step_1_desc),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Visual stack
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.background.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MessageStackCard(
                title = stringResource(Res.string.summary_step_1_msg_1),
                subtitle = stringResource(Res.string.summary_step_1_msg_1_sub),
                count = 10,
                isHighlighted = false
            )
            MessageStackCard(
                title = stringResource(Res.string.summary_step_1_msg_2),
                subtitle = stringResource(Res.string.summary_step_1_msg_2_sub),
                count = 10,
                isHighlighted = false
            )
            MessageStackCard(
                title = stringResource(Res.string.summary_step_1_msg_3),
                subtitle = stringResource(Res.string.summary_step_1_msg_3_sub),
                count = 10,
                isHighlighted = false
            )
            MessageStackCard(
                title = stringResource(Res.string.summary_step_1_msg_4),
                subtitle = stringResource(Res.string.summary_step_1_msg_4_sub),
                count = 10,
                isHighlighted = false
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        ContextGaugeBadge(
            totalMessages = 40,
            statusText = stringResource(Res.string.summary_step_1_gauge),
            gaugeColor = colors.neonAccent
        )
    }
}

@Composable
private fun StepThresholdTriggerView() {
    val colors = AvalonTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.summary_step_2_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.textMain
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.summary_step_2_desc),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.background.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Highlighted oldest 10 messages
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pulseScale)
            ) {
                MessageStackCard(
                    title = stringResource(Res.string.summary_step_2_msg_highlight),
                    subtitle = stringResource(Res.string.summary_step_2_msg_highlight_sub),
                    count = 10,
                    isHighlighted = true
                )
            }

            val keepSub = stringResource(Res.string.summary_step_2_msg_keep_sub)
            MessageStackCard(title = stringResource(Res.string.summary_step_1_msg_2), subtitle = keepSub, count = 10, isHighlighted = false)
            MessageStackCard(title = stringResource(Res.string.summary_step_1_msg_3), subtitle = keepSub, count = 10, isHighlighted = false)
            MessageStackCard(title = stringResource(Res.string.summary_step_1_msg_4), subtitle = keepSub, count = 10, isHighlighted = false)
            MessageStackCard(
                title = stringResource(Res.string.summary_step_2_msg_new),
                subtitle = stringResource(Res.string.summary_step_2_msg_new_sub),
                count = 10,
                isHighlighted = false
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        ContextGaugeBadge(
            totalMessages = 50,
            statusText = stringResource(Res.string.summary_step_2_gauge),
            gaugeColor = Color(0xFFFFA726)
        )
    }
}

@Composable
private fun StepCompressionView() {
    val colors = AvalonTheme.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.summary_step_3_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.textMain
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.summary_step_3_desc),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.background.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Compressed Summary Capsule
            SummaryCrystalCard(
                title = stringResource(Res.string.summary_step_3_crystal_title),
                details = stringResource(Res.string.summary_step_3_crystal_desc)
            )

            val activeSub = stringResource(Res.string.summary_step_3_msg_active_sub)
            MessageStackCard(title = stringResource(Res.string.summary_step_1_msg_2), subtitle = activeSub, count = 10, isHighlighted = false)
            MessageStackCard(title = stringResource(Res.string.summary_step_1_msg_3), subtitle = activeSub, count = 10, isHighlighted = false)
            MessageStackCard(title = stringResource(Res.string.summary_step_1_msg_4), subtitle = activeSub, count = 10, isHighlighted = false)
            MessageStackCard(title = stringResource(Res.string.summary_step_2_msg_new), subtitle = activeSub, count = 10, isHighlighted = false)
        }

        Spacer(modifier = Modifier.height(10.dp))

        ContextGaugeBadge(
            totalMessages = 41,
            statusText = stringResource(Res.string.summary_step_3_gauge),
            gaugeColor = Color(0xFF66BB6A)
        )
    }
}

@Composable
private fun StepLoopCycleView() {
    val colors = AvalonTheme.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.summary_step_4_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.textMain
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.summary_step_4_desc),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.background.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Updated Summary Capsule
            SummaryCrystalCard(
                title = stringResource(Res.string.summary_step_4_crystal_title),
                details = stringResource(Res.string.summary_step_4_crystal_desc)
            )

            val activeSub = stringResource(Res.string.summary_step_3_msg_active_sub)
            MessageStackCard(title = stringResource(Res.string.summary_step_1_msg_3), subtitle = activeSub, count = 10, isHighlighted = false)
            MessageStackCard(title = stringResource(Res.string.summary_step_1_msg_4), subtitle = activeSub, count = 10, isHighlighted = false)
            MessageStackCard(title = stringResource(Res.string.summary_step_2_msg_new), subtitle = activeSub, count = 10, isHighlighted = false)
            MessageStackCard(
                title = stringResource(Res.string.summary_step_4_msg_new),
                subtitle = stringResource(Res.string.summary_step_2_msg_new_sub),
                count = 10,
                isHighlighted = false
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        ContextGaugeBadge(
            totalMessages = 41,
            statusText = stringResource(Res.string.summary_step_4_gauge),
            gaugeColor = colors.neonAccent
        )
    }
}

@Composable
private fun MessageStackCard(
    title: String,
    subtitle: String,
    count: Int,
    isHighlighted: Boolean
) {
    val colors = AvalonTheme.colors
    val borderCol = if (isHighlighted) colors.neonAccent else colors.strokeSubtle.copy(alpha = 0.4f)
    val bgCol = if (isHighlighted) colors.neonAccent.copy(alpha = 0.16f) else colors.overlaySurface.copy(alpha = 0.05f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            .background(bgCol)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isHighlighted) colors.neonAccent else colors.textMain
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = colors.textSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isHighlighted) colors.neonAccent.copy(alpha = 0.3f) else colors.overlaySurface.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count ${stringResource(Res.string.summary_msg_count_suffix)}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isHighlighted) colors.neonAccent else colors.textSecondary
            )
        }
    }
}

@Composable
private fun SummaryCrystalCard(
    title: String,
    details: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.5.dp,
                color = Color(0xFFAB47BC),
                shape = RoundedCornerShape(10.dp)
            )
            .background(Color(0xFFAB47BC).copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = IconsManager.Psychology,
                contentDescription = null,
                tint = Color(0xFFCE93D8),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFF3E5F5)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = details,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
            color = Color(0xFFE1BEE7)
        )
    }
}

@Composable
private fun ContextGaugeBadge(
    totalMessages: Int,
    statusText: String,
    gaugeColor: Color
) {
    val colors = AvalonTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(gaugeColor.copy(alpha = 0.1f))
            .border(1.dp, gaugeColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(gaugeColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = 14.sp
                ),
                color = colors.textMain,
                softWrap = true,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(gaugeColor.copy(alpha = 0.2f))
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Σ: $totalMessages",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = gaugeColor
            )
        }
    }
}
