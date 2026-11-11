package app.application.chat

import app.domain.chat.usecases.ToggleReactionUseCase
import app.domain.chat.usecases.ChangeActiveContainerUseCase
import app.domain.chat.usecases.DeleteMessageUseCase
import app.domain.chat.usecases.EditChatMessageUseCase
import app.domain.chat.usecases.HandleChatMessageUseCase
import app.service.chat.MessageNotifier
import com.ensodai.shared.avalon.chat.wss.ClientMessage
import app.utils.logger.WssLogger
import org.koin.core.annotation.Single

@Single
class ChatCommandDispatcher(
    private val chatContextLoader: ChatContextLoader,
    private val sendMessageHandler: SendMessageChatCommandHandler,
    private val handleChatMessageUseCase: HandleChatMessageUseCase,
    private val editUseCase: EditChatMessageUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val changeActiveContainerUseCase: ChangeActiveContainerUseCase,
    private val toggleReactionUseCase: ToggleReactionUseCase,
    private val messageNotifier: MessageNotifier
) {

    suspend fun dispatch(actor: ChatActor, command: ChatCommand) {
        when (val loadResult = chatContextLoader.load(actor = actor, command = command)) {
            is ChatContextLoadResult.Failure -> {
                WssLogger.fail("Chat command rejected: ${loadResult.reason}")
                messageNotifier.notifyUserWithError(actor.userId, loadResult.reason)
                return
            }

            is ChatContextLoadResult.Success -> {
                dispatchValidated(
                    actor = actor,
                    command = loadResult.context.command,
                    context = loadResult.context
                )
            }
        }
    }

    private suspend fun dispatchValidated(
        actor: ChatActor,
        command: ChatCommand,
        context: ChatCommandContext
    ) {
        when (command) {
            is ChatCommand.SendMessage -> {
                sendMessageHandler.handle(
                    actor = actor,
                    command = command,
                    context = context
                )
            }

            is ChatCommand.ChangeActiveContainer -> {
                changeActiveContainerUseCase.change(
                    userId = actor.userId,
                    message = ClientMessage.ChangeActiveContainer(
                        chatVariantId = command.chatVariantId,
                        chatSessionId = command.chatSessionId,
                        slotId = command.slotId,
                        newActiveContainerId = command.newActiveContainerId
                    )
                )
            }

            is ChatCommand.DeleteMessages -> {
                deleteMessageUseCase.delete(
                    userId = actor.userId,
                    message = ClientMessage.DeleteMessages(
                        slotIds = command.slotIds,
                        chatVariantId = command.chatVariantId,
                        chatSessionId = command.chatSessionId
                    )
                )
            }

            is ChatCommand.EditMessage -> {
                editUseCase.edit(
                    userId = actor.userId,
                    message = ClientMessage.EditChatMessage(
                        slotId = command.slotId,
                        containerId = command.containerId,
                        chatVariantId = command.chatVariantId,
                        chatSessionId = command.chatSessionId,
                        text = command.text,
                        messageSystemInstruction = command.messageSystemInstruction
                    )
                )
            }

            is ChatCommand.ForceStartGenerateResponse -> {
                handleChatMessageUseCase.forceStart(
                    userId = actor.userId,
                    chatSessionId = command.chatSessionId,
                    chatVariantId = command.chatVariantId,
                    characterId = command.characterId,
                    llmModelCode = command.llmModelCode
                )
            }

            is ChatCommand.StopGenerateResponse -> {
                handleChatMessageUseCase.stop(command.chatSessionId)
            }

            is ChatCommand.GenerateNewContainer -> {
                handleChatMessageUseCase.generateVariant(
                    userId = actor.userId,
                    chatSessionId = command.chatSessionId,
                    chatVariantId = command.chatVariantId,
                    characterId = command.characterId,
                    slotId = command.slotId,
                    llmModelCode = command.llmModelCode
                )
            }

            is ChatCommand.SendUserReaction -> {
                toggleReactionUseCase.toggleReaction(
                    userId = actor.userId,
                    reaction = command.reaction,
                    characterId = command.characterId,
                    chatVariantId = command.chatVariantId,
                    chatSessionId = command.chatSessionId,
                    containerId = command.containerId,
                    slotId = command.slotId,
                    wssUserSessionId = actor.sessionId
                )
            }
        }
    }
}
