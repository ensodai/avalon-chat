package com.ensodai.avalon.presentation.importCharacterScreen.viewState

import androidx.compose.runtime.Immutable
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.spec.AvalonCardCustomization
import com.ensodai.shared.avalon.character.spec.CharacterCardData
import com.ensodai.avalon.presentation.core.BaseViewState
import kotlin.uuid.Uuid

@Immutable
data class ImportCharacterScreenViewState(
    val isLoading: Boolean = false,
    val selectedFileName: String? = null,
    val fileBytes: ByteArray? = null,
    val isPngFile: Boolean = false,
    val parsedCardData: CharacterCardData? = null,
    val specVersion: String? = null,
    val customization: AvalonCardCustomization = AvalonCardCustomization(),
    val selectedVisibility: CharacterVisibility = CharacterVisibility.PRIVATE,
    val errorMessage: String? = null,
    val isImportSuccess: Boolean = false,
    val importedCharacterId: Uuid? = null
) : BaseViewState() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ImportCharacterScreenViewState

        if (isLoading != other.isLoading) return false
        if (selectedFileName != other.selectedFileName) return false
        if (fileBytes != null) {
            if (other.fileBytes == null) return false
            if (!fileBytes.contentEquals(other.fileBytes)) return false
        } else if (other.fileBytes != null) return false
        if (isPngFile != other.isPngFile) return false
        if (parsedCardData != other.parsedCardData) return false
        if (specVersion != other.specVersion) return false
        if (customization != other.customization) return false
        if (selectedVisibility != other.selectedVisibility) return false
        if (errorMessage != other.errorMessage) return false
        if (isImportSuccess != other.isImportSuccess) return false
        if (importedCharacterId != other.importedCharacterId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isLoading.hashCode()
        result = 31 * result + (selectedFileName?.hashCode() ?: 0)
        result = 31 * result + (fileBytes?.contentHashCode() ?: 0)
        result = 31 * result + isPngFile.hashCode()
        result = 31 * result + (parsedCardData?.hashCode() ?: 0)
        result = 31 * result + (specVersion?.hashCode() ?: 0)
        result = 31 * result + customization.hashCode()
        result = 31 * result + selectedVisibility.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + isImportSuccess.hashCode()
        result = 31 * result + (importedCharacterId?.hashCode() ?: 0)
        return result
    }
}
