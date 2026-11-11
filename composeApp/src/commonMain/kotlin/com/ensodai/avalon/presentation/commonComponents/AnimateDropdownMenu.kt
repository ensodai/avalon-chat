package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme

// 1. Новый современный вариант с кастомным триггером (используется в настройках)
@Composable
fun AnimateDropdownMenu(
    selectedItem: String?,
    itemsList: List<String>,
    onChangeItem: (String) -> Unit,
    modifier: Modifier = Modifier,
    triggerContent: @Composable (onClick: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = AvalonTheme.colors

    Box(modifier = modifier) {
        triggerContent { expanded = true }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            itemsList.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        onChangeItem(item)
                        expanded = false
                    },
                    trailingIcon = {
                        if (selectedItem == item) {
                            Icon(
                                imageVector = IconsManager.Check,
                                contentDescription = "Selected",
                                tint = colors.neonAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

// 2. Старый классический вариант с OutlinedTextField для совместимости с другими экранами
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimateDropdownMenu(
    selectedItem: String?,
    itemsList: List<String>,
    onChangeItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val animatedHeight by animateDpAsState(
        targetValue = if (expanded) with(density) { (itemsList.size * 52).dp } else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "DpAnimation"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = selectedItem.orEmpty(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                BaseRotateAnimation(isRotated = expanded) {
                    Icon(
                        imageVector = IconsManager.ExpandMore,
                        contentDescription = "Dropdown Arrow",
                    )
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size
                }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .height(animatedHeight)
                .width(with(density) { textFieldSize.width.toDp() })
        ) {
            itemsList.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        onChangeItem(item)
                        expanded = false
                    },
                    trailingIcon = {
                        if (selectedItem == item) {
                            Icon(
                                imageVector = IconsManager.Check,
                                contentDescription = "Selected"
                            )
                        }
                    }
                )
            }
        }
    }
}
