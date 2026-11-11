package com.ensodai.avalon.presentation.characterProfileScreen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.chat.models.ImagePayload
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.chat_media
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.ImageFullscreenDialog
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
fun SharedMediaSection(
    sharedMedia: List<String>,
    onImageClick: (String) -> Unit = {}
) {
    if (sharedMedia.isEmpty()) return

    val colors = AvalonTheme.colors
    var activeFullScreenImageIndex by remember { mutableStateOf<Int?>(null) }

    GemCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        enabled = false, // Нам не нужен клик на всю карточку
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = IconsManager.Image,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(Res.string.chat_media).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val chunkedMedia = remember(sharedMedia) { sharedMedia.chunked(3) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunkedMedia.forEach { rowMedia ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowMedia.forEach { url ->
                            ShimmerAsyncImage(
                                model = url,
                                seed = url,
                                blurhash = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        activeFullScreenImageIndex = sharedMedia.indexOf(url)
                                        onImageClick(url)
                                    }
                            )
                        }

                        repeat(3 - rowMedia.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    activeFullScreenImageIndex?.let { index ->
        ImageFullscreenDialog(
            imagePayloads = sharedMedia.map { url ->
                ImagePayload(
                    id = Uuid.random(),
                    url = url,
                    width = 0,
                    height = 0
                )
            },
            initialPageIndex = index,
            onDismissRequest = { activeFullScreenImageIndex = null }
        )
    }
}
