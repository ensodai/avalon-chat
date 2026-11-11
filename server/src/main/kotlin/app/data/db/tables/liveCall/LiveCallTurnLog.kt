package app.data.db.tables.liveCall

import app.core.BaseUuidTable
import app.data.db.tables.users.AppUsers

enum class LiveTurnBillingDisposition {
    BILLABLE,
    NON_BILLABLE,
    AMBIGUOUS,
    REJECTED
}

object LiveCallTurnLogs : BaseUuidTable("live_call_turn_logs") {
    val user = reference("user_id", AppUsers)
    val chatVariantId = uuid("chat_variant_id")
    val liveModel = varchar("live_model", 100).nullable()
    val turnCompleteReason = varchar("turn_complete_reason", 50).nullable()
    val hadModelTurn = bool("had_model_turn").default(false)
    val interrupted = bool("interrupted").default(false)
    val waitingForInput = bool("waiting_for_input").default(false)
    val totalTokens = long("total_tokens").nullable()
    val promptTokens = long("prompt_tokens").nullable()
    val responseTokens = long("response_tokens").nullable()
    val cachedTokens = long("cached_tokens").nullable()
    val thoughtsTokens = long("thoughts_tokens").nullable()
    val billingDisposition = enumerationByName("billing_disposition", 30, LiveTurnBillingDisposition::class)
    val energyCost = integer("energy_cost").nullable()
    val energyHistoryRecordId = uuid("energy_history_record_id").nullable()
    val metaData = text("meta_data").nullable()

    init {
        index("idx_live_turn_logs_user", false, user)
        index("idx_live_turn_logs_chat_variant", false, chatVariantId)
        index("idx_live_turn_logs_created", false, createdAt)
    }
}
