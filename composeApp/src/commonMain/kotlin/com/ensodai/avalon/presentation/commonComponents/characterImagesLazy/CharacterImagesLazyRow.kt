package com.ensodai.avalon.presentation.commonComponents.characterImagesLazy

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.data.db.dto.CharacterImageDTO

@Composable
fun CharacterImagesLazyRow(
    modifier: Modifier = Modifier,
    currentAvatar: CharacterImageDTO?,
    characterImages: List<CharacterImageDTO>?,
    maxImageInRow: Int = 10,
    contentPadding: Int = 0,
    onAddImage: () -> Unit,
    onSelectImage: (CharacterImageDTO) -> Unit,
    onDeleteImage: (CharacterImageDTO) -> Unit
) {
    LazyRow(
        modifier = modifier.height(120.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = contentPadding.dp)
    ) {

        val imageInMaxSize = characterImages == null || characterImages.size < maxImageInRow

        if (imageInMaxSize) {
            item {
                SmallDashedCircleBox(
                    modifier = Modifier
                        .animateItem()
                        .clip(CircleShape)
                        .size(80.dp),
                    onAddImageClick = { onAddImage() }
                )
                Spacer(modifier = Modifier.padding(8.dp))
            }
        }


        if (characterImages != null) {
            items(
                items = characterImages,
                key = { it.imagePublicId }
            ) { image ->
                AvatarWithBorder(
                    modifier = Modifier.animateItem(),
                    image = image.url,
                    isSelected = image == currentAvatar,
                    onSelectImage = { onSelectImage(image) },
                    onDeleteImage = { onDeleteImage(image) }
                )
                Spacer(modifier = Modifier.padding(8.dp))
            }
        }
    }
}

