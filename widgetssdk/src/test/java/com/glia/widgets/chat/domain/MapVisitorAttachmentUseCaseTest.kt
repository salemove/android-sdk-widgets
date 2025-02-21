package com.glia.widgets.chat.domain

import com.glia.androidsdk.chat.AttachmentFile
import com.glia.androidsdk.chat.VisitorMessage
import com.glia.widgets.chat.model.VisitorAttachmentItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapVisitorAttachmentUseCaseTest {
    private lateinit var mockAttachment: AttachmentFile
    private lateinit var visitorMessage: VisitorMessage
    private val useCase: MapVisitorAttachmentUseCase = MapVisitorAttachmentUseCase()

    @Before
    fun setUp() {
        mockAttachment = mock()
        whenever(mockAttachment.id) doReturn "attachment_id"

        visitorMessage = mock()
        whenever(visitorMessage.id) doReturn "message_id"
        whenever(visitorMessage.timestamp) doReturn -1
    }

    @Test
    fun `invoke returns VisitorAttachmentItem_RemoteImage when attachment is Image`() {
        whenever(mockAttachment.contentType) doReturn "image_sdj"
        val newAttachment = useCase(mockAttachment, visitorMessage)

        assertTrue(newAttachment is VisitorAttachmentItem.RemoteImage)
        assertEquals(newAttachment.id, mockAttachment.id)
        assertEquals(newAttachment.timestamp, visitorMessage.timestamp)
        assertFalse(newAttachment.isError)
    }

    @Test
    fun `invoke returns VisitorAttachmentItem_RemoteFile when attachment is not Image`() {
        whenever(mockAttachment.contentType) doReturn "imasge_sdj"
        val newAttachment = useCase(mockAttachment, visitorMessage)

        assertTrue(newAttachment is VisitorAttachmentItem.RemoteFile)
        assertEquals(newAttachment.id, mockAttachment.id)
        assertEquals(newAttachment.timestamp, visitorMessage.timestamp)
        assertFalse(newAttachment.isError)
    }
}
