package app.domain.liveCall

import app.data.models.UserEnergyBalance
import app.data.repository.UserRepository
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class LiveCallPolicyService(
    private val userRepository: UserRepository
) {

    suspend fun ensureCanStartCall(
        userId: Uuid,
        requiredTurnCost: Int
    ) {
        val balance = userRepository.getUserEnergyBalance(userId)
        if (balance.dailyEnergy + balance.reserveEnergy < requiredTurnCost) {
            throw InsufficientStartEnergyException(
                currentBalance = balance,
                requiredCost = requiredTurnCost
            )
        }
    }

    class InsufficientStartEnergyException(
        val currentBalance: UserEnergyBalance,
        val requiredCost: Int
    ) : RuntimeException("Insufficient energy to start live call")

    class ChatVariantNotFoundException(message: String) : RuntimeException(message)

    class ChatVariantNotOwnerException(message: String) : RuntimeException(message)
}
