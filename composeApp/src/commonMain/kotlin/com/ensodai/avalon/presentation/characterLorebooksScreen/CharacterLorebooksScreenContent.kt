package com.ensodai.avalon.presentation.characterLorebooksScreen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.character_lorebooks_attach_from_library
import com.ensodai.avalon.generated.resources.character_lorebooks_create_new
import com.ensodai.avalon.generated.resources.character_lorebooks_empty
import com.ensodai.avalon.generated.resources.character_lorebooks_subtitle
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.characterLorebooksScreen.action.CharacterLorebooksScreenActions
import com.ensodai.avalon.presentation.characterLorebooksScreen.component.AttachLorebookBottomSheet
import com.ensodai.avalon.presentation.characterLorebooksScreen.component.CharacterLorebookItemCard
import com.ensodai.avalon.presentation.characterLorebooksScreen.component.CharacterLorebooksTopBar
import com.ensodai.avalon.presentation.characterLorebooksScreen.component.CreateLorebookDialog
import com.ensodai.avalon.presentation.characterLorebooksScreen.viewState.CharacterLorebooksScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun CharacterLorebooksScreenContent(
    viewState: CharacterLorebooksScreenViewState,
    actions: CharacterLorebooksScreenActions,
    navigation: Navigation,
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val colors = AvalonTheme.colors

    val isBusy = viewState.isLoading || viewState.isCreatingBook

    AvalonBackHandler(isBusy) {
        if (!isBusy) {
            navigation.navigateBack()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            CharacterLorebooksTopBar(
                modifier = Modifier.fillMaxWidth(),
                onBackButtonClick = { navigation.navigateBack() }
            )
        }
    ) { innerPadding ->
        if (viewState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.neonAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 16.dp + innerPadding.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 24.dp + innerPadding.calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Global Error Message
                if (viewState.errorMessage != null) {
                    item(key = "character_lorebooks_error") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.error.copy(alpha = 0.15f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = viewState.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.error
                            )
                        }
                    }
                }

                // Subtitle description
                item(key = "character_lorebooks_subtitle") {
                    Text(
                        text = stringResource(Res.string.character_lorebooks_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }

                // Action buttons row: From Library & New Book
                item(key = "character_lorebooks_actions_row") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HeaderActionButton(
                            text = stringResource(Res.string.character_lorebooks_attach_from_library),
                            icon = IconsManager.SystemInstruction,
                            isPrimary = false,
                            onClick = actions.onOpenAttachSheet,
                            modifier = Modifier.weight(1f)
                        )
                        HeaderActionButton(
                            text = stringResource(Res.string.character_lorebooks_create_new),
                            icon = IconsManager.Add,
                            isPrimary = true,
                            onClick = actions.onOpenCreateDialog,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Empty state
                if (viewState.bindings.isEmpty()) {
                    item(key = "character_lorebooks_empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.character_lorebooks_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Bindings list
                items(
                    items = viewState.bindings,
                    key = { it.lorebookId.toString() }
                ) { binding ->
                    CharacterLorebookItemCard(
                        binding = binding,
                        onToggleEnabled = actions.onToggleBindingEnabled,
                        onSetPrimary = actions.onSetPrimary,
                        onDetach = actions.onDetachClick,
                        onOpenDetails = { lorebookId ->
                            navigation.navigateToEditLorebookScreen(
                                lorebookId = lorebookId
                            )
                        }
                    )
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Attach from library bottom sheet
    AttachLorebookBottomSheet(
        isVisible = viewState.isAttachSheetVisible,
        onDismissRequest = actions.onCloseAttachSheet,
        library = viewState.userLibrary,
        attachedLorebookIds = viewState.bindings.map { it.lorebookId }.toSet(),
        isLoading = viewState.isLibraryLoading,
        onAttach = actions.onAttachFromLibrary,
        onImportLorebook = actions.onImportLorebook
    )

    // Create & attach dialog
    CreateLorebookDialog(
        isVisible = viewState.isCreateDialogVisible,
        onDismissRequest = actions.onCloseCreateDialog,
        name = viewState.newBookName,
        description = viewState.newBookDescription,
        isPublic = viewState.newBookIsPublic,
        isLoading = viewState.isCreatingBook,
        onNameChanged = actions.onNewBookNameChanged,
        onDescriptionChanged = actions.onNewBookDescriptionChanged,
        onIsPublicChanged = actions.onNewBookIsPublicChanged,
        onCreateAndAttach = actions.onCreateAndAttachBook
    )
}

@Composable
private fun HeaderActionButton(
    text: String,
    icon: ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "header_btn_scale",
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val bgColor = Color.Transparent
    val borderColor = if (isPrimary) colors.textMain else colors.strokeActive
    val contentColor = if (isPrimary) colors.textMain else colors.textSecondary

    Box(
        modifier = modifier
            .height(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = contentColor),
                onClick = onClick
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
