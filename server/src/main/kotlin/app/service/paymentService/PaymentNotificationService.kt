package app.service.paymentService
import com.ensodai.shared.avalon.gifts.models.*

import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.java.KoinJavaComponent
import java.math.BigDecimal
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val notificationLog = KotlinLogging.logger("PaymentNotifications")

class PaymentNotificationService(
    private val adminChatId: Long
) {

    // Lazy получение Bot для избежания циклической зависимости
    private fun getBot(): Bot {
        return KoinJavaComponent.getKoin().get()
    }

    /**
     * Отправляет уведомление о новой подписке или продлении в Telegram
     */
    suspend fun sendNewSubscriptionNotification(
        userId: Uuid,
        plan: PremiumStatus,
        amount: BigDecimal?,
        currency: String?,
        provider: SubscriptionProvider,
        expirationDate: Instant,
        isAutoRenewing: Boolean,
        isTestPurchase: Boolean,
        countryCode: String?,
        isRenewal: Boolean = false
    ) = withContext(Dispatchers.IO) {
        if (adminChatId <= 0L) {
            notificationLog.debug { "Skip TG notification: TELEGRAM_ADMIN_ID not configured" }
            return@withContext
        }
        try {
            val bot = getBot()
            val message = formatSubscriptionMessage(
                userId = userId,
                plan = plan,
                amount = amount,
                currency = currency,
                provider = provider,
                expirationDate = expirationDate,
                isAutoRenewing = isAutoRenewing,
                isTestPurchase = isTestPurchase,
                countryCode = countryCode,
                isRenewal = isRenewal
            )

            bot.sendMessage(
                chatId = ChatId.fromId(adminChatId),
                text = message,
                parseMode = ParseMode.HTML
            )

            notificationLog.info { "📬 [TG_NOTIFY] Sent notification for user $userId, plan $plan" }
        } catch (e: Exception) {
            notificationLog.error(e) { "❌ [TG_NOTIFY] Failed to send notification for user $userId" }
        }
    }

    /**
     * Форматирует красивое сообщение для Telegram с HTML-разметкой
     */
    private fun formatSubscriptionMessage(
        userId: Uuid,
        plan: PremiumStatus,
        amount: BigDecimal?,
        currency: String?,
        provider: SubscriptionProvider,
        expirationDate: Instant,
        isAutoRenewing: Boolean,
        isTestPurchase: Boolean,
        countryCode: String?,
        isRenewal: Boolean
    ): String {
        // Конвертация суммы в рубли (если валюта не RUB)
        val formattedAmount = formatAmount(amount, currency)

        // Эмодзи для типа события
        val eventEmoji = if (isRenewal) "🔄" else "🎉"
        
        // Эмодзи для плана подписки
        val planEmoji = getPlanEmoji(plan)
        
        // Эмодзи провайдера
        val providerEmoji = getProviderEmoji(provider)
        
        // Форматирование даты
        val moscowTime = expirationDate.toLocalDateTime(TimeZone.of("Europe/Moscow"))
        val javaLocalDateTime = java.time.LocalDateTime.of(
            moscowTime.year,
            moscowTime.month.number,
            moscowTime.day,
            moscowTime.hour,
            moscowTime.minute,
            moscowTime.second
        )
        val formattedDate = javaLocalDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        
        // Автопродление
        val autoRenewalText = if (isAutoRenewing) "✅ Включено" else "❌ Отключено"
        
        // Тестовая покупка
        val testBadge = if (isTestPurchase) "\n🧪 <b>Тестовая покупка:</b> ДА" else ""
        
        // Страна
        val countryBadge = if (countryCode != null) "\n🌍 <b>Страна:</b> $countryCode" else ""

        return buildString {
            appendLine("$eventEmoji <b>${if (isRenewal) "ПРОДЛЕНИЕ ПОДПИСКИ" else "НОВАЯ ПОДПИСКА"}</b>")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("$planEmoji <b>Тариф:</b> <b>${getPlanDisplayName(plan)}</b>")
            appendLine("💰 <b>Сумма:</b> $formattedAmount")
            appendLine("$providerEmoji <b>Провайдер:</b> <code>${provider.name}</code>")
            appendLine()
            appendLine("👤 <b>ID пользователя:</b> <code>${userId}</code>")
            appendLine("📅 <b>Истекает:</b> $formattedDate")
            appendLine("🔁 <b>Автопродление:</b> $autoRenewalText")
            if (countryBadge.isNotEmpty()) appendLine(countryBadge)
            if (testBadge.isNotEmpty()) appendLine(testBadge)
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("🕒 ${java.time.LocalTime.now(ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")
        }
    }

    /**
     * Форматирует сумму с конвертацией в рубли
     */
    private fun formatAmount(amount: BigDecimal?, currency: String?): String {
        if (amount == null || currency == null) return "<i>Не указано</i>"

        return when (currency.uppercase()) {
            "RUB" -> "<b>${amount.setScale(2)} ₽</b>"
            "USD" -> {
                // Примерный курс (можно вынести в конфиг)
                val rubAmount = amount.multiply(BigDecimal("90"))
                "<b>${amount.setScale(2)} \$</b> (~${rubAmount.setScale(0)} ₽)"
            }
            "EUR" -> {
                val rubAmount = amount.multiply(BigDecimal("98"))
                "<b>${amount.setScale(2)} €</b> (~${rubAmount.setScale(0)} ₽)"
            }
            else -> "<b>${amount.setScale(2)} $currency</b>"
        }
    }

    /**
     * Возвращает эмодзи для плана подписки
     */
    private fun getPlanEmoji(plan: PremiumStatus): String {
        return when (plan) {
            PremiumStatus.FREE -> "🆓"
            PremiumStatus.SUPPORTER -> "❤️"
            PremiumStatus.PREMIUM -> "⭐"
            PremiumStatus.CRYSTAL -> "💎"
            PremiumStatus.ELITE -> "👑"
            PremiumStatus.SPECIAL -> "🎁"
            else -> "💳"
        }
    }

    /**
     * Возвращает отображаемое имя плана
     */
    private fun getPlanDisplayName(plan: PremiumStatus): String {
        return when (plan) {
            PremiumStatus.FREE -> "Free"
            PremiumStatus.SUPPORTER -> "Supporter"
            PremiumStatus.PREMIUM -> "Premium"
            PremiumStatus.CRYSTAL -> "Crystal"
            PremiumStatus.ELITE -> "Elite"
            PremiumStatus.SPECIAL -> "Special"
            else -> plan.name
        }
    }

    /**
     * Отправляет уведомление о покупке пакета энергии в Telegram
     */
    suspend fun sendConsumablePurchaseNotification(
        userId: Uuid,
        productId: String,
        amount: BigDecimal?,
        currency: String?,
        provider: SubscriptionProvider,
        isTestPurchase: Boolean,
        countryCode: String?
    ) = withContext(Dispatchers.IO) {
        if (adminChatId <= 0L) {
            notificationLog.debug { "Skip TG notification: TELEGRAM_ADMIN_ID not configured" }
            return@withContext
        }
        try {
            val bot = getBot()
            val message = formatConsumableMessage(
                userId = userId,
                productId = productId,
                amount = amount,
                currency = currency,
                provider = provider,
                isTestPurchase = isTestPurchase,
                countryCode = countryCode
            )

            bot.sendMessage(
                chatId = ChatId.fromId(adminChatId),
                text = message,
                parseMode = ParseMode.HTML
            )

            notificationLog.info { "📬 [TG_NOTIFY] Sent consumable notification for user $userId, product $productId" }
        } catch (e: Exception) {
            notificationLog.error(e) { "❌ [TG_NOTIFY] Failed to send consumable notification for user $userId" }
        }
    }

    /**
     * Форматирует сообщение о покупке энергии для Telegram с HTML-разметкой
     */
    private fun formatConsumableMessage(
        userId: Uuid,
        productId: String,
        amount: BigDecimal?,
        currency: String?,
        provider: SubscriptionProvider,
        isTestPurchase: Boolean,
        countryCode: String?
    ): String {
        val formattedAmount = formatAmount(amount, currency)
        val providerEmoji = getProviderEmoji(provider)
        val testBadge = if (isTestPurchase) "\n🧪 <b>Тестовая покупка:</b> ДА" else ""
        val countryBadge = if (countryCode != null) "\n🌍 <b>Страна:</b> $countryCode" else ""

        val energy = BillingProductSpecs.mapProductIdToEnergy(productId)
        val packName = if (energy > 0) {
            val typeName = when (energy) {
                500 -> "Стартовый"
                2000 -> "Средний"
                5000 -> "Большой"
                else -> "Дополнительный"
            }
            "⚡ $typeName пак ($energy энергии)"
        } else {
            "⚡ Пак энергии: $productId"
        }

        return buildString {
            appendLine("⚡ <b>ПОКУПКА ЭНЕРГИИ</b>")
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("📦 <b>Пак:</b> <b>$packName</b>")
            appendLine("💰 <b>Сумма:</b> $formattedAmount")
            appendLine("$providerEmoji <b>Провайдер:</b> <code>${provider.name}</code>")
            appendLine()
            appendLine("👤 <b>ID пользователя:</b> <code>$userId</code>")
            if (countryBadge.isNotEmpty()) appendLine(countryBadge)
            if (testBadge.isNotEmpty()) appendLine(testBadge)
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━")
            appendLine("🕒 ${java.time.LocalTime.now(ZoneId.of("Europe/Moscow")).format(DateTimeFormatter.ofPattern("HH:mm:ss"))}")
        }
    }

    /**
     * Возвращает эмодзи для провайдера
     */
    private fun getProviderEmoji(provider: SubscriptionProvider): String {
        return when (provider) {
            SubscriptionProvider.GOOGLE -> "🤖"
            SubscriptionProvider.RUSTORE -> "🇷🇺"
            else -> "💳"
        }
    }
}
