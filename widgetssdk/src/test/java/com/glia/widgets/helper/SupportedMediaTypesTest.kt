package com.glia.widgets.helper

import com.glia.widgets.engagement.MediaType
import com.glia.widgets.queue.Queue
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
            every { status } returns Queue.Status.FULL
            every { media } returns listOf(MediaType.TEXT, MediaType.MESSAGING)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertEquals(1, supportedMediaTypes?.size)
        assertTrue(supportedMediaTypes!!.contains(MediaType.MESSAGING))
    }

    @Test
    fun `supportedMediaTypes returns null when queue is FULL and does not contain MESSAGING`() {
        val queue: Queue = mockk {
            every { status } returns Queue.Status.FULL
            every { media } returns listOf(MediaType.TEXT, MediaType.AUDIO)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertNull(supportedMediaTypes)
    }

    @Test
    fun `supportedMediaTypes returns MESSAGING when queue is UNSTAFFED and contains MESSAGING`() {
        val queue: Queue = mockk {
            every { status } returns Queue.Status.UNSTAFFED
            every { media } returns listOf(MediaType.TEXT, MediaType.MESSAGING)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertEquals(1, supportedMediaTypes?.size)
        assertTrue(supportedMediaTypes!!.contains(MediaType.MESSAGING))
    }

    @Test
    fun `supportedMediaTypes returns MESSAGING when queue is UNSTAFFED and does not contain MESSAGING`() {
        val queue: Queue = mockk {
            every { status } returns Queue.Status.UNSTAFFED
            every { media } returns listOf(MediaType.TEXT, MediaType.VIDEO)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertNull(supportedMediaTypes)
    }

    @Test
    fun `supportedMediaTypes filters out Phone and UNKNOWN when queue is OPEN`() {
        val queue: Queue = mockk {
            every { status } returns Queue.Status.OPEN
            every { media } returns listOf(MediaType.PHONE, MediaType.UNKNOWN)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertNull(supportedMediaTypes)
    }

    @Test
    fun `supportedMediaTypes filters returns all types except Phone and UNKNOWN when queue is OPEN`() {
        val queue: Queue = mockk {
            every { status } returns Queue.Status.OPEN
            every { media } returns listOf(
                MediaType.TEXT,
                MediaType.AUDIO,
                MediaType.VIDEO,
                MediaType.MESSAGING,
                MediaType.PHONE,
                MediaType.UNKNOWN
            )
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertEquals(4, supportedMediaTypes?.size)
        assertTrue(supportedMediaTypes!!.contains(MediaType.TEXT))
        assertTrue(supportedMediaTypes.contains(MediaType.AUDIO))
        assertTrue(supportedMediaTypes.contains(MediaType.VIDEO))
        assertTrue(supportedMediaTypes.contains(MediaType.MESSAGING))
    }

    @Test
    fun `supportedMediaTypes returns null when queue is CLOSED`() {
        val queue: Queue = mockk {
            every { status } returns Queue.Status.CLOSED
            every { media } returns listOf(MediaType.TEXT, MediaType.UNKNOWN)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertNull(supportedMediaTypes)
    }

    @Test
    fun `supportedMediaTypes returns null when queue status is UNKNOWN`() {
        val queue: Queue = mockk {
            every { status } returns Queue.Status.UNKNOWN
            every { media } returns listOf(MediaType.TEXT, MediaType.UNKNOWN)
        }

        val supportedMediaTypes = queue.supportedMediaTypes()

        assertNull(supportedMediaTypes)
    }

}
