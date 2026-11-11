package com.ensodai.avalon.presentation.importCharacterScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.presentation.commonComponents.GemPill
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.commonComponents.cardMonolith.AvalonCardCustomizerSection
import com.ensodai.avalon.presentation.commonComponents.cardMonolith.AvalonMonolithCard
import com.ensodai.avalon.presentation.importCharacterScreen.action.ImportCharacterScreenActions
import com.ensodai.avalon.presentation.importCharacterScreen.component.TopBarImportCharacterScreen
import com.ensodai.avalon.presentation.importCharacterScreen.viewState.ImportCharacterScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

@Composable
fun ImportCharacterScreenContent(
    viewState: ImportCharacterScreenViewState,
    actions: ImportCharacterScreenActions,
    navigation: Navigation,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val scrollState = rememberScrollState()

    val filePicker = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("png", "json")),
        mode = FileKitMode.Single,
        onResult = { file: PlatformFile? ->
            actions.onFilePicked(file)
        }
    )

    // При успешном импорте переходим сразу в чат с созданным персонажем
    LaunchedEffect(viewState.isImportSuccess, viewState.importedCharacterId) {
        if (viewState.isImportSuccess && viewState.importedCharacterId != null) {
            navigation.navigateToChatScreen(viewState.importedCharacterId)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            TopBarImportCharacterScreen(
                onBackButtonClick = { navigation.navigateBack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Ошибка если есть
            AnimatedVisibility(visible = viewState.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceHigh)
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = IconsManager.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = viewState.errorMessage.orEmpty(),
                            color = colors.textMain,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = IconsManager.Close,
                            contentDescription = "Закрыть",
                            tint = colors.textSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { actions.onClearError() }
                        )
                    }
                }
            }

            // Зона выбора файла или монолитная карточка
            if (viewState.parsedCardData == null) {
                // Зона Drag & Drop / Выбора файла
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.cardBackground)
                        .border(1.5.dp, colors.surfaceHigh, RoundedCornerShape(20.dp))
                        .clickable { filePicker.launch() }
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.neonAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconsManager.Download,
                                contentDescription = null,
                                tint = colors.neonAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "Выберите карточку персонажа",
                            color = colors.textMain,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Поддерживаются PNG с метаданными V2/V3 (SillyTavern, Chub, TavernAI) и файлы .json",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        TechnoButton(
                            text = "Выбрать файл",
                            onClick = { filePicker.launch() },
                            isLoading = viewState.isLoading,
                            isPrimary = true
                        )
                    }
                }
            } else {
                val card = viewState.parsedCardData
                val estimatedTokens = (card.description.length + card.personality.length + card.firstMes.length + card.systemPrompt.length) / 4

                // 1. Фирменная карточка Dark Technical Monolith
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AvalonMonolithCard(
                        characterName = card.name,
                        nickname = card.creator.ifBlank { null },
                        creatorNick = card.creator.ifBlank { null },
                        shortDescription = card.description.ifBlank { card.personality },
                        tags = card.tags,
                        avatarModel = if (viewState.isPngFile) viewState.fileBytes else null,
                        hasLorebook = card.characterBook != null,
                        versionText = card.characterVersion.ifBlank { "v1.0" },
                        specVersion = viewState.specVersion ?: "V3",
                        tokenCount = estimatedTokens.coerceAtLeast(350),
                        altGreetingsCount = card.alternateGreetings.size,
                        customization = viewState.customization,
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .widthIn(max = 300.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewState.selectedFileName ?: "Файл загружен",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Text(
                            text = "Выбрать другой файл",
                            color = colors.neonAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { actions.onResetFile() }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        )
                    }
                }

                // 2. Секция стилизации карточки Dark Monolith
                AvalonCardCustomizerSection(
                    customization = viewState.customization,
                    onCustomizationChange = actions.onUpdateCustomization,
                    hasAvatar = viewState.isPngFile
                )

                // 3. Информационный блок карточки
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.cardBackground)
                        .border(1.dp, colors.surfaceHigh, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ДАННЫЕ ПЕРСОНАЖА",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    if (card.firstMes.isNotBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceHigh)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Первое сообщение:",
                                color = colors.neonAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = card.firstMes,
                                color = colors.textMain,
                                fontSize = 13.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val book = card.characterBook
                    if (book != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = IconsManager.Psychology,
                                contentDescription = null,
                                tint = colors.neonAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "База знаний: ${book.name ?: "Лорбук"} (${book.entries.size} записей)",
                                color = colors.textMain,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (card.alternateGreetings.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = IconsManager.Chat,
                                contentDescription = null,
                                tint = colors.neonAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Альтернативных приветствий: ${card.alternateGreetings.size}",
                                color = colors.textMain,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (card.tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            card.tags.take(4).forEach { tag ->
                                GemPill {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Выбор видимости
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.cardBackground)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Видимость созданного персонажа",
                        color = colors.textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VisibilityOptionChip(
                            title = "Приватный",
                            isSelected = viewState.selectedVisibility == CharacterVisibility.PRIVATE,
                            onClick = { actions.onChangeVisibility(CharacterVisibility.PRIVATE) },
                            modifier = Modifier.weight(1f)
                        )

                        VisibilityOptionChip(
                            title = "Публичный",
                            isSelected = viewState.selectedVisibility == CharacterVisibility.PUBLIC,
                            onClick = { actions.onChangeVisibility(CharacterVisibility.PUBLIC) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 5. Кнопка импорта
                TechnoButton(
                    text = "Импортировать персонажа",
                    onClick = { actions.onImportClick() },
                    isLoading = viewState.isLoading,
                    enabled = !viewState.isLoading,
                    isPrimary = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun VisibilityOptionChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.neonAccent.copy(alpha = 0.2f) else colors.surfaceHigh)
            .border(
                width = 1.dp,
                color = if (isSelected) colors.neonAccent else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) colors.neonAccent else colors.textSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
