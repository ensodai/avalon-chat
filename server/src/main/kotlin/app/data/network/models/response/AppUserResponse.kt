package app.data.network.models.response
import com.ensodai.shared.avalon.gifts.models.*

import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class AppUserResponse(
    @Contextual
    val id: Uuid,
    val name: String,
    val nickName: String,
    val avatarUrl: String?,
    val currentQuotaLimit: Int,
    val maxQuotaLimit: Int,
    val reserveBalance: Int,
    val maxReserveBalance: Int,
    val premiumStatus: PremiumStatus,
    @Contextual
    val defaultPersonaId: Uuid?,
    val paymentProvider: SubscriptionProvider?,
    @Contextual
    val nextBillingDate: Instant?
)
