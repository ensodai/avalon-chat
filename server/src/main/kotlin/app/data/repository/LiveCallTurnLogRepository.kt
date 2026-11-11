package app.data.repository

import app.data.db.tables.liveCall.LiveCallTurnLogs
import app.data.db.tables.liveCall.LiveTurnBillingDisposition
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

data class LiveCallTurnLogEntry(
    val userId: Uuid,
    val chatVariantId: Uuid,
    val liveModel: String?,
    val turnCompleteReason: String?,
    val hadModelTurn: Boolean,
    val interrupted: Boolean,
    val waitingForInput: Boolean,
    val totalTokens: Long?,
    val promptTokens: Long?,
    val responseTokens: Long?,
    val cachedTokens: Long?,
    val thoughtsTokens: Long?,
    val billingDisposition: LiveTurnBillingDisposition,
    val energyCost: Int?,
    val energyHistoryRecordId: Uuid?,
    val metaData: String?
)

@Single
class LiveCallTurnLogRepository {

    fun logTurn(entry: LiveCallTurnLogEntry) {
        transaction {
            LiveCallTurnLogs.insert { row ->
                row[LiveCallTurnLogs.id] = Uuid.random()
                row[LiveCallTurnLogs.user] = entry.userId
                row[LiveCallTurnLogs.chatVariantId] = entry.chatVariantId
                row[LiveCallTurnLogs.liveModel] = entry.liveModel
                row[LiveCallTurnLogs.turnCompleteReason] = entry.turnCompleteReason
                row[LiveCallTurnLogs.hadModelTurn] = entry.hadModelTurn
                row[LiveCallTurnLogs.interrupted] = entry.interrupted
                row[LiveCallTurnLogs.waitingForInput] = entry.waitingForInput
                row[LiveCallTurnLogs.totalTokens] = entry.totalTokens
                row[LiveCallTurnLogs.promptTokens] = entry.promptTokens
                row[LiveCallTurnLogs.responseTokens] = entry.responseTokens
                row[LiveCallTurnLogs.cachedTokens] = entry.cachedTokens
                row[LiveCallTurnLogs.thoughtsTokens] = entry.thoughtsTokens
                row[LiveCallTurnLogs.billingDisposition] = entry.billingDisposition
                row[LiveCallTurnLogs.energyCost] = entry.energyCost
                row[LiveCallTurnLogs.energyHistoryRecordId] = entry.energyHistoryRecordId
                row[LiveCallTurnLogs.metaData] = entry.metaData
            }
        }
    }
}
