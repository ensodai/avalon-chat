package com.ensodai.avalon.presentation.characterProfileScreen.component.fullDescriptionSection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.promnt_instruction_explain
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.WelcomeCardState
import com.ensodai.avalon.presentation.commonComponents.GemTemperatureSlider
import com.ensodai.avalon.presentation.commonComponents.SafetySettingsColumnSliders
import com.ensodai.avalon.presentation.commonComponents.privacyCards.PrivacyCardColumn
import com.ensodai.avalon.presentation.commonComponents.privacyCards.PrivacyFullDescriptionCardRow
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditDescriptionSection(
    editSettingsList: List<ProfileSettings>,
    avatar: String?,
    fullDescription: String,
    pagerState: PagerState,
    temperatureSliderPosition: Float,
    currentPrivacySetting: CharacterVisibility,
    harassment: Int,
    hate: Int,
    sexuallyExplicit: Int,
    dangerousContent: Int,
    firstMessage: String?,
    welcomeCardState: WelcomeCardState?,
    fullDescriptionPrivacy: CharacterVisibility,
    onChangeFullDescriptionPrivacy: (CharacterVisibility) -> Unit,
    onChangeHarassment: (Int) -> Unit,
    onChangeHate: (Int) -> Unit,
    onChangeSexuallyExplicit: (Int) -> Unit,
    onChangeDangerousContent: (Int) -> Unit,
    onChangeFullDescription: (String) -> Unit,
    onChangePrivacy: (CharacterVisibility) -> Unit,
    onChangeTemperature: (Float) -> Unit,
    onChangeFirstMessage: (String) -> Unit
) {
    val colors = AvalonTheme.colors

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .background(color = colors.background),
        verticalAlignment = Alignment.Top
    ) { page ->
        editSettingsList.getOrNull(page)?.let { setting ->
            val scrolling by remember(pagerState) {
                derivedStateOf {
                    pagerState.currentPageOffsetFraction != 0f
                }
            }

            val heightModifier = if (pagerState.currentPage == page || scrolling) {
                Modifier.wrapContentHeight()
            } else {
                Modifier.requiredHeightIn(min = 620.dp)
            }

            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = heightModifier
                    .fillMaxWidth()
            ) {

                when (editSettingsList[page]) {
                    ProfileSettings.FullDescriptionSetting -> {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            PrivacyFullDescriptionCardRow(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                selectedOption = fullDescriptionPrivacy,
                                onOptionSelected = { onChangeFullDescriptionPrivacy(it) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = fullDescription,
                                onValueChange = { newValue ->
                                    if (newValue.length <= 21000) {
                                        onChangeFullDescription(newValue)
                                    } else {
                                        val truncatedText = newValue.take(21000)
                                        onChangeFullDescription(truncatedText)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(8.dp),
                                minLines = 8,
                                maxLines = 8,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = colors.surfaceHigh,
                                    unfocusedContainerColor = colors.surfaceHigh,
                                    cursorColor = colors.neonAccent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                placeholder = {
                                    Text(
                                        text = stringResource(Res.string.promnt_instruction_explain),
                                        color = colors.placeholderText,
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val count = fullDescription.length
                            Text(
                                text = "${count}/21000",
                                modifier = Modifier.align(Alignment.End)
                                    .padding(horizontal = 16.dp),
                                color = colors.textSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    ProfileSettings.PrivacySettings -> {
                        PrivacyCardColumn(
                            modifier = Modifier
                                .padding(16.dp),
                            selectedOption = currentPrivacySetting,
                            onOptionSelected = { onChangePrivacy(it) }
                        )
                    }

                    ProfileSettings.SafetySettings -> {
                        SafetySettingsColumnSliders(
                            modifier = Modifier
                                .padding(16.dp),
                            harassment = harassment,
                            hate = hate,
                            sexuallyExplicit = sexuallyExplicit,
                            dangerousContent = dangerousContent,
                            onChangeHarassment = { onChangeHarassment(it) },
                            onChangeHate = { onChangeHate(it) },
                            onChangeSexuallyExplicit = { onChangeSexuallyExplicit(it) },
                            onChangeDangerousContent = { onChangeDangerousContent(it) }
                        )
                    }

                    ProfileSettings.TemperatureSettings -> {
                        GemTemperatureSlider(
                            modifier = Modifier
                                .padding(16.dp),
                            sliderPosition = temperatureSliderPosition,
                            onChangeTemperature = { onChangeTemperature(it) }
                        )
                    }

                    ProfileSettings.FirstMessageSettings -> {
                        FirstMessageSection(
                            firstMessage = firstMessage,
                            onChangeFirstMessage = { onChangeFirstMessage(it) }
                        )
                    }

                    ProfileSettings.WelcomeCardSettings -> {
//                        HelloCardStageScreen(
//                            text = welcomeCardState?.welcomeCardDescription,
//                            images = welcomeCardState?.welcomeCardImages,
//                            isShowPreviewCard = !welcomeCardState?.welcomeCardDescription.isNullOrEmpty() || welcomeCardState?.welcomeCardImages?.isNotEmpty() == true,
//                            isEnableLight = welcomeCardState?.isEnableLight == true,
//                            onChangeDescription = { welcomeCardAction.onChangeDescription(it) },
//                            onAttachImages = { welcomeCardAction.onAttachImages(it) },
//                            onShowPermissionDialog = { welcomeCardAction.onShowPermissionDialog() },
//                            onDeleteImage = { welcomeCardAction.onDeleteImage(it) },
//                            startColor = welcomeCardState?.startColor ?: CardColor.DEFAULT,
//                            endColor = welcomeCardState?.endColor ?: CardColor.DEFAULT,
//                            allowedColors = welcomeCardState?.allowedColors
//                                ?: CardColor.getSelectableColors(),
//                            onSelectStartTone = { welcomeCardAction.onSelectStartTone(it) },
//                            onSelectEndTone = { welcomeCardAction.onSelectEndTone(it) },
//                            onToggleLight = { welcomeCardAction.onToggleLight() }
//                        )
                    }
                }
            }
        }
    }
}
