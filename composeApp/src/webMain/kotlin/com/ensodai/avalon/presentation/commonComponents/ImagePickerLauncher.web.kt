package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.runtime.Composable
import com.ensodai.avalon.data.db.dto.CharacterImageDTO

@Composable
actual fun ImagePickerLauncher(
    onAttachImages: (List<CharacterImageDTO>) -> Unit,
    maxItems: Int,
    attachedImagesSize: Int,
    onShowPermissionDialog: () -> Unit,
    onStartAttachingFiles: () -> Unit,
    content: @Composable (launcher: () -> Unit) -> Unit
) {
    content {}
}
