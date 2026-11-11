package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar.actionButtonsSection

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ensodai.shared.avalon.character.models.CharacterCardExportFormat
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.complaint_character
import com.ensodai.avalon.generated.resources.edit
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.commonComponents.ExpressiveMenuItem
import com.ensodai.avalon.presentation.commonComponents.ExpressivePopupMenu
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@Composable
fun DropDownButtonCharacterProfile(
    isEnableEditButton: Boolean,
    isEnableDropDownButton: Boolean,
    canExportCard: Boolean = false,
    onEnableEditProfileMode: () -> Unit,
    onReportCharacter: () -> Unit,
    onExportCharacterCard: (CharacterCardExportFormat) -> Unit = {}
) {
    if (isEnableDropDownButton) {
        Box {
            ExpressivePopupMenu(
                triggerContent = { onClick ->
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.testTag(BenchmarkTags.CharacterProfileMenuButton)
                    ) {
                        Icon(
                            imageVector = IconsManager.MoreVert,
                            contentDescription = "Expand menu",
                            tint = AvalonTheme.colors.textMain
                        )
                    }
                },
                menuContent = { onDismiss ->
                    if (isEnableEditButton) {
                        ExpressiveMenuItem(
                            modifier = Modifier.testTag(BenchmarkTags.CharacterProfileEditMenuItem),
                            text = stringResource(Res.string.edit),
                            icon = IconsManager.Edit,
                            onClick = {
                                onEnableEditProfileMode()
                                onDismiss()
                            }
                        )
                    }

                    if (canExportCard) {
                        ExpressiveMenuItem(
                            text = "Экспорт PNG V3 (ccv3)",
                            icon = IconsManager.Download,
                            onClick = {
                                onExportCharacterCard(CharacterCardExportFormat.PNG_V3)
                                onDismiss()
                            }
                        )

                        ExpressiveMenuItem(
                            text = "Экспорт JSON V3",
                            icon = IconsManager.Code,
                            onClick = {
                                onExportCharacterCard(CharacterCardExportFormat.JSON_V3)
                                onDismiss()
                            }
                        )
                    }

                    ExpressiveMenuItem(
                        text = stringResource(Res.string.complaint_character),
                        icon = IconsManager.Report,
                        onClick = {
                            onReportCharacter()
                            onDismiss()
                        }
                    )
                }
            )
        }
    }
}
