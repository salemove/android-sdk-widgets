package com.glia.widgets.core.secureconversations.domain

import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.GliaWidgets

class SendSecureMessageUseCase(val queueId: String) {
    fun execute(message: String,
                callback: RequestCallback<VisitorMessage?>
    ) {
        val queueIds = arrayOf(queueId)
        GliaWidgets.getSecureConversations().send(message, queueIds, callback)
    }
}
