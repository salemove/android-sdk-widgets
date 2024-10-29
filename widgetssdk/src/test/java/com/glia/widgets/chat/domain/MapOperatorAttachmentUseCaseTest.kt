package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.widgets.chat.MockChatMessageInternal
import com.glia.widgets.chat.model.OperatorAttachmentItem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapOperatorAttachmentUseCaseTest {
    private val mockChatMessageInternal: MockChatMessageInternal = MockChatMessageInternal()
    private lateinit var mockAttachment: AttachmentFile
    private val useCase: MapOperatorAttachmentUseCase = MapOperatorAttachmentUseCase()

    @Before
    fun setUp() {
        mockAttachment = mock()
        whenever(mockAttachment.id) doReturn "attachment_id"
        mockChatMessageInternal.mockChatMessage()
        mockChatMessageInternal.mockOperatorProperties()
    }

    @After
    fun tearDown() {
        mockChatMessageInternal.reset()
    }

    @Test
    fun `invoke returns OperatorAttachmentItem_Image when attachment is Image`() {
        whenever(mockAttachment.contentType) doReturn "image_asdfg"
        mockChatMessageInternal.apply {
            val mappedMessage = useCase(mockAttachment, chatMessageInternal, true)
            assertTrue(mappedMessage is OperatorAttachmentItem.Image)
//  TODO          assertEquals(Attachment.Remote(mockAttachment), mappedMessage.attachment)
            assertEquals(messageTimeStamp, mappedMessage.timestamp)
            assertEquals(true, mappedMessage.showChatHead)
            assertEquals(operatorImageUrl, mappedMessage.operatorProfileImgUrl)
            assertEquals(operatorId, mappedMessage.operatorId)
        }
    }

    @Test
    fun `invoke returns OperatorAttachmentItem_File when attachment is not Image`() {
        whenever(mockAttachment.contentType) doReturn "imagse_asdfg"
        mockChatMessageInternal.apply {
            val mappedMessage = useCase(mockAttachment, chatMessageInternal, false)
            assertTrue(mappedMessage is OperatorAttachmentItem.File)
//            assertEquals(Attachment.Remote(mockAttachment), mappedMessage.attachment)
            assertEquals(messageTimeStamp, mappedMessage.timestamp)
            assertEquals(false, mappedMessage.showChatHead)
            assertEquals(operatorImageUrl, mappedMessage.operatorProfileImgUrl)
            assertEquals(operatorId, mappedMessage.operatorId)
        }
    }
}
