package app.domain.liveCall.usecases

import app.data.db.tables.LlmConfig
import app.data.db.tables.liveCall.LiveTurnBillingDisposition
import app.data.repository.LiveCallTurnLogEntry
import app.data.repository.LiveCallTurnLogRepository
import com.ensodai.shared.avalon.liveCall.models.LiveCallTurnCompleteReason
import app.service.EnergyService
import app.data.models.EnergyDeductionResult
import app.utils.logger.WssLogger
import kotlin.uuid.Uuid
import org.koin.core.annotation.Factory

data class LiveTurnUsageSnapshot(
    val totalTokens: Long?,
    val promptTokens: Long?,
    val responseTokens: Long?,
    val cachedTokens: Long?,
    val thoughtsTokens: Long?
)

data class ProcessCompletedLiveTurnCommand(
    val userId: Uuid,
    val chatVariantId: Uuid,
    val llmConfig: LlmConfig,
    val resolvedModel: String,
    val hadModelOutput: Boolean,
    val interrupted: Boolean,
    val waitingForInput: Boolean,
    val turnCompleteReason: LiveCallTurnCompleteReason?,
    val usage: LiveTurnUsageSnapshot?
)

sealed interface ProcessCompletedLiveTurnResult {
    data class Billed(
        val energyHistoryRecordId: Uuid?
    ) : ProcessCompletedLiveTurnResult

    data object NonBillable : ProcessCompletedLiveTurnResult

    data object AmbiguousLogged : ProcessCompletedLiveTurnResult

    data object Rejected : ProcessCompletedLiveTurnResult

    data class StopInsufficientEnergy(
        val requiredCost: Int,
        val dailyEnergy: Int,
        val reserveEnergy: Int
    ) : ProcessCompletedLiveTurnResult

    data object StopUserNotFound : ProcessCompletedLiveTurnResult
}

@Factory
class ProcessCompletedLiveTurnUseCase(
    private val spendLiveCallTurnUseCase: SpendLiveCallTurnUseCase,
    private val energyService: EnergyService,
    private val turnLogRepository: LiveCallTurnLogRepository
) {

    suspend operator fun invoke(command: ProcessCompletedLiveTurnCommand): ProcessCompletedLiveTurnResult {
        val disposition = classify(command)

        return when (disposition) {
            LiveTurnBillingDisposition.BILLABLE -> bill(command, disposition)
            LiveTurnBillingDisposition.NON_BILLABLE -> {
                logTurn(command, disposition, energyCost = null, energyHistoryRecordId = null)
                WssLogger.info(
                    "LiveCall turn classified as non-billable: reason=${command.turnCompleteReason}, interrupted=${command.interrupted}, waitingForInput=${command.waitingForInput}"
                )
                ProcessCompletedLiveTurnResult.NonBillable
            }

            LiveTurnBillingDisposition.AMBIGUOUS -> {
                logTurn(command, disposition, energyCost = null, energyHistoryRecordId = null)
                WssLogger.info(
                    "LiveCall turn classified as ambiguous: reason=${command.turnCompleteReason}, interrupted=${command.interrupted}, waitingForInput=${command.waitingForInput}, hadModelOutput=${command.hadModelOutput}"
                )
                ProcessCompletedLiveTurnResult.AmbiguousLogged
            }

            LiveTurnBillingDisposition.REJECTED -> {
                logTurn(command, disposition, energyCost = null, energyHistoryRecordId = null)
                WssLogger.info(
                    "LiveCall turn rejected by model: reason=${command.turnCompleteReason}"
                )
                ProcessCompletedLiveTurnResult.Rejected
            }
        }
    }

    private suspend fun bill(
        command: ProcessCompletedLiveTurnCommand,
        disposition: LiveTurnBillingDisposition
    ): ProcessCompletedLiveTurnResult {
        return when (val result = spendLiveCallTurnUseCase(
            SpendLiveCallTurnCommand(
                userId = command.userId,
                chatVariantId = command.chatVariantId,
                llmConfig = command.llmConfig,
                resolvedModel = command.resolvedModel
            )
        )) {
            is EnergyDeductionResult.Success -> {
                result.energyHistoryRecordId?.let { recordId ->
                    command.usage?.let { usage ->
                        energyService.updateTokenStats(
                            recordId = recordId,
                            totalTokens = usage.totalTokens ?: 0L,
                            promptTokens = usage.promptTokens ?: 0L,
                            outputTokens = usage.responseTokens ?: 0L,
                            cachedTokens = usage.cachedTokens ?: 0L,
                            thoughtsTokens = usage.thoughtsTokens ?: 0L,
                            modelName = command.resolvedModel,
                            cost = command.llmConfig.energyCost,
                            modelCode = command.llmConfig.code
                        )
                    }
                }

                logTurn(command, disposition, command.llmConfig.energyCost, result.energyHistoryRecordId)

                ProcessCompletedLiveTurnResult.Billed(
                    energyHistoryRecordId = result.energyHistoryRecordId
                )
            }

            is EnergyDeductionResult.InsufficientFunds -> {
                logTurn(command, disposition, energyCost = null, energyHistoryRecordId = null)
                ProcessCompletedLiveTurnResult.StopInsufficientEnergy(
                    requiredCost = result.requiredCost,
                    dailyEnergy = result.currentBalance.dailyEnergy,
                    reserveEnergy = result.currentBalance.reserveEnergy
                )
            }

                            is EnergyDeductionResult.RouteError -> {
                    throw IllegalStateException("RouteError not supported in this flow yet")
                }
                EnergyDeductionResult.UserNotFound -> {
                logTurn(command, disposition, energyCost = null, energyHistoryRecordId = null)
                ProcessCompletedLiveTurnResult.StopUserNotFound
            }
        }
    }

    private fun logTurn(
        command: ProcessCompletedLiveTurnCommand,
        disposition: LiveTurnBillingDisposition,
        energyCost: Int?,
        energyHistoryRecordId: Uuid?
    ) {
        turnLogRepository.logTurn(
            LiveCallTurnLogEntry(
                userId = command.userId,
                chatVariantId = command.chatVariantId,
                liveModel = command.resolvedModel,
                turnCompleteReason = command.turnCompleteReason?.name,
                hadModelTurn = command.hadModelOutput,
                interrupted = command.interrupted,
                waitingForInput = command.waitingForInput,
                totalTokens = command.usage?.totalTokens,
                promptTokens = command.usage?.promptTokens,
                responseTokens = command.usage?.responseTokens,
                cachedTokens = command.usage?.cachedTokens,
                thoughtsTokens = command.usage?.thoughtsTokens,
                billingDisposition = disposition,
                energyCost = energyCost,
                energyHistoryRecordId = energyHistoryRecordId,
                metaData = null
            )
        )
    }

    private fun classify(command: ProcessCompletedLiveTurnCommand): LiveTurnBillingDisposition {
        if (command.turnCompleteReason == LiveCallTurnCompleteReason.RESPONSE_REJECTED) {
            return LiveTurnBillingDisposition.REJECTED
        }

        if (!command.hadModelOutput) {
            return LiveTurnBillingDisposition.AMBIGUOUS
        }

        if (command.interrupted) {
            return LiveTurnBillingDisposition.NON_BILLABLE
        }

        if (command.turnCompleteReason == LiveCallTurnCompleteReason.NEED_MORE_INPUT) {
            return LiveTurnBillingDisposition.NON_BILLABLE
        }

        return LiveTurnBillingDisposition.BILLABLE
    }
}
