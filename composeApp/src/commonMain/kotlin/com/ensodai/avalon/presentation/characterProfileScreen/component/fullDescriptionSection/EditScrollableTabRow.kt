package com.ensodai.avalon.presentation.characterProfileScreen.component.fullDescriptionSection

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.first_message_stage
import com.ensodai.avalon.generated.resources.full_description_stage
import com.ensodai.avalon.generated.resources.safety_and_censor_stage
import com.ensodai.avalon.generated.resources.temperature
import com.ensodai.avalon.generated.resources.visible
import com.ensodai.avalon.generated.resources.welcome_card_stage
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditScrollableTabRow(pagerState: PagerState, items: List<ProfileSettings>) {
    val colors = AvalonTheme.colors
    val coroutineScope = rememberCoroutineScope()
    PrimaryScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        indicator = {
            Box(
                Modifier
                    .tabIndicatorOffset(pagerState.currentPage, matchContentSize = true)
                    .height(4.dp)
                    .background(
                        color = colors.textMain.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
            )
        }
    ) {
        items.forEachIndexed { index, setting ->
            val settingName = getSettingName(setting)
            val isSelected = pagerState.currentPage == index

            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .height(40.dp)
                    .background(
                        color = if (isSelected)
                            colors.textMain.copy(alpha = 0.1f)
                        else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        indication = ripple(
                            bounded = true,
                            color = colors.textMain
                        ),
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = settingName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected)
                        colors.textMain
                    else colors.textMain.copy(alpha = 0.7f),
                    modifier = Modifier.animateContentSize()
                )
            }
        }
    }
}

@Composable
private fun getSettingName(setting: ProfileSettings): String {
    return when (setting) {
        ProfileSettings.FullDescriptionSetting -> stringResource(Res.string.full_description_stage)
        ProfileSettings.PrivacySettings -> stringResource(Res.string.visible)
        ProfileSettings.TemperatureSettings -> stringResource(Res.string.temperature)
        ProfileSettings.SafetySettings -> stringResource(Res.string.safety_and_censor_stage)
        ProfileSettings.FirstMessageSettings -> stringResource(Res.string.first_message_stage)
        ProfileSettings.WelcomeCardSettings -> stringResource(Res.string.welcome_card_stage)
    }
}
