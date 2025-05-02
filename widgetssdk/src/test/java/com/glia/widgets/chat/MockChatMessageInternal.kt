package com.glia.widgets.chat

import com.glia.androidsdk.chat.ChatMessage
import com.glia.widgets.chat.model.Gva
import com.glia.widgets.internal.engagement.domain.model.ChatMessageInternal
import org.json.JSONObject
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class MockChatMessageInternal {
    val messageId = "message_id"
    val operatorId = "operator_id"
    val messageTimeStamp = 123L
    val operatorImageUrl = "operator_url"
    val operatorName = "operator_name"
    val content = "content"

    private val chatMessage: ChatMessage = mock()
    val chatMessageInternal: ChatMessageInternal = mock()

    init {
        whenever(chatMessageInternal.chatMessage) doReturn chatMessage
    }

    fun mockChatMessage(metadata: JSONObject = JSONObject()) {
        whenever(chatMessage.id) doReturn messageId
        whenever(chatMessage.timestamp) doReturn messageTimeStamp
        whenever(chatMessage.metadata) doReturn metadata
        whenever(chatMessage.content) doReturn content
    }

    fun mockOperatorProperties() {
        whenever(chatMessageInternal.operatorId) doReturn operatorId
        whenever(chatMessageInternal.operatorImageUrl) doReturn operatorImageUrl
        whenever(chatMessageInternal.operatorName) doReturn operatorName
    }

    fun metadataWithContent(content: String = this.content): JSONObject = JSONObject().put(Gva.Keys.CONTENT, content)

    fun reset() {
        Mockito.reset(chatMessage, chatMessageInternal)
    }
}
