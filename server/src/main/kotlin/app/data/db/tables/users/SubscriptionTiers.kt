package app.data.db.tables.users
import com.ensodai.shared.avalon.gifts.models.*

import com.ensodai.shared.avalon.gifts.models.PremiumStatus

import app.core.BaseUuidTable

object SubscriptionTiers : BaseUuidTable("subscription_tiers") {
    val code = enumerationByName<PremiumStatus>("code", 20).uniqueIndex()
    val dailyEnergy = integer("daily_energy").default(0)
    val maxCapacity = integer("max_capacity").default(0)
}
