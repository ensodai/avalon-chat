package com.ensodai.avalon.presentation.userAccountScreen.viewState

import androidx.compose.runtime.Immutable
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider
import com.ensodai.avalon.domain.models.PersonaInfo
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.utils.EMPTY_STRING
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Immutable
data class UserAccountScreenViewState(
    val userName: String = "User name",
    val userId: Uuid? = null,
    val userAvatar: String = EMPTY_STRING,
    val userNickName: String = "User nickname",
    val currentQuota: Int = 10,
    val maxQuota: Int = 10,
    val loadingState: LoadingState = LoadingState.Idle,
    val purchasedBatteries: Int = 100,
    val isAuthorized: Boolean = true,
    val isShowLogoutAlertDialog: Boolean = false,
    val isShowDeleteAccountAlertDialog: Boolean = false,
    val selectedPersona: PersonaInfo? = null,
    val personas: List<PersonaInfo> = emptyList(),
    val reserveBalance: Int = 0,
    val maxReserveBalance: Int = 0,
    val premiumStatus: UserTariff = UserTariff.Free,
    val bottomSheetState: UserAccountScreenBottomSheetState = UserAccountScreenBottomSheetState.Idle,
    val nextBillingDate: Instant? = null,
    val paymentProvider: SubscriptionProvider? = null,
    val charactersCount: Int = 10,
    val totalUnclaimedCreatorEnergy: Double = 0.0,
    val isEarlySupporter: Boolean = false
) : BaseViewState()

@Immutable
sealed class UserAccountScreenBottomSheetState {

    data object Idle : UserAccountScreenBottomSheetState()
    data object SupporterGratitude : UserAccountScreenBottomSheetState()
    data class ManageSubscription(
        val tariff: UserTariff,
        val nextBillingDate: Instant?,
        val provider: SubscriptionProvider?
    ) : UserAccountScreenBottomSheetState()

    data class ManageEnergy(
        val currentQuota: Int,
        val maxQuota: Int,
        val isShowUpgradeButton: Boolean
    ) : UserAccountScreenBottomSheetState()

    data class ManageReservedEnergy(
        val reserveBalance: Int,
        val maxReserveBalance: Int,
    ) : UserAccountScreenBottomSheetState()

}
