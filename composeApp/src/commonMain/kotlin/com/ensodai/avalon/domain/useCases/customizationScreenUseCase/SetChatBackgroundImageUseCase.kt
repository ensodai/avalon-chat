package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.avalon.domain.models.ChatBackgroundImage
import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class SetChatBackgroundImageUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke(url: String?, blur: Int = 0) {
        appSettingsRepository.updateChatBackground(url = url, blur = blur)
    }

    suspend fun updateBlur(url: String?, blur: Int) {
        appSettingsRepository.updateChatBackground(url = url, blur = blur)
    }

    fun getImages(): Flow<List<ChatBackgroundImage>> {
        return appSettingsRepository.getChatBackgroundImagesFlow()
    }

    suspend fun addImage(id: Uuid, url: String) {
        appSettingsRepository.addChatBackgroundImage(id, url)
    }

    suspend fun removeImage(id: Uuid) {
        appSettingsRepository.removeChatBackgroundImage(id)
    }

    suspend fun reset() {
        appSettingsRepository.resetChatBackground()
    }
}
