package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.data.db.dto.CategoryItemDto
import com.ensodai.avalon.data.db.dto.SelectableCategoryDto
import com.ensodai.avalon.data.db.dto.ServerCategoryDto
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun CategoryItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    item: CategoryItemDto,
    onClick: () -> Unit,
    backgroundColor: Color = Color.Transparent
) {
    val colors = AvalonTheme.colors
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) colors.textMain else colors.textMain.copy(alpha = 0.7f),
        animationSpec = tween(durationMillis = 300)
    )
    GemPill(
        modifier = Modifier.padding(8.dp),
        contentPadding = PaddingValues(8.dp),
        onClick = onClick
    ) {
        Row {
            Text(
                text = item.localizedName,
                color = animatedBorderColor,
                style = MaterialTheme.typography.bodyMedium
            )

            when (item) {
                is SelectableCategoryDto -> {}
                is ServerCategoryDto -> {
                    if (item.count > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.count.toString(),
                            color = animatedBorderColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
//    Box(
//        modifier = modifier
//            .padding(8.dp)
//            .clip(RoundedCornerShape(12.dp))
//            .border(
//                width = 2.dp,
//                color = animatedBorderColor,
//                shape = RoundedCornerShape(12.dp)
//            )
//            .clickable { onClick() }
//            .background(backgroundColor)
//            .padding(horizontal = 16.dp, vertical = 8.dp)
//
//    ) {
//        Row {
//            Text(
//                text = item.localizedName,
//                color = animatedBorderColor,
//                style = MaterialTheme.typography.bodyMedium
//            )
//
//            when (item) {
//                is SelectableCategoryDto -> {}
//                is ServerCategoryDto -> {
//                    if (item.count > 0) {
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = item.count.toString(),
//                            color = animatedBorderColor,
//                            style = MaterialTheme.typography.bodyMedium
//                        )
//                    }
//                }
//            }
//        }
//    }
}