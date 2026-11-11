package com.ensodai.avalon.presentation.editCharacterTextScreen.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.full_description_stage
import com.ensodai.avalon.generated.resources.mes_example_stage
import com.ensodai.avalon.generated.resources.scenario_stage
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.component.PrivacyFullDescriptionDropDownMenu
import com.ensodai.avalon.presentation.editCharacterTextScreen.model.CharacterTextType
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarEditCharacterTextScreen(
    modifier: Modifier = Modifier,
    textType: CharacterTextType,
    characterVisibility: CharacterVisibility,
    isLoading: Boolean,
    onBackButtonClick: () -> Unit,
    onDoneButtonClick: () -> Unit,
    onChangeCharacterVisibility: (CharacterVisibility) -> Unit
) {
    val colors = AvalonTheme.colors

    val titleText = when (textType) {
        CharacterTextType.PERSONALITY -> stringResource(Res.string.full_description_stage)
        CharacterTextType.SCENARIO -> stringResource(Res.string.scenario_stage)
        CharacterTextType.MES_EXAMPLE -> stringResource(Res.string.mes_example_stage)
    }

    GemIslandTopBar(
        modifier = modifier,
        title = {
            Text(text = titleText)
        },
        navigationIcon = {
            NavigationBackButton(onBackButtonClick = onBackButtonClick)
        },
        actions = {
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.8f))
                        .togetherWith(fadeOut() + scaleOut(targetScale = 0.8f))
                },
                label = "HeaderActionsAnimation"
            ) { loading ->
                if (loading) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colors.textSecondary,
                            strokeWidth = 2.5.dp,
                            trackColor = colors.textSecondary.copy(alpha = 0.3f)
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (textType == CharacterTextType.PERSONALITY) {
                            PrivacyFullDescriptionDropDownMenu(
                                modifier = Modifier,
                                characterVisibility = characterVisibility,
                                onChangeCharacterVisibility = onChangeCharacterVisibility
                            )
                        }

                        IconButton(onClick = onDoneButtonClick) {
                            Icon(
                                tint = colors.textMain,
                                imageVector = IconsManager.Done,
                                contentDescription = "Done"
                            )
                        }
                    }
                }
            }
        }
    )
}
