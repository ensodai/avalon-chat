package com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.private_setting_full_description
import com.ensodai.avalon.generated.resources.public_setting_full_description
import com.ensodai.avalon.presentation.commonComponents.ExpressivePopupMenu
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrivacyFullDescriptionDropDownMenu(
    modifier: Modifier,
    characterVisibility: CharacterVisibility,
    onChangeCharacterVisibility: (CharacterVisibility) -> Unit
) {
    ExpressivePopupMenu(
        modifier = modifier,
        triggerContent = { onClick ->
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = IconsManager.Visibility,
                    contentDescription = "Visibility",
                    tint = AvalonTheme.colors.textMain
                )
            }
        },
        menuContent = { onDismiss ->
            PrivacyFullDescriptionCards(
                modifier = Modifier.padding(4.dp),
                selectedOption = characterVisibility,
                onOptionSelected = {
                    onChangeCharacterVisibility(it)
                    onDismiss()
                }
            )
        }
    )
}

@Composable
fun PrivacyFullDescriptionCards(
    modifier: Modifier = Modifier,
    selectedOption: CharacterVisibility,
    onOptionSelected: (CharacterVisibility) -> Unit
) {

    Column(
        modifier = modifier,
//        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PrivacyFullDescriptionCard(
            tittle = stringResource(Res.string.public_setting_full_description),
            isSelected = selectedOption == CharacterVisibility.PUBLIC,
            onClick = { onOptionSelected(CharacterVisibility.PUBLIC) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        PrivacyFullDescriptionCard(
            tittle = stringResource(Res.string.private_setting_full_description),
            isSelected = selectedOption == CharacterVisibility.PRIVATE,
            onClick = { onOptionSelected(CharacterVisibility.PRIVATE) }
        )
    }
}

@Composable
fun PrivacyFullDescriptionCard(
    tittle: String,
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
        Text(
            text = tittle,
            color = animatedTitleColor,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}
