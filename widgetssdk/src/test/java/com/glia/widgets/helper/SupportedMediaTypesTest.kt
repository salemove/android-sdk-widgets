package com.glia.widgets.helper

import com.glia.androidsdk.Engagement
import com.glia.androidsdk.queuing.QueueState
import com.glia.widgets.core.queue.Queue
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportedMediaTypesTest {

    @Test
    fun `supportedMediaTypes returns MESSAGING when queue is FULL and contains MESSAGING`() {
        val queue: Queue = mockk {
            every { status } returns QueueState.Status.FULL
            every { medias } returns listOf(Engagement.MediaType.TEXT, Engagement.MediaType.MESSAGING)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertEquals(1, supportedMediaTypes?.size)
        assertTrue(supportedMediaTypes!!.contains(Engagement.MediaType.MESSAGING))
    }

    @Test
    fun `supportedMediaTypes returns null when queue is FULL and does not contain MESSAGING`() {
        val queue: Queue = mockk {
            every { status } returns QueueState.Status.FULL
            every { medias } returns listOf(Engagement.MediaType.TEXT, Engagement.MediaType.AUDIO)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertNull(supportedMediaTypes)
    }

    @Test
    fun `supportedMediaTypes returns MESSAGING when queue is UNSTAFFED and contains MESSAGING`() {
        val queue: Queue = mockk {
            every { status } returns QueueState.Status.UNSTAFFED
            every { medias } returns listOf(Engagement.MediaType.TEXT, Engagement.MediaType.MESSAGING)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertEquals(1, supportedMediaTypes?.size)
        assertTrue(supportedMediaTypes!!.contains(Engagement.MediaType.MESSAGING))
    }

    @Test
    fun `supportedMediaTypes returns MESSAGING when queue is UNSTAFFED and does not contain MESSAGING`() {
        val queue: Queue = mockk {
            every { status } returns QueueState.Status.UNSTAFFED
            every { medias } returns listOf(Engagement.MediaType.TEXT, Engagement.MediaType.VIDEO)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertNull(supportedMediaTypes)
    }

    @Test
    fun `supportedMediaTypes filters out Phone and UNKNOWN when queue is OPEN`() {
        val queue: Queue = mockk {
            every { status } returns QueueState.Status.OPEN
            every { medias } returns listOf(Engagement.MediaType.PHONE, Engagement.MediaType.UNKNOWN)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertNull(supportedMediaTypes)
    }

    @Test
    fun `supportedMediaTypes filters returns all types except Phone and UNKNOWN when queue is OPEN`() {
        val queue: Queue = mockk {
            every { status } returns QueueState.Status.OPEN
            every { medias } returns listOf(
                Engagement.MediaType.TEXT,
                Engagement.MediaType.AUDIO,
                Engagement.MediaType.VIDEO,
                Engagement.MediaType.MESSAGING,
                Engagement.MediaType.PHONE,
                Engagement.MediaType.UNKNOWN
            )
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertEquals(4, supportedMediaTypes?.size)
        assertTrue(supportedMediaTypes!!.contains(Engagement.MediaType.TEXT))
        assertTrue(supportedMediaTypes.contains(Engagement.MediaType.AUDIO))
        assertTrue(supportedMediaTypes.contains(Engagement.MediaType.VIDEO))
        assertTrue(supportedMediaTypes.contains(Engagement.MediaType.MESSAGING))
    }

    @Test
    fun `supportedMediaTypes returns null when queue is CLOSED`() {
        val queue: Queue = mockk {
            every { status } returns QueueState.Status.CLOSED
            every { medias } returns listOf(Engagement.MediaType.TEXT, Engagement.MediaType.UNKNOWN)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertNull(supportedMediaTypes)
    }

    @Test
    fun `supportedMediaTypes returns null when queue status is UNKNOWN`() {
        val queue: Queue = mockk {
            every { status } returns QueueState.Status.UNKNOWN
            every { medias } returns listOf(Engagement.MediaType.TEXT, Engagement.MediaType.UNKNOWN)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertNull(supportedMediaTypes)
    }

}
