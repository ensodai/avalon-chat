package com.ensodai.avalon.presentation.selectAiProviderScreen.viewState

import com.ensodai.avalon.presentation.core.BaseViewState

data class AiProviderItemUi(
    val id: String,
    val displayName: String,
    val description: String,
    val keyUrl: String,
    val isDefault: Boolean = false,
    val badgeText: String? = null,
    val isFree: Boolean = false
)

data class SelectAiProviderScreenViewState(
    val isLoading: Boolean = false,
    val selectedProviderId: String = "google",
    val providers: List<AiProviderItemUi> = emptyList(),
    val updatingProviderId: String? = null,
    val errorMessage: String? = null
) : BaseViewState() {
    val isUpdating: Boolean get() = updatingProviderId != null
}
