package com.ensodai.avalon.data.db.models.user

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.ensodai.shared.avalon.gifts.models.*
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    val id: Uuid,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String?,
    @ColumnInfo(name = "nickname")
    val nickname: String,
    @ColumnInfo(name = "current_quota")
    val currentQuota: Int,
    @ColumnInfo(name = "max_quota")
    val maxQuota: Int,
    @ColumnInfo(name = "reserve_balance")
    val reserveBalance: Int,
    @ColumnInfo(name = "max_reserve_balance")
    val maxReserveBalance: Int,
    @ColumnInfo(name = "premium_status")
    val premiumStatus: PremiumStatus,
    @ColumnInfo(name = "default_persona_id")
    val defaultPersonaId: Uuid?,
    @ColumnInfo(name = "payment_provider")
    val paymentProvider: SubscriptionProvider?,
    @ColumnInfo(name = "next_billing_date")
    val nextBillingDate: Instant?,
    @ColumnInfo(name = "is_outreach_enabled")
    val isOutreachEnabled: Boolean = false,
    @ColumnInfo(name = "daily_outreach_limit")
    val dailyOutreachLimit: Int = 3,
    @ColumnInfo(name = "is_flow_mode_enabled")
    val isFlowModeEnabled: Boolean = false
)
