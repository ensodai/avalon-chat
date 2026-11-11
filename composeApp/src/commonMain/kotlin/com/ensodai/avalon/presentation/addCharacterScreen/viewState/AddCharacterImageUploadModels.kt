package com.ensodai.avalon.presentation.addCharacterScreen.viewState

import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto

data class UploadedCharacterImageRef(
    val image: CharacterImageDTO,
    val uploadToken: String
)

data class UploadedWelcomeCardImageRef(
    val image: WelcomeCardImageDto,
    val uploadToken: String
)

data class PendingImageUpload(
    val id: String,
    val fileName: String
)
