package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.runtime.Composable
import com.ensodai.avalon.data.db.dto.CharacterImageDTO

@Composable
expect fun ImagePickerLauncher(
    onAttachImages: (List<CharacterImageDTO>) -> Unit,
    maxItems: Int = 10,
    attachedImagesSize: Int,
    onShowPermissionDialog: () -> Unit,
    onStartAttachingFiles: () -> Unit,
    content: @Composable (launcher: () -> Unit) -> Unit
)
