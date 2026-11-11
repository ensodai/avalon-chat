package com.ensodai.avalon.presentation.commonComponents.privacyCards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.link_only_setting
import com.ensodai.avalon.generated.resources.link_only_setting_description
import com.ensodai.avalon.generated.resources.private_setting
import com.ensodai.avalon.generated.resources.private_setting_description
import com.ensodai.avalon.generated.resources.public_setting
import com.ensodai.avalon.generated.resources.public_setting_description
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrivacyCardColumn(
    modifier: Modifier = Modifier,
    selectedOption: CharacterVisibility,
    onOptionSelected: (CharacterVisibility) -> Unit
) {
    Column(modifier) {
        AvailabilityCard(
            title = stringResource(Res.string.public_setting),
            description = stringResource(Res.string.public_setting_description),
            isSelected = selectedOption == CharacterVisibility.PUBLIC,
            onClick = { onOptionSelected(CharacterVisibility.PUBLIC) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        AvailabilityCard(
            title = stringResource(Res.string.link_only_setting),
            description = stringResource(Res.string.link_only_setting_description),
            isSelected = selectedOption == CharacterVisibility.LINK_ONLY,
            onClick = { onOptionSelected(CharacterVisibility.LINK_ONLY) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        AvailabilityCard(
            title = stringResource(Res.string.private_setting),
            description = stringResource(Res.string.private_setting_description),
            isSelected = selectedOption == CharacterVisibility.PRIVATE,
            onClick = { onOptionSelected(CharacterVisibility.PRIVATE) }
        )
    }

}

@Composable
fun AvailabilityCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) colors.textMain else colors.textSecondary,
        animationSpec = tween(durationMillis = 300)
    )

    val animatedTitleColor by animateColorAsState(
        targetValue = if (isSelected) colors.textMain else colors.textSecondary,
        animationSpec = tween(durationMillis = 300)
    )

    val backgroundColor = AvalonTheme.colors.overlaySurface.copy(alpha = if (isSelected) 0.14f else 0.04f)
    val textColorDescription = colors.textSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = animatedTitleColor,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = description,
                color = textColorDescription,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
