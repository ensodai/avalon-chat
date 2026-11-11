package com.ensodai.avalon.domain.chatEvents

import com.ensodai.avalon.utils.L
import com.ensodai.avalon.utils.getUTCTimeNow
import kotlin.uuid.Uuid


/**
 * Оптимизатор исходящей очереди локальных действий чата.
 */
class MessageQueueOptimizer {

    fun optimizeAndAdd(queue: ArrayDeque<LocalChatAction>, newItem: LocalChatAction) {

        // 1. Batch Delete (Удаление слотов)
        if (newItem is LocalChatAction.DeleteMessages) {
            processBatchDelete(queue, newItem)
            return
        }

        // 2. Stop Generation
        if (newItem is LocalChatAction.StopGenerateResponse) {
            processStopGeneration(queue, newItem)
            return
        }

        // 3. Pairwise Optimization
        val key = newItem.optimizationKey
        val index = queue.indexOfLast { it.optimizationKey == key }

        if (index == -1) {
            queue.add(newItem)
            return
        }

        val oldItem = queue[index]
        val decision = resolveConflict(oldItem, newItem)

        applyDecision(queue, index, decision, newItem)
    }

    private fun resolveConflict(old: LocalChatAction, new: LocalChatAction): OptimizationResult {
        return when (old) {
            is LocalChatAction.SendChatMessage if new is LocalChatAction.EditChatMessage ->
                OptimizationResult.Replace(old.copy(text = new.text))

            is LocalChatAction.EditChatMessage if new is LocalChatAction.EditChatMessage ->
                OptimizationResult.Replace(new)

            is LocalChatAction.ChangeActiveContainer if new is LocalChatAction.ChangeActiveContainer ->
                OptimizationResult.Replace(new)

            is LocalChatAction.GenerateNewContainer if new is LocalChatAction.GenerateNewContainer -> {
                OptimizationResult.Replace(new)
            }

            is LocalChatAction.ForceStartGenerateResponse if new is LocalChatAction.ForceStartGenerateResponse -> {
                OptimizationResult.Replace(new)
            }

            else -> OptimizationResult.KeepBoth
        }
    }

    private fun applyDecision(
        queue: ArrayDeque<LocalChatAction>,
        index: Int,
        decision: OptimizationResult,
        newItem: LocalChatAction
    ) {
        when (decision) {
            is OptimizationResult.Replace -> queue[index] = decision.action
            is OptimizationResult.Annihilate -> queue.removeAt(index)
            is OptimizationResult.RemoveOldAndAddNew -> {
                queue.removeAt(index)
                queue.add(newItem)
            }

            is OptimizationResult.KeepBoth -> queue.add(newItem)
        }
    }

    private fun processBatchDelete(
        queue: ArrayDeque<LocalChatAction>,
        deleteCommand: LocalChatAction.DeleteMessages
    ) {
        val slotsToDelete = deleteCommand.slotIds.toSet()
        val purelyLocalSlotIds = mutableSetOf<Uuid>()

        val removed = queue.removeAll { action ->
            val slotId = action.relatedSlotId

            if (slotId != null && slotsToDelete.contains(slotId)) {
                if (action is LocalChatAction.SendChatMessage) {
                    purelyLocalSlotIds.add(slotId)
                }
                true 
            } else {
                false
            }
        }

        if (removed) {
            L.d("Optimizer", "💥 Batch Delete wiped out related actions from queue")
        }

        val realSlotsToDeleteOnServer = deleteCommand.slotIds.filter { id ->
            !purelyLocalSlotIds.contains(id)
        }

        if (realSlotsToDeleteOnServer.isNotEmpty()) {
            val optimizedDeleteCommand = deleteCommand.copy(slotIds = realSlotsToDeleteOnServer)
            queue.add(optimizedDeleteCommand)
        }
    }

    private fun processStopGeneration(
        queue: ArrayDeque<LocalChatAction>,
        stopCmd: LocalChatAction.StopGenerateResponse
    ) {
        val stopTargetSessionId = stopCmd.chatSessionId

        val index = queue.indexOfLast { action ->
            when (action) {
                is LocalChatAction.ForceStartGenerateResponse -> {
                    action.chatSessionId == stopTargetSessionId
                }

                is LocalChatAction.GenerateNewContainer -> {
                    action.chatSessionId == stopTargetSessionId
                }

                else -> false
            }
        }

        if (index != -1) {
            val removed = queue.removeAt(index)
            L.d(
                "Optimizer",
                "💥 Annihilated Generation in Session $stopTargetSessionId: ${removed::class.simpleName} + Stop"
            )
            return
        }

        val stopIndex = queue.indexOfLast {
            it is LocalChatAction.StopGenerateResponse && it.chatSessionId == stopTargetSessionId
        }

        if (stopIndex != -1) {
            queue[stopIndex] = stopCmd
        } else {
            queue.add(stopCmd)
        }
    }
}

val LocalChatAction.optimizationKey: String
    get() = when (this) {
        is LocalChatAction.SendChatMessage -> "msg_$containerId"
        is LocalChatAction.EditChatMessage -> "msg_$containerId"
        is LocalChatAction.DeleteMessages -> "batch_delete_${getUTCTimeNow()}"
        is LocalChatAction.ForceStartGenerateResponse -> "gen_$chatSessionId"
        is LocalChatAction.StopGenerateResponse -> "gen_$chatSessionId"
        is LocalChatAction.ChangeActiveContainer -> "swipe_$slotId"
        is LocalChatAction.GenerateNewContainer -> "new_generated_$slotId"
        is LocalChatAction.SendUserReaction -> "message_reaction_$containerId"
    }

sealed interface OptimizationResult {
    data class Replace(val action: LocalChatAction) : OptimizationResult
    object Annihilate : OptimizationResult
    object RemoveOldAndAddNew : OptimizationResult
    object KeepBoth : OptimizationResult
}

private val LocalChatAction.relatedSlotId: Uuid?
    get() = when (this) {
        is LocalChatAction.SendChatMessage -> this.slotId
        is LocalChatAction.ChangeActiveContainer -> this.slotId
        is LocalChatAction.EditChatMessage -> this.slotId
        is LocalChatAction.GenerateNewContainer -> this.slotId
        else -> null
    }
