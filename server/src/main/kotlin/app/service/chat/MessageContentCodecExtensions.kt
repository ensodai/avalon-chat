package com.ensodai.shared.avalon.chat.services

import com.ensodai.shared.avalon.chat.models.ImagePayload
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import app.utils.toServerUrl

fun MessageContentCodec.processImageContentUrl(contentString: String): String {
    val payload = decodeAs<ImagePayload>(contentString)
    return if (payload != null) {
        encode(AvalonMessageContentType.IMAGE, payload.copy(url = payload.url.toServerUrl()))
    } else contentString
}
