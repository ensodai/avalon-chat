package com.ensodai.avalon.presentation.characterProfileScreen.component.fullDescriptionSection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Box
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Shield
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.full_description_empty
import com.ensodai.avalon.generated.resources.full_description_private
import com.ensodai.avalon.generated.resources.full_description_stage
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.EditCharacterConfigState
import com.ensodai.avalon.presentation.characterProfileScreen.viewState.WelcomeCardState
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.presentation.commonComponents.BaseItemScaleUpToDownAnimation
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.utils.EMPTY_STRING
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullDescriptionSection(
    fullDescription: String?,
    isEditMode: Boolean,
    avatar: String?,
    firstMessage: String?,
    editSettingsList: List<ProfileSettings>,
    updatedCharacterProfile: EditCharacterConfigState,
    isPublicFullDescription: Boolean,
    welcomeCardState: WelcomeCardState?,
    onChangeFullDescriptionPrivacy: (CharacterVisibility) -> Unit,
    onChangeFullDescription: (String) -> Unit,
    onChangePrivacy: (CharacterVisibility) -> Unit,
    onChangeTemperature: (Float) -> Unit,
    onChangeHarassment: (Int) -> Unit,
    onChangeHate: (Int) -> Unit,
    onChangeSexuallyExplicit: (Int) -> Unit,
    onChangeDangerousContent: (Int) -> Unit,
    onChangeFirstMessage: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { editSettingsList.size })
    val colors = AvalonTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
//            .background(GemModelMessageBubble),
    ) {
        Column {
            BaseItemScaleUpToDownAnimation(isEditMode) {
                EditScrollableTabRow(
                    pagerState = pagerState,
                    items = editSettingsList
                )
            }

            Box {

                BaseItemAlphaAnimation(isVisible = !isEditMode) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.full_description_stage),
                            fontWeight = FontWeight.Bold,
                            color = colors.warningAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isPublicFullDescription) {
                            if (fullDescription == null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        modifier = Modifier
                                            .size(133.dp)
                                            .alpha(0.5f),
                                        imageVector = Lucide.Box,
                                        contentDescription = null
                                    )
                                    Row {
                                        Text(
                                            modifier = Modifier.alpha(0.5f),
                                            text = stringResource(Res.string.full_description_empty)
                                        )
                                        Text(
                                            text = "👀"
                                        )
                                    }
                                }

                            } else {
                                Text(
                                    text = fullDescription
                                )
                            }

                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .size(133.dp)
                                        .alpha(0.5f),
                                    imageVector = Lucide.Shield,
                                    contentDescription = null
                                )
                                Text(
                                    modifier = Modifier.alpha(0.5f),
                                    text = stringResource(Res.string.full_description_private)
                                )
                            }

                        }
                    }
                }

                BaseItemAlphaAnimation(isVisible = isEditMode) {
                    EditDescriptionSection(
                        editSettingsList = editSettingsList,
                        fullDescription = updatedCharacterProfile.fullDescription ?: EMPTY_STRING,
                        pagerState = pagerState,
                        avatar = avatar,
                        currentPrivacySetting = updatedCharacterProfile.characterPrivacy,
                        temperatureSliderPosition = updatedCharacterProfile.temperature,
                        harassment = updatedCharacterProfile.harassment,
                        hate = updatedCharacterProfile.hate,
                        sexuallyExplicit = updatedCharacterProfile.sexuallyExplicit,
                        dangerousContent = updatedCharacterProfile.dangerousContent,
                        fullDescriptionPrivacy = updatedCharacterProfile.fullDescriptionPrivacy,
                        firstMessage = firstMessage,
                        welcomeCardState = welcomeCardState,
                        onChangeFullDescription = { onChangeFullDescription(it) },
                        onChangePrivacy = { onChangePrivacy(it) },
                        onChangeTemperature = { onChangeTemperature(it) },
                        onChangeHarassment = { onChangeHarassment(it) },
                        onChangeHate = { onChangeHate(it) },
                        onChangeSexuallyExplicit = { onChangeSexuallyExplicit(it) },
                        onChangeDangerousContent = { onChangeDangerousContent(it) },
                        onChangeFullDescriptionPrivacy = { onChangeFullDescriptionPrivacy(it) },
                        onChangeFirstMessage = { onChangeFirstMessage(it) }
                    )
                }
            }
        }
    }
}
