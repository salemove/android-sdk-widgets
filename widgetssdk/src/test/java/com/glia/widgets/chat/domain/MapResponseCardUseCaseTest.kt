package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.SingleChoiceAttachment
import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.OperatorMessageItem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

class MapResponseCardUseCaseTest {
    private val mockChatMessageInternal: MockChatMessageInternal = MockChatMessageInternal()
    private val useCase: MapResponseCardUseCase = MapResponseCardUseCase()
    private val imageUrl = "sd"
    private lateinit var attachment: SingleChoiceAttachment

    @Before
    fun setUp() {
        attachment = mock()
        whenever(attachment.options) doReturn arrayOf(mock())
        whenever(attachment.imageUrl) doReturn Optional.ofNullable(imageUrl)

        mockChatMessageInternal.mockChatMessage()
        mockChatMessageInternal.mockOperatorProperties()
    }

    @After
    fun tearDown() {
        mockChatMessageInternal.reset()
    }

    @Test
    fun `invoke returns OperatorMessageItem_ResponseCard with showChatHead true when true is passed`() {
        mockChatMessageInternal.apply {
            val message = useCase(attachment, chatMessageInternal, true)

            assertTrue(message is OperatorMessageItem.ResponseCard)

            assertEquals(message.showChatHead, true)
            assertEquals(message.id, messageId)
            assertEquals(message.timestamp, messageTimeStamp)
            assertEquals(message.operatorProfileImgUrl, operatorImageUrl)
            assertEquals(message.operatorId, operatorId)
            assertEquals(message.operatorName, operatorName)
            assertEquals(message.content, content)
            assertEquals(message.singleChoiceOptions, attachment.options.asList())
            assertEquals(message.choiceCardImageUrl, imageUrl)
        }
    }

    @Test
    fun `invoke returns OperatorMessageItem_ResponseCard with showChatHead false when false is passed`() {
        mockChatMessageInternal.apply {
            val message = useCase(attachment, chatMessageInternal, false)

            assertTrue(message is OperatorMessageItem.ResponseCard)

            assertEquals(message.showChatHead, false)
            assertEquals(message.id, messageId)
            assertEquals(message.timestamp, messageTimeStamp)
            assertEquals(message.operatorProfileImgUrl, operatorImageUrl)
            assertEquals(message.operatorId, operatorId)
            assertEquals(message.operatorName, operatorName)
            assertEquals(message.content, content)
            assertEquals(message.singleChoiceOptions, attachment.options.asList())
            assertEquals(message.choiceCardImageUrl, imageUrl)
        }
    }

}
