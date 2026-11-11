package com.ensodai.avalon.presentation.chatScreen.component.bottomBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import com.ensodai.avalon.generated.resources.ai_warning_text
import com.ensodai.avalon.generated.resources.ai_warning_link
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ensodai.avalon.core.pagination.PaginatorState
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.shared.avalon.gifts.models.Gift
import com.ensodai.shared.avalon.gifts.models.GiftPack
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.message
import com.ensodai.avalon.generated.resources.out_of
import com.ensodai.avalon.generated.resources.remark_explanation
import com.ensodai.avalon.generated.resources.system_instruction_placeholder
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.chatScreen.component.bottomBar.gift.GiftPickerLayout
import com.ensodai.avalon.presentation.chatScreen.viewState.ActionButtonState
import com.ensodai.avalon.presentation.chatScreen.viewState.BottomBarMode
import com.ensodai.avalon.presentation.chatScreen.viewState.BottomBarState
import com.ensodai.avalon.presentation.chatScreen.viewState.TextFieldMode
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun TextFieldSection(
    modifier: Modifier,
    actionButtonState: ActionButtonState,
    isLoadingFiles: Boolean,
    attachedImages: List<CharacterImageDTO>,
    bottomBarState: BottomBarState,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    onSendMessageClick: (String) -> Unit,
    onForceGenerateResponseClick: () -> Unit,
    onShowPermissionDialog: () -> Unit,
    onChangeMessageText: (String) -> Unit,
    isGenerateResponseButtonEnable: Boolean,
    isEnableSearchMessageMode: Boolean,
    searchMessageCount: Int,
    selectedMessageNumber: Int,
    onOpenGiftsMagazine: () -> Unit,
    onStopGenerateResponse: () -> Unit,
    onSendGift: (Gift) -> Unit,
    onAttachImages: (List<CharacterImageDTO>) -> Unit,
    onDetachImages: (CharacterImageDTO) -> Unit,
    onToggleBottomBarMenu: () -> Unit,
    onSetInstructionMode: (Boolean) -> Unit,
    onSetMessageSystemInstruction: (String) -> Unit,
    onClearSystemInstruction: () -> Unit,
    onToggleFloatingPlusButton: (Boolean) -> Unit,
    myGiftsState: PaginatorState<GiftPack>,
    allGiftsState: PaginatorState<GiftPack>,
    showAiWarning: Boolean,
    onShowAiWarningClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val haptic = LocalHapticFeedback.current
    val isMessageInstructionMode = bottomBarState.textFieldMode == TextFieldMode.MessageInstruction
    val isMainBottomSheetMenuMode =
        bottomBarState.bottomBarMode == BottomBarMode.MainBottomSheetMenuMode
    val isGiftBottomSheetMode = bottomBarState.bottomBarMode == BottomBarMode.GiftBottomSheetMode

    val isAnyBottomSheetOpen = isMainBottomSheetMenuMode || isGiftBottomSheetMode

    // Запоминаем последний активный режим меню, чтобы при закрытии оно не пропадало мгновенно.
    // СИНХРОННОЕ обновление предотвращает визуальный прыжок.
    var activeBottomBarMode by remember { mutableStateOf(bottomBarState.bottomBarMode) }
    if (bottomBarState.bottomBarMode != BottomBarMode.Idle) {
        activeBottomBarMode = bottomBarState.bottomBarMode
    }

    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isMessageInstructionMode) {
        if (isMessageInstructionMode) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // Состояние: кнопка "+" видна внутри строки
    var isPlusButtonInline by remember { mutableStateOf(true) }

    // Состояние перехода от меню к клавиатуре.
    // Если true, мы удерживаем высоту меню, пока клавиатура выезжает,
    // чтобы текстовое поле не проваливалось вниз.
    var isTransitioningMenuToIme by remember { mutableStateOf(false) }

    // Размеры
    var inputBarHeight by remember { mutableStateOf(0.dp) }
    var textFieldTotalWidth by remember { mutableIntStateOf(0) }

    // Отслеживание клавиатуры и навигации
    val imeInsets = WindowInsets.ime
    val imeHeightPx = imeInsets.getBottom(density)
    val navBarInsets = WindowInsets.navigationBars
    val navBarHeightPx = navBarInsets.getBottom(density)

    // Анимация отступа NavigationBar (если меню открыто - отступ 0, иначе - высота навбара)
    val animatedNavBarPaddingPx by animateFloatAsState(
        targetValue = if (isAnyBottomSheetOpen) 0f else navBarHeightPx.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "NavBarPaddingAnimation"
    )

    // --- ЛОГИКА АНИМАЦИИ МЕНЮ ---

    // Определяем, нужно ли "мгновенное" появление (snap).
    // Если меню включается (true) И клавиатура сейчас видна (ime > 0) -> snap(1f).
    // Это нужно, чтобы меню оказалось под клавиатурой ДО того, как она начнет уезжать.
    val shouldSnap = isAnyBottomSheetOpen && imeHeightPx > 0

    val menuAnimationProgress by animateFloatAsState(
        targetValue = if (isAnyBottomSheetOpen || isTransitioningMenuToIme) 1f else 0f,
        animationSpec = if (shouldSnap) {
            snap() // Мгновенно 1.0, без анимации
        } else {
            spring(stiffness = Spring.StiffnessMedium) // Плавная пружина
        },
        label = "MenuAnimationProgress"
    )
// СБРОС: Если анимация закончилась и в основном стейте Idle — ставим Idle в локальный стейт
    LaunchedEffect(menuAnimationProgress, bottomBarState.bottomBarMode) {
        if (menuAnimationProgress == 0f && bottomBarState.bottomBarMode == BottomBarMode.Idle) {
            activeBottomBarMode = BottomBarMode.Idle
        }
    }
    // Управление клавиатурой и фокусом
    LaunchedEffect(isAnyBottomSheetOpen) {
        if (isAnyBottomSheetOpen) {
            // Как только включили меню - скрываем клавиатуру
            keyboardController?.hide()
            focusManager.clearFocus(force = true)
        }
    }

    // Состояние текста
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = bottomBarState.messageText))
    }

    LaunchedEffect(isPlusButtonInline) {
        onToggleFloatingPlusButton(!isPlusButtonInline)
    }

    // Синхронизация текста при смене режима
    LaunchedEffect(isMessageInstructionMode) {
        val text = if (isMessageInstructionMode) {
            bottomBarState.messageSystemInstructionText.orEmpty()
        } else {
            bottomBarState.messageText
        }
        textFieldValue = TextFieldValue(text = text, selection = TextRange(text.length))
    }

    // Синхронизация текста из VM
    LaunchedEffect(bottomBarState.messageText) {
        if (!isMessageInstructionMode && bottomBarState.messageText != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = bottomBarState.messageText,
                selection = TextRange(bottomBarState.messageText.length)
            )
        }
    }

    // Закрываем меню, если начали печатать
    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text.isNotEmpty() && isAnyBottomSheetOpen) {
            onToggleBottomBarMenu()
        }
    }

    // Закрываем меню, если клавиатура открылась (imeHeight > 0)
    var previousImeHeight by remember { mutableIntStateOf(0) }
    LaunchedEffect(imeHeightPx) {
        if (imeHeightPx > 0 && previousImeHeight == 0 && isAnyBottomSheetOpen) {
            isTransitioningMenuToIme = true
            onToggleBottomBarMenu()
        }
        previousImeHeight = imeHeightPx
    }

    // Сбрасываем флаг удержания высоты, когда анимация клавиатуры завершена.
    // Это позволяет текстовому полю плавно "упасть" на клавиатуру (или остаться, если клава выше).
    LaunchedEffect(isTransitioningMenuToIme) {
        if (isTransitioningMenuToIme) {
            kotlinx.coroutines.delay(300)
            isTransitioningMenuToIme = false
        }
    }

    val textFieldSpring = spring<IntSize>(
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioNoBouncy
    )

    // Calculate preferred height for Gift Picker (match keyboard or default min 300dp)
    remember(previousImeHeight) {
        val minHeightPx = with(density) { 300.dp.roundToPx() }
        val targetPx =
            if (previousImeHeight > 0) max(previousImeHeight, minHeightPx) else minHeightPx
        with(density) { targetPx.toDp() }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedVisibility(
            visible = showAiWarning && !isMessageInstructionMode && !isEnableSearchMessageMode,
            modifier = Modifier.fillMaxWidth()
        ) {
            val warningText = stringResource(Res.string.ai_warning_text)
            val linkText = stringResource(Res.string.ai_warning_link)
            val annotatedString = buildAnnotatedString {
                append(warningText)
                pushStringAnnotation(tag = "more_info", annotation = "more_info")
                withStyle(
                    style = SpanStyle(
                        color = colors.neonAccent,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(linkText)
                }
                pop()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                ClickableText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    ),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "more_info", start = offset, end = offset)
                            .firstOrNull()?.let {
                                onShowAiWarningClick()
                            }
                    }
                )
            }
        }

        Box(
            modifier = Modifier.zIndex(1f),
            contentAlignment = Alignment.BottomEnd
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { with(density) { inputBarHeight = it.height.toDp() } },
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 8.dp)
                        .shadow(elevation = 0.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .let {
                            if (colors.strokeSubtle == Color.Transparent) it
                            else it.border(1.dp, colors.strokeSubtle, RoundedCornerShape(16.dp))
                        }
                        .background(color = colors.surfaceHigh)
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            val safeValue = if (newValue.text.length > 1000) {
                                val truncated = newValue.text.take(1000)
                                newValue.copy(
                                    text = truncated,
                                    selection = TextRange(truncated.length.coerceAtMost(newValue.selection.start))
                                )
                            } else newValue
                            textFieldValue = safeValue
                            if (!isMessageInstructionMode) onChangeMessageText(safeValue.text)
                        },
                        onTextLayout = { res ->
                            val lastLineRight = res.getLineRight(res.lineCount - 1)
                            val buttonsWidth = with(density) { 110.dp.toPx() }
                            if (textFieldTotalWidth > 0) {
                                isPlusButtonInline = if (textFieldValue.text.isEmpty()) true
                                else if (res.lineCount > 1) false
                                else lastLineRight < (textFieldTotalWidth - buttonsWidth)
                            }
                        },
                        modifier = modifier
                            .testTag(BenchmarkTags.ChatInputField)
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onSizeChanged { textFieldTotalWidth = it.width }
                            .padding(0.dp)
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown &&
                                    keyEvent.key == Key.Enter &&
                                    !keyEvent.isShiftPressed
                                ) {
                                    if (textFieldValue.text.isNotBlank() && !isEnableSearchMessageMode && !isMessageInstructionMode) {
                                        onSendMessageClick(textFieldValue.text)
                                        textFieldValue = TextFieldValue("")
                                        isPlusButtonInline = true
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                            .animateContentSize(textFieldSpring, Alignment.BottomStart),

                        cursorBrush = Brush.linearGradient(
                            listOf(
                                colors.textMain,
                                colors.textMain
                            )
                        ),
                        maxLines = 6,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.textMain),
                        decorationBox = { innerTextField ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                AnimatedVisibility(
                                    visible = isMessageInstructionMode,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.remark_explanation),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = colors.textMain
                                        ),
                                        modifier = Modifier.padding(
                                            start = 14.dp,
                                            end = 14.dp,
                                            top = 8.dp,
                                            bottom = 4.dp
                                        )
                                    )
                                }
                                BaseItemAlphaAnimation(isVisible = !isEnableSearchMessageMode) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (bottomBarState.messageSystemInstructionText != null && !isMessageInstructionMode) {
                                            InstructionIndicator(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(start = 12.dp, top = 8.dp)
                                                    .zIndex(2f),
                                                onRemove = onClearSystemInstruction
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    start = 14.dp,
                                                    top = if (bottomBarState.messageSystemInstructionText != null && !isMessageInstructionMode) 38.dp else 16.dp,
                                                    bottom = 12.dp,
                                                    end = 56.dp
                                                )
                                        ) {
                                            if (textFieldValue.text.isEmpty()) {
                                                Text(
                                                    text = if (isMessageInstructionMode) stringResource(
                                                        Res.string.system_instruction_placeholder
                                                    ) else stringResource(
                                                        Res.string.message
                                                    ),
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        color = colors.placeholderText
                                                    )
                                                )
                                            }
                                            innerTextField()
                                        }
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(end = 4.dp)
                                        ) {
                                            SendMessageButtons(
                                                actionButtonState = actionButtonState,
                                                attachedImagesSize = attachedImages.size,
                                                onSendMessageClick = {
                                                    onSendMessageClick(textFieldValue.text)
                                                    textFieldValue = TextFieldValue("")
                                                    isPlusButtonInline = true
                                                },
                                                onForceGenerateResponseClick = onForceGenerateResponseClick,
                                                onShowPermissionDialog = onShowPermissionDialog,
                                                isGenerateResponseButtonEnable = isGenerateResponseButtonEnable,
                                                onClickAddIconButton = { onToggleBottomBarMenu() },
                                                onStopGenerateResponse = onStopGenerateResponse,
                                                onAttachImages = onAttachImages,
                                                showInlinePlusButton = isPlusButtonInline,
                                                isInstructionMode = isMessageInstructionMode,
                                                onConfirmInstruction = {
                                                    onSetMessageSystemInstruction(
                                                        textFieldValue.text
                                                    )
                                                },
                                                onCancelInstruction = { onSetInstructionMode(false) }
                                            )
                                        }
                                    }
                                }
                                BaseItemAlphaAnimation(isVisible = isEnableSearchMessageMode) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            imageVector = IconsManager.Search,
                                            contentDescription = null
                                        )
                                        Text(selectedMessageNumber.toString())
                                        Text(stringResource(Res.string.out_of))
                                        Text(searchMessageCount.toString())
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        Layout(
            content = {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = menuAnimationProgress
                        }
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(color = colors.surfaceHigh)
                        .let {
                            if (colors.strokeSubtle == Color.Transparent) it
                            else it.topBorder(
                                color = colors.strokeSubtle,
                                width = 1.dp,
                                cornerRadius = 16.dp
                            )
                        }
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    when (activeBottomBarMode) {
                        BottomBarMode.MainBottomSheetMenuMode -> {
                            // Отрисовываем, только если анимация идет или меню открыто
                            if (menuAnimationProgress > 0f) {
                                AttachmentMenu(
                                    modifier = Modifier.pointerInput(menuAnimationProgress) {
                                        // Если анимация почти закончилась, блокируем клики
                                        if (menuAnimationProgress < 0.1f) return@pointerInput
                                    },
                                    onAddSystemInstruction = { onSetInstructionMode(true) },
                                    onOpenGiftsMagazine = { onOpenGiftsMagazine() }
                                )
                            }
                        }

                        BottomBarMode.GiftBottomSheetMode -> {
                            if (menuAnimationProgress > 0f) {
                                GiftPickerLayout(
                                    myGiftsState = myGiftsState,
                                    onGiftClick = {
                                        // Дополнительная защита: клик срабатывает только если меню полностью видно
                                        if (menuAnimationProgress > 0.9f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                            onSendGift(it)
                                        }
                                    },
                                    modifier = Modifier,
                                    onLoadMore = {}
                                )
                            }
                        }

                        else -> {
                            Box(Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { measurables, constraints ->
            // Измеряем меню, чтобы узнать его реальную высоту
            val placeable = measurables.first().measure(constraints)
            val menuFullHeight = placeable.height

            // Фиксированный отступ между полем ввода и низом (8.dp)
            val baseGapPx = 8.dp.roundToPx()

            // Высота меню в данный момент анимации
            // Если сработал snap(), то menuAnimationProgress=1f, и мы получаем полную высоту сразу
            val currentMenuHeight = (menuFullHeight * menuAnimationProgress).roundToInt()

            // Высота клавиатуры (или навбара, если клавиатуры нет)
            val currentImeOrNavHeight = max(imeHeightPx, animatedNavBarPaddingPx.roundToInt())

            // ГЛАВНОЕ: Итоговая высота это МАКСИМУМ из (Клавиатура, Меню).
            // Это решает проблему "спотыкания":
            // 1. Клавиатура (500px) начинает закрываться.
            // 2. Меню (300px) уже готово (snap=1f).
            // 3. LayoutHeight = max(490, 300) = 490 -> max(400, 300) = 400 -> max(200, 300) = 300.
            // Поле ввода плавно едет на "лифте" клавиатуры, пока не сядет на "пол" меню.
            val layoutHeight = baseGapPx + max(currentImeOrNavHeight, currentMenuHeight)

            val layoutWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else placeable.width
            layout(layoutWidth, layoutHeight) {
                // Меню всегда рисуем в самом низу этого пространства.
                // Если пространство больше меню (клавиатура выше), меню будет визуально "приклеено" к низу экрана.
                val yPosition = layoutHeight - menuFullHeight
                placeable.place(0, yPosition)
            }
        }
    }
}

fun Modifier.topBorder(
    color: Color,
    width: Dp,
    cornerRadius: Dp
): Modifier = this.drawWithContent {
    drawContent()

    val strokeWidthPx = width.toPx()
    val radiusPx = cornerRadius.toPx()

    val path = Path().apply {
        moveTo(0f, radiusPx)

        arcTo(
            rect = Rect(
                left = 0f,
                top = 0f,
                right = radiusPx * 2,
                bottom = radiusPx * 2
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        lineTo(size.width - radiusPx, 0f)

        arcTo(
            rect = Rect(
                left = size.width - (radiusPx * 2),
                top = 0f,
                right = size.width,
                bottom = radiusPx * 2
            ),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidthPx,
            cap = StrokeCap.Round
        )
    )
}
