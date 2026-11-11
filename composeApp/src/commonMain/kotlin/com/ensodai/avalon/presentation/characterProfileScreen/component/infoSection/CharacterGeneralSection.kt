package com.ensodai.avalon.presentation.characterProfileScreen.component.infoSection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.character_name
import com.ensodai.avalon.generated.resources.profile_images
import com.ensodai.avalon.presentation.commonComponents.BaseItemScaleUpToDownAnimation
import com.ensodai.avalon.presentation.commonComponents.ImagePickerLauncher
import com.ensodai.avalon.presentation.commonComponents.characterImagesLazy.CharacterImagesLazyRow
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun CharacterGeneralSection(
    isEditProfileMode: Boolean,
    currentAvatar: CharacterImageDTO?,
    characterAvatars: List<CharacterImageDTO>?,
    characterName: String,
    isErrorInputName: Boolean,
    onChangeCharacterName: (String) -> Unit,
    onAttachImages: (List<CharacterImageDTO>) -> Unit,
    onStartAttachingFiles: () -> Unit,
    onSelectImage: (CharacterImageDTO) -> Unit,
    onDeleteImage: (CharacterImageDTO) -> Unit,
    onShowPermissionDialog: () -> Unit
) {
    val colors = AvalonTheme.colors
    BaseItemScaleUpToDownAnimation(isEditProfileMode) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)

            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(Res.string.profile_images),
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
//                color = GemMainOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                ImagePickerLauncher(
                    attachedImagesSize = characterAvatars?.size ?: 0,
                    onAttachImages = { onAttachImages(it) },
                    onStartAttachingFiles = { onStartAttachingFiles() },
                    onShowPermissionDialog = { onShowPermissionDialog() }
                ) { launchImagePicker ->
                    CharacterImagesLazyRow(
                        currentAvatar = currentAvatar,
                        characterImages = characterAvatars,
                        contentPadding = 16,
                        onAddImage = { launchImagePicker() },
                        onSelectImage = { onSelectImage(it) },
                        onDeleteImage = { onDeleteImage(it) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                    text = stringResource(Res.string.character_name),
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
//                color = GemMainOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = characterName,
                    onValueChange = { newValue ->
                        onChangeCharacterName(newValue)
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .imePadding(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = colors.neonAccent,
                        focusedIndicatorColor = colors.strokeActive,
                        unfocusedIndicatorColor = colors.strokeActive,
                        errorIndicatorColor = colors.error,
                        errorContainerColor = Color.Transparent,
                        errorCursorColor = colors.neonAccent,
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    isError = isErrorInputName
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 16.dp),
                    text = "${characterName.length}/60",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
