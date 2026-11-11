package com.ensodai.avalon.presentation.editCharacterScreen.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.shimmer.ShimmerContainer
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme

enum class EditActionType {
    NAVIGATION,
    SHEET
}

@Composable
fun EditFieldCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String?,
    placeholder: String = "Не задано",
    icon: ImageVector,
    actionType: EditActionType,
    onClick: () -> Unit,
    isUpdating: Boolean
) {
    LocalHapticFeedback.current
    val colors = AvalonTheme.colors
    val iconColor = colors.textMain

    GemCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onClick,
        enabled = !isUpdating,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            ShimmerContainer(
                isLoading = isUpdating,
                cornerShape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier
                        .size(26.dp)
                        .alpha(0.9f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                ShimmerContainer(
                    isLoading = isUpdating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp
                        ),
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                ShimmerContainer(
                    isLoading = isUpdating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (value.isNullOrBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.placeholderText,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.textMain,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!isUpdating) {
                val iconSize = if (actionType == EditActionType.NAVIGATION) 24.dp else 18.dp
                Icon(
                    imageVector = if (actionType == EditActionType.NAVIGATION)
                        IconsManager.KeyboardArrowRight
                    else
                        IconsManager.Edit,
                    contentDescription = null,
                    tint = colors.strokeSubtle,
                    modifier = Modifier.size(iconSize)
                )
            } else {
                Box(modifier = Modifier.size(24.dp))
            }
        }
    }
}
