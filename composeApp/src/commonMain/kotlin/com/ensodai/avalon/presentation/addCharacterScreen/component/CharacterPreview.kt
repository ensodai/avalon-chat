package com.ensodai.avalon.presentation.addCharacterScreen.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.TextPayload
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.advanced_settings
import com.ensodai.avalon.generated.resources.description
import com.ensodai.avalon.generated.resources.full_description_private
import com.ensodai.avalon.generated.resources.full_description_stage
import com.ensodai.avalon.generated.resources.scenario_stage
import com.ensodai.avalon.generated.resources.mes_example_stage
import com.ensodai.avalon.presentation.commonComponents.drawHexagonPath
import com.ensodai.avalon.presentation.characterProfileScreen.component.PremiumExpandableText
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.MessageBubble
import com.ensodai.avalon.presentation.chatScreen.component.topBar.ChatImage
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.avalon.domain.models.MessageContentModel
import com.ensodai.avalon.utils.getUTCTimeNow
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageContainerUiState
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageBodyUiItem
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageActionsConfigUiState
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import com.ensodai.avalon.domain.models.ChatMessageModel
import com.ensodai.avalon.domain.models.chat.RpgMessageBlock
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.MessageStatus
import com.ensodai.avalon.domain.audio.TtsPlayerState
import kotlin.uuid.Uuid
import com.ensodai.avalon.domain.models.MessageContainerModel
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.SegmentedProgressBar
import com.ensodai.avalon.presentation.commonComponents.welcomCard.CardColor
import com.ensodai.avalon.presentation.commonComponents.welcomCard.WelcomeCard
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.avalon.generated.resources.interaction_format_setting_title_short
import com.ensodai.avalon.generated.resources.interaction_format_literary_rpg_title
import com.ensodai.avalon.generated.resources.interaction_format_chat_simulator_title

@Composable
fun CharacterPreview(
    avatar: CharacterImageDTO?,
    isAvatarStagesScreen: Boolean,
    characterImages: List<CharacterImageDTO>,
    characterName: String,
    shortDescription: String?,
    fullDescription: String?,
    scenario: String? = null,
    mesExample: String? = null,
    isPublicFullDescription: Boolean,
    firstMessage: String?,
    welcomeCardDescription: String?,
    welcomeCardImages: List<WelcomeCardImageDto>,
    isEnableLight: Boolean,
    welcomeCardStartColor: CardColor,
    welcomeCardEndColor: CardColor,
    isShowAdvancedBlock: Boolean,
    onClickAdvancedBlock: () -> Unit,
    selectedVoice: VoiceModel = VoiceModel.ZEPHYR,
    isShowVoiceSection: Boolean = false,
    onVoiceClick: () -> Unit = {},
    selectedInteractionFormat: InteractionFormat = InteractionFormat.LITERARY_RPG,
    onInteractionFormatClick: () -> Unit = {}
) {


    Column {
        Spacer(modifier = Modifier.height(8.dp))
        GemCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box {
                if (isAvatarStagesScreen) {
                    if (avatar != null) {
                        AsyncImage(
                            model = avatar.url,
                            contentDescription = "Character Avatar",
                            modifier = Modifier
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            HexagonAvatarPlaceholder(size = 112.dp)
                        }
                    }
                } else {
                    if (characterImages.isNotEmpty()) {
                        Carousel(items = characterImages)
                    } else {
                        Box(modifier = Modifier.padding(16.dp)) {
                            SmallImagePreview(characterName = characterName)
                        }

                    }
                }

                if (characterImages.isNotEmpty()) {
                    BottomTint(modifier = Modifier.align(Alignment.BottomCenter))
                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        text = characterName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                }
            }
        }

        if (!shortDescription.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            GemCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = IconsManager.Info,
                            contentDescription = null,
                            tint = AvalonTheme.colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(Res.string.description).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = AvalonTheme.colors.textSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = shortDescription,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AvalonTheme.colors.textMain.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (!fullDescription.isNullOrBlank()) {
            var isExpanded by remember { mutableStateOf(false) }

            val rotationState by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                label = "ArrowRotation",
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
            GemCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = if (isPublicFullDescription) {
                    { isExpanded = !isExpanded }
                } else null,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPublicFullDescription) IconsManager.Description else IconsManager.PrivacyTip,
                            contentDescription = null,
                            tint = AvalonTheme.colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = stringResource(Res.string.full_description_stage).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = AvalonTheme.colors.textSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f)
                        )

                        if (isPublicFullDescription) {
                            Icon(
                                modifier = Modifier
                                    .rotate(rotationState)
                                    .size(24.dp),
                                imageVector = IconsManager.ExpandMore,
                                contentDescription = null,
                                tint = AvalonTheme.colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isPublicFullDescription) {
                        PremiumExpandableText(
                            text = fullDescription,
                            isExpanded = isExpanded,
                            collapsedMaxLines = 4
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.full_description_private),
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            color = AvalonTheme.colors.textSecondary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        if (!scenario.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            var isScenarioExpanded by remember { mutableStateOf(false) }

            val scenarioRotationState by animateFloatAsState(
                targetValue = if (isScenarioExpanded) 180f else 0f,
                label = "ScenarioArrowRotation",
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
            GemCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = { isScenarioExpanded = !isScenarioExpanded },
                contentPadding = PaddingValues(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = IconsManager.Explore,
                            contentDescription = null,
                            tint = AvalonTheme.colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = stringResource(Res.string.scenario_stage).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = AvalonTheme.colors.textSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            modifier = Modifier
                                .rotate(scenarioRotationState)
                                .size(24.dp),
                            imageVector = IconsManager.ExpandMore,
                            contentDescription = null,
                            tint = AvalonTheme.colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PremiumExpandableText(
                        text = scenario,
                        isExpanded = isScenarioExpanded,
                        collapsedMaxLines = 4
                    )
                }
            }
        }

        if (!mesExample.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            var isMesExampleExpanded by remember { mutableStateOf(false) }

            val mesExampleRotationState by animateFloatAsState(
                targetValue = if (isMesExampleExpanded) 180f else 0f,
                label = "MesExampleArrowRotation",
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
            GemCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = { isMesExampleExpanded = !isMesExampleExpanded },
                contentPadding = PaddingValues(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = IconsManager.Chat,
                            contentDescription = null,
                            tint = AvalonTheme.colors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = stringResource(Res.string.mes_example_stage).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = AvalonTheme.colors.textSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            modifier = Modifier
                                .rotate(mesExampleRotationState)
                                .size(24.dp),
                            imageVector = IconsManager.ExpandMore,
                            contentDescription = null,
                            tint = AvalonTheme.colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PremiumExpandableText(
                        text = mesExample,
                        isExpanded = isMesExampleExpanded,
                        collapsedMaxLines = 4
                    )
                }
            }
        }

        if (!welcomeCardDescription.isNullOrEmpty() || welcomeCardImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            WelcomeCard(
                modifier = Modifier.zIndex(1f),
                text = welcomeCardDescription,
                images = welcomeCardImages,
                isEnableLight = isEnableLight,
                startColor = welcomeCardStartColor,
                endColor = welcomeCardEndColor
            )
        }

        if (!firstMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                if (avatar != null) {
                    ChatImage(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clip(CircleShape)
                            .size(44.dp),
                        chatImage = avatar.url
                    )
                } else {
                    HexagonAvatarPlaceholder(
                        modifier = Modifier.padding(start = 16.dp),
                        size = 44.dp
                    )
                }
                val dummySlot = remember(firstMessage) {
                    val containerId = Uuid.random()
                    val slotId = Uuid.random()
                    val container = MessageContainerModel(
                        id = containerId,
                        slotId = slotId,
                        rarity = Rarity.COMMON,
                        role = MessageRole.MODEL,
                        status = MessageStatus.SENT,
                        createdAt = getUTCTimeNow(),
                        userReaction = null,
                        characterReaction = null,
                        isRead = true,
                        content = listOf(
                            MessageContentModel(
                                id = Uuid.random(),
                                type = AvalonMessageContentType.TEXT,
                                payload = TextPayload(text = firstMessage)
                            )
                        )
                    )
                    ChatMessageModel(
                        slotId = slotId,
                        createdAt = container.createdAt,
                        containers = listOf(container),
                        activeContainerId = container.id
                    )
                }
                val uiState = remember(firstMessage) {
                    val id = Uuid.random()
                    MessageContainerUiState(
                        id = id,
                        role = MessageRole.MODEL,
                        rarity = Rarity.COMMON,
                        status = MessageStatus.SENT,
                        isRead = true,
                        bodyItems = listOf(
                            MessageBodyUiItem.Text(
                                textId = id,
                                rawText = firstMessage,
                                parsedBlocks = listOf(RpgMessageBlock.Plain(firstMessage)),
                                isStreaming = false
                            )
                        ),
                        userReaction = null,
                        characterReaction = null,
                        showStatus = false,
                        actionsConfig = MessageActionsConfigUiState.None,
                        showExternalPlayButton = false,
                        isTtsLoading = false,
                        rawText = firstMessage,
                        systemInstructionText = null,
                        isStreaming = false
                    )
                }
                MessageBubble(
                    uiState = uiState,
                    preset = ChatRoleplayPreset.STANDARD_BUBBLE,
                    modifier = Modifier.fillMaxWidth(),
                    selectedMessagesText = null,
                    isSelected = false,
                    isEnableSelectedMessagesMode = false,
                    isEnableEditMessageMode = false,
                    shouldShowAnimatedText = false,
                    highlightQuery = "",
                    isEnableSelectMessage = false,
                    isDefaultFocusRequester = false,
                    userReaction = null,
                    characterReaction = null,
                    userAvatarUrl = null,
                    characterAvatarUrl = null,
                    showReactionPicker = false,
                    reactionPickerOffset = null,
                    onShowReactionMenu = {},
                    onDismissReactionMenu = {},
                    onToggleReaction = {},
                    onEditMessageText = {},
                    onSelectMessage = {},
                    onEnableSelectedMessagesMode = {},
                    onComplaintIconClick = {},
                    onOpenSettingsScreen = {},
                    onActionButtonClick = {},
                    currentPlayingContentId = null,
                    ttsPlayerState = TtsPlayerState.Idle,
                    voice = null,
                    onPlaySpeech = { _, _ -> },
                    onPauseSpeech = {},
                    slot = dummySlot
                )
            }
        }
        if (isShowAdvancedBlock) {
            GemCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = onClickAdvancedBlock,
                enabled = true,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = IconsManager.Tune,
                        contentDescription = null,
                        tint = AvalonTheme.colors.textMain,
                        modifier = Modifier
                            .size(26.dp)
                            .alpha(0.9f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.advanced_settings),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = AvalonTheme.colors.textMain
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = IconsManager.Add,
                        contentDescription = "Add",
                        tint = AvalonTheme.colors.strokeSubtle,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (isShowVoiceSection) {
            Spacer(modifier = Modifier.height(16.dp))
            GemCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = onVoiceClick,
                enabled = true,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedVoice.emoji,
                        fontSize = 24.sp,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Голос персонажа",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AvalonTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${selectedVoice.modelName} (${selectedVoice.description})",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = AvalonTheme.colors.textMain
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = IconsManager.KeyboardArrowRight,
                        contentDescription = "Select voice",
                        tint = AvalonTheme.colors.strokeSubtle,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GemCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = onInteractionFormatClick,
                enabled = true,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = IconsManager.Chat,
                        contentDescription = null,
                        tint = AvalonTheme.colors.textMain,
                        modifier = Modifier
                            .size(24.dp)
                            .alpha(0.9f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.interaction_format_setting_title_short),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AvalonTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val formatTitleRes = when (selectedInteractionFormat) {
                            InteractionFormat.LITERARY_RPG -> Res.string.interaction_format_literary_rpg_title
                            InteractionFormat.CHAT_SIMULATOR -> Res.string.interaction_format_chat_simulator_title
                        }
                        Text(
                            text = stringResource(formatTitleRes),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = AvalonTheme.colors.textMain
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = IconsManager.KeyboardArrowRight,
                        contentDescription = "Select interaction format",
                        tint = AvalonTheme.colors.strokeSubtle,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GemCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = IconsManager.AutoMode,
                        contentDescription = null,
                        tint = AvalonTheme.colors.neonAccent,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Сюжетные события (Curiosity)",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = AvalonTheme.colors.textMain
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Вы сможете настроить уникальные сюжетные события и адаптацию характера вашего персонажа сразу после его создания!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AvalonTheme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun Carousel(items: List<CharacterImageDTO>) {

    val pagerState = rememberPagerState(pageCount = { items.size })

    Box {
        HorizontalPager(
            state = pagerState
        ) { page ->
            val item = items[page]
            AsyncImage(
                model = item.url,
                contentDescription = "Character Avatar",
                modifier = Modifier
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
        }
        if (items.size > 1) {
            SegmentedProgressBar(
                modifier = Modifier.padding(8.dp),
                totalSteps = items.size,
                currentStep = pagerState.currentPage + 1,
                segmentHeight = 4.dp
            )
        }
    }
}

@Composable
private fun BottomTint(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
    )
}

@Composable
private fun SmallImagePreview(characterName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HexagonAvatarPlaceholder(size = 44.dp)
        Text(
            modifier = Modifier
                .padding(start = 16.dp),
            text = characterName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun HexagonAvatarPlaceholder(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawHexagonPath(
                color = colors.textMain.copy(alpha = 0.8f),
                style = Stroke(width = (size / 16).toPx().coerceAtLeast(2.dp.toPx()))
            )
        }
        Text(
            text = "A",
            color = colors.textMain.copy(alpha = 0.8f),
            fontWeight = FontWeight.Light,
            fontSize = (size.value * 0.38f).sp
        )
    }
}
