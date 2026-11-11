package com.ensodai.avalon.presentation.addCharacterScreen.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.data.db.dto.CategoryItemDto
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.categories_stage
import com.ensodai.avalon.presentation.commonComponents.CategoryItem
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesStageScreen(
    categories: List<CategoryItemDto>?,
    selectedCategory: CategoryItemDto?,
    onCategorySelected: (CategoryItemDto) -> Unit
) {
    val colors = AvalonTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(Res.string.categories_stage),
            style = TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMain
            )
        )
        FlowRow {
            categories?.forEach { category ->
                CategoryItem(
                    isSelected = selectedCategory == category,
                    item = category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}
