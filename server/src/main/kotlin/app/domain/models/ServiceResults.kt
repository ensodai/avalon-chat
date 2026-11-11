package app.domain.models

import kotlin.uuid.Uuid

sealed class NicknameUpdateResult {
    data object Success : NicknameUpdateResult()
    data object NicknameTaken : NicknameUpdateResult()
    data object UserNotFound : NicknameUpdateResult()
}

sealed class DeleteVariantResult {
    data class Success(val imagePathToDelete: String?) : DeleteVariantResult()
    data object VariantNotFound : DeleteVariantResult()
    data object CannotDeleteLast : DeleteVariantResult()
}
