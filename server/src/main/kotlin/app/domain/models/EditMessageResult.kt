package app.domain.models

import com.ensodai.shared.avalon.chat.wss.ServerMessage

sealed interface EditMessageResult {

    data class Success(val event: ServerMessage.MessageUpdated) : EditMessageResult

    object NotFound : EditMessageResult

    object Error : EditMessageResult
}