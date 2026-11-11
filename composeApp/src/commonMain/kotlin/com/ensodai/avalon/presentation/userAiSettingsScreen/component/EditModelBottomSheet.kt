package com.ensodai.avalon.presentation.userAiSettingsScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.user.models.AiModelCatalogItem
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.user_ai_catalog_all
import com.ensodai.avalon.generated.resources.user_ai_catalog_empty
import com.ensodai.avalon.generated.resources.user_ai_catalog_loading
import com.ensodai.avalon.generated.resources.user_ai_catalog_recommended
import com.ensodai.avalon.generated.resources.user_ai_catalog_search_hint
import com.ensodai.avalon.generated.resources.user_ai_edit_model_banner_subtitle
import com.ensodai.avalon.generated.resources.user_ai_edit_model_banner_title
import com.ensodai.avalon.presentation.commonComponents.textFields.TechnoInput
import com.ensodai.avalon.presentation.userAiSettingsScreen.viewState.ModelType
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditModelBottomSheet(
    modelType: ModelType,
    title: String,
    description: String,
    currentValue: String,
    catalogModels: List<AiModelCatalogItem>,
    isCatalogLoading: Boolean,
    catalogSearchQuery: String,
    onUpdateCatalogSearch: (String) -> Unit,
    onSelectModel: (type: ModelType, modelId: String) -> Unit,
    onNavigateToSummaryExplanation: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val colors = AvalonTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var localSelectedId by remember(currentValue) { mutableStateOf(currentValue) }

    val query = catalogSearchQuery.trim().lowercase()
    val filteredModels = remember(catalogModels, query) {
        if (query.isBlank()) {
            catalogModels
        } else {
            catalogModels.filter {
                it.name.lowercase().contains(query) ||
                        it.id.lowercase().contains(query) ||
                        it.description?.lowercase()?.contains(query) == true
            }
        }
    }

    val (recommended, others) = remember(filteredModels) {
        filteredModels.partition { it.isRecommended }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        contentColor = colors.textMain,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = colors.strokeSubtle
            )
        },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            item(key = "header_title_desc") {
                // Заголовок
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textMain,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Описание
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Баннер схемы работы памяти (для Summary)
            if (modelType == ModelType.SUMMARY && onNavigateToSummaryExplanation != null) {
                item(key = "summary_banner") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = colors.strokeSubtle,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(colors.overlaySurface.copy(alpha = 0.04f))
                            .clickable {
                                onDismiss()
                                onNavigateToSummaryExplanation()
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = IconsManager.GeminiIcon,
                                contentDescription = null,
                                tint = colors.textMain,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.user_ai_edit_model_banner_title),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textMain
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(Res.string.user_ai_edit_model_banner_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Icon(
                                imageVector = IconsManager.KeyboardArrowRight,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Поле поиска по каталогу
            if (catalogModels.size > 3 || isCatalogLoading) {
                item(key = "search_field") {
                    TechnoInput(
                        value = catalogSearchQuery,
                        onValueChange = onUpdateCatalogSearch,
                        placeholder = stringResource(Res.string.user_ai_catalog_search_hint),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Индикатор загрузки каталога
            if (isCatalogLoading) {
                item(key = "catalog_loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colors.textMain,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(Res.string.user_ai_catalog_loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            } else if (catalogModels.isNotEmpty()) {
                // Секция 1: Рекомендуемые
                if (recommended.isNotEmpty()) {
                    item(key = "header_rec") {
                        Text(
                            text = stringResource(Res.string.user_ai_catalog_recommended),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textMain,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    items(
                        items = recommended,
                        key = { "rec_${it.id}" }
                    ) { model ->
                        CatalogModelCard(
                            model = model,
                            isSelected = localSelectedId.equals(model.id, ignoreCase = true),
                            onClick = {
                                localSelectedId = model.id
                                onSelectModel(modelType, model.id)
                            },
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    item(key = "spacer_rec") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Секция 2: Все остальные модели
                if (others.isNotEmpty()) {
                    item(key = "header_others") {
                        Text(
                            text = stringResource(Res.string.user_ai_catalog_all),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textMain,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    items(
                        items = others,
                        key = { "other_${it.id}" }
                    ) { model ->
                        CatalogModelCard(
                            model = model,
                            isSelected = localSelectedId.equals(model.id, ignoreCase = true),
                            onClick = {
                                localSelectedId = model.id
                                onSelectModel(modelType, model.id)
                            },
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                }

                if (filteredModels.isEmpty()) {
                    item(key = "catalog_empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.user_ai_catalog_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CatalogModelCard(
    model: AiModelCatalogItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val borderColor = if (isSelected) colors.textMain else colors.strokeSubtle
    val bgColor = colors.overlaySurface.copy(alpha = if (isSelected) 0.14f else 0.04f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Первая строка: Название слева (полный текст) и Бейдж справа вверху
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textMain
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = model.id,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = colors.textSecondary.copy(alpha = 0.8f)
                    )
                }

                // Бейдж в правом верхнем углу
                if (!model.badge.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.overlaySurface.copy(alpha = 0.15f))
                            .border(1.dp, colors.strokeSubtle, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = model.badge!!,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textMain
                        )
                    }
                }
            }

            // Вторая строка: Описание (если есть)
            if (!model.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = model.description!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Третья строка: Характеристики (Чипы)
            val hasChips = !model.contextWindow.isNullOrBlank() || !model.promptPrice.isNullOrBlank() || !model.completionPrice.isNullOrBlank()
            if (hasChips) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Контекстное окно
                    if (!model.contextWindow.isNullOrBlank()) {
                        ModelSpecChip(
                            label = "Контекст: ${model.contextWindow}"
                        )
                    }

                    // Стоимость входных токенов / генерации
                    if (!model.promptPrice.isNullOrBlank()) {
                        val promptLabel = if (model.completionPrice != null) {
                            "Вход: ${model.promptPrice}"
                        } else {
                            "Тариф: ${model.promptPrice}"
                        }
                        ModelSpecChip(label = promptLabel)
                    }

                    // Стоимость выходных токенов
                    if (!model.completionPrice.isNullOrBlank()) {
                        ModelSpecChip(
                            label = "Выход: ${model.completionPrice}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelSpecChip(
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.overlaySurface.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = colors.strokeSubtle,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = colors.textSecondary
        )
    }
}
