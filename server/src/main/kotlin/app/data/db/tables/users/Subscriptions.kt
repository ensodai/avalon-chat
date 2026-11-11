package app.data.db.tables.users
import com.ensodai.shared.avalon.gifts.models.*

import app.core.BaseUuidTable
import com.ensodai.shared.avalon.gifts.models.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

enum class SubscriptionStatus {
    ACTIVE,
    CANCELED,
    EXPIRED
}

object Subscriptions : BaseUuidTable("subscriptions") {
    // Ссылка на юзера
    val user = reference("user_id", AppUsers).index("idx_subscriptions_user")

    // Статус и Провайдер
    val status = enumerationByName<SubscriptionStatus>("status", 20)
        .index("idx_subs_status")
    val provider = enumerationByName<SubscriptionProvider>("provider", 20)

    // Какой именно тариф куплен (например, "premium_monthly" или "gold_yearly")
    // Это важно, чтобы понимать, какой plan выставлять в AppUsers
    val productId = varchar("product_id", 100)

    // Храним тип плана (PREMIUM, CRYSTAL...), чтобы быстро понимать уровень доступа
    val plan = enumerationByName<PremiumStatus>("plan", 20)

    // --- ИДЕНТИФИКАТОРЫ ---

    // Google: purchaseToken. RuStore: purchaseId.
    // Это главный ключ, по которому мы проверяем валидность в API стора.
    // Делаем uniqueIndex, так как один токен = одна подписка.
    val purchaseId = text("purchase_token").uniqueIndex()

    // Google: orderId (GPA.123...). RuStore: invoiceId (последнего платежа).
    // Полезно для поиска проблемных платежей вручную.
    val lastOrderId = varchar("last_order_id", 100).nullable()

    // --- ВРЕМЯ ---

    val startDate = timestamp("start_date")
        .defaultExpression(CurrentTimestamp)

    val expiresAt = timestamp("expires_at")
        .index("idx_subs_expires")

    // Дата, когда мы в последний раз успешно свалидировали подписку через API
    // Полезно, чтобы не долбить API стора на каждый чих
    val lastValidationDate = timestamp("last_validation_date")
        .defaultExpression(CurrentTimestamp)

    val isAutoRenewing = bool("is_auto_renewing").default(true)

    val country = varchar("country", 30).nullable()

    val isTestPurchase = bool("is_test_purchase")
}

enum class TransactionType {
    SUBSCRIPTION_PURCHASE, // Первая покупка подписки
    SUBSCRIPTION_RENEWAL,  // Автопродление
    CONSUMABLE             // Разовая покупка (батарейки)
}

object Transactions : BaseUuidTable("transactions") {
    val user = reference("user_id", AppUsers)

    // Тип операции
    val type = enumerationByName<TransactionType>("type", 30)
    val provider = enumerationByName<SubscriptionProvider>("provider", 20)

    // ID товара ("premium_month" или "batteries_100")
    val productId = varchar("product_id", 100)

    // Уникальный ID транзакции из банка/стора
    // Google: orderId (GPA.123...), RuStore: invoiceId
    // uniqueIndex - защита от повторного начисления (идемпотентность)
    val transactionId = varchar("transaction_id", 255).uniqueIndex()

    // Ссылка на токен подписки (если это операция по подписке)
    // Может быть null, если купили просто батарейки
    val relatedSubscriptionId = text("related_subscription_token").nullable()

    // Снимаем комментарий!
    // precision = 19, scale = 4 (храним до 4 знаков после запятой для точности)
    val amount = decimal("amount", 19, 4).nullable()

    // "RUB", "USD", "EUR"
    val currency = varchar("currency", 3).nullable()

    val country = varchar("country", 30).nullable()

    val isTestPurchase = bool("is_test_purchase")
}
