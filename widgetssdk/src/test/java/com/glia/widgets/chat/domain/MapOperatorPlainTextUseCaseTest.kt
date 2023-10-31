package com.glia.widgets.chat.domain

import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.OperatorMessageItem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MapOperatorPlainTextUseCaseTest {
    private val mockChatMessageInternal: MockChatMessageInternal = MockChatMessageInternal()
    private val useCase: MapOperatorPlainTextUseCase = MapOperatorPlainTextUseCase()

    @Before
    fun setUp() {
        mockChatMessageInternal.mockChatMessage()
        mockChatMessageInternal.mockOperatorProperties()
    }

    @After
    fun tearDown() {
        mockChatMessageInternal.reset()
    }

    @Test
    fun `invoke returns OperatorMessageItem_PlainText with showChatHead true when true is passed`() {
        mockChatMessageInternal.apply {
            val message = useCase(chatMessageInternal, true)

            assertTrue(message is OperatorMessageItem.PlainText)

            assertEquals(message.showChatHead, true)
            assertEquals(message.id, messageId)
            assertEquals(message.timestamp, messageTimeStamp)
            assertEquals(message.operatorProfileImgUrl, operatorImageUrl)
            assertEquals(message.operatorId, operatorId)
            assertEquals(message.operatorName, operatorName)
            assertEquals(message.content, content)
        }
    }

    @Test
    fun `invoke returns OperatorMessageItem_PlainText with showChatHead false when false is passed`() {
        mockChatMessageInternal.apply {
            val message = useCase(chatMessageInternal, false)

            assertTrue(message is OperatorMessageItem.PlainText)
            assertEquals(message.showChatHead, false)
            assertEquals(message.id, messageId)
            assertEquals(message.timestamp, messageTimeStamp)
            assertEquals(message.operatorProfileImgUrl, operatorImageUrl)
            assertEquals(message.operatorId, operatorId)
            assertEquals(message.operatorName, operatorName)
            assertEquals(message.content, content)
        }
    }
}
