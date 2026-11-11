package com.ensodai.avalon.presentation.commonComponents

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.utils.IODispatcher
import com.ensodai.avalon.utils.LOCAL_IMAGE_PATH
import com.ensodai.avalon.utils.saveImageToInternalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@Composable
actual fun ImagePickerLauncher(
    onAttachImages: (List<CharacterImageDTO>) -> Unit,
    maxItems: Int,
    attachedImagesSize: Int,
    onShowPermissionDialog: () -> Unit,
    onStartAttachingFiles: () -> Unit,
    content: @Composable (launcher: () -> Unit) -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun hasRequiredPermissions(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                        ) == PackageManager.PERMISSION_GRANTED)

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED

            else ->
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getPermissionsToRequest(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)

            else ->
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun checkPermissionsResult(permissions: Map<String, Boolean>): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.values.any { it }
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        }
    }

    val remainingSlots = maxItems - attachedImagesSize
    val useMultiplePicker = remainingSlots > 1

    @Suppress("UNCHECKED_CAST")
    val pickerResultHandler: (Any?) -> Unit = { result ->
        if (useMultiplePicker) {
            val uris = result as? List<Uri> ?: emptyList()
            if (uris.isNotEmpty()) {
                scope.launch(IODispatcher) {
                    onStartAttachingFiles()
                    val imageDTOs = mutableListOf<CharacterImageDTO>()
                    uris.forEach { uri ->
                        try {
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                val imageBytes = inputStream.readBytes()
                                if (imageBytes.isNotEmpty()) {
                                    val fileName =
                                        "$LOCAL_IMAGE_PATH${System.currentTimeMillis()}_${uri.lastPathSegment ?: "image"}"
                                    val imagePath = saveImageToInternalStorage(imageBytes, fileName)
                                    if (imagePath != null) {
                                        imageDTOs.add(
                                            CharacterImageDTO(
                                                imagePublicId = Uuid.random(),
                                                url = imagePath
                                            )
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            println("Ошибка при обработке URI $uri: ${e.message}")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        if (imageDTOs.isNotEmpty()) {
                            onAttachImages(imageDTOs)
                        }
                    }
                }
            }
        } else {
            val uri = result as? Uri
            if (uri != null) {
                scope.launch(IODispatcher) {
                    onStartAttachingFiles()
                    try {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val imageBytes = inputStream.readBytes()
                            if (imageBytes.isNotEmpty()) {
                                val fileName =
                                    "$LOCAL_IMAGE_PATH${System.currentTimeMillis()}_${uri.lastPathSegment ?: "image"}"
                                val imagePath = saveImageToInternalStorage(imageBytes, fileName)
                                if (imagePath != null) {
                                    val imageDTO = CharacterImageDTO(
                                        imagePublicId = Uuid.random(),
                                        url = imagePath
                                    )
                                    withContext(Dispatchers.Main) {
                                        onAttachImages(listOf(imageDTO))
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Ошибка при обработке URI $uri: ${e.message}")
                    }
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = if (useMultiplePicker) {
            ActivityResultContracts.PickMultipleVisualMedia(remainingSlots)
        } else {
            ActivityResultContracts.PickVisualMedia()
        },
        onResult = pickerResultHandler
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        val granted = checkPermissionsResult(permissions)
        if (granted) {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            onShowPermissionDialog()
        }
    }

    val launchPickerAction = remember(maxItems, attachedImagesSize) {
        {
            if (maxItems - attachedImagesSize < 1) {
                println("Лимит изображений достигнут. Пикер не будет запущен.")
                return@remember
            }

            if (hasRequiredPermissions()) {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                permissionLauncher.launch(getPermissionsToRequest())
            }
        }
    }

    content(launchPickerAction)
}
