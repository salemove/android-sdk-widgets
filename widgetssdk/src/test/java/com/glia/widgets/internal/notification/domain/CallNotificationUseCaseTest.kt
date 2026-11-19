package com.glia.widgets.internal.notification.domain

import com.glia.androidsdk.comms.Audio
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.Video
import com.glia.widgets.internal.notification.domain.CallNotification.Phase.LIVE
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallNotificationUseCaseTest {
    private val useCase = CallNotificationUseCase()

    @Test
    fun `returns an audio call when both sides have audio but no video`() {
        val call = useCase(mediaState(audio = mockk(), video = null), mediaState(audio = mockk(), video = null))

        assertEquals(CallNotification(LIVE, MediaDirection.NONE, hasAudio = true), call)
    }

    @Test
    fun `returns a one-way video call when the operator has video and the visitor does not`() {
        val call = useCase(mediaState(audio = mockk(), video = null), mediaState(audio = mockk(), video = mockk()))

        assertEquals(CallNotification(LIVE, MediaDirection.ONE_WAY, hasAudio = true), call)
    }

    @Test
    fun `returns a two-way video call when both sides have video`() {
        val call = useCase(mediaState(audio = mockk(), video = mockk()), mediaState(audio = mockk(), video = mockk()))

        assertEquals(CallNotification(LIVE, MediaDirection.TWO_WAY, hasAudio = true), call)
    }

    @Test
    fun `returns a two-way video call without audio when neither side has audio`() {
        val call = useCase(mediaState(audio = null, video = mockk()), mediaState(audio = null, video = mockk()))

        assertEquals(CallNotification(LIVE, MediaDirection.TWO_WAY, hasAudio = false), call)
    }

    @Test
    fun `returns no call when neither side has media`() {
        assertNull(useCase(mediaState(audio = null, video = null), mediaState(audio = null, video = null)))
    }

    @Test(expected = IllegalStateException::class)
    fun `rejects one-way visitor video`() {
        useCase(mediaState(audio = null, video = mockk()), mediaState(audio = null, video = null))
    }

    @Test(expected = IllegalStateException::class)
    fun `rejects one-way audio`() {
        useCase(mediaState(audio = mockk(), video = null), mediaState(audio = null, video = null))
    }

    private fun mediaState(audio: Audio?, video: Video?): MediaState = mockk<MediaState>().also {
        every { it.audio } returns audio
        every { it.video } returns video
    }
}
