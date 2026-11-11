package app.data.models

import kotlin.uuid.Uuid

sealed interface EnergyDeductionResult {
    // 1. Успешное списание (возвращаем НОВЫЙ баланс)
    data class Success(
        val newBalance: UserEnergyBalance,
        val energyHistoryRecordId: Uuid? = null
    ) : EnergyDeductionResult

    // 2. Недостаточно средств (возвращаем ТЕКУЩИЙ баланс, чтобы показать юзеру)
    data class InsufficientFunds(
        val currentBalance: UserEnergyBalance,
        val requiredCost: Int
    ) : EnergyDeductionResult

    // 3. Юзер не найден (критическая ошибка данных)
    data object UserNotFound : EnergyDeductionResult

    // 4. Ошибка маршрутизации (от BillingRouter, например нет ключа)
    data class RouteError(
        val eventType: com.ensodai.shared.avalon.chat.models.SystemEventType
    ) : EnergyDeductionResult

}