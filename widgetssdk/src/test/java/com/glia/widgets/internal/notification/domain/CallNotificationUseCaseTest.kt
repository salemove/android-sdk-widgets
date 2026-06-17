package com.glia.widgets.internal.notification.domain

import android.mock
import android.unMock
import com.glia.androidsdk.comms.Audio
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.Video
import com.glia.widgets.helper.Logger
import com.glia.widgets.internal.notification.device.INotificationManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class CallNotificationUseCaseTest {
    private lateinit var notificationManager: INotificationManager
    private lateinit var useCase: CallNotificationUseCase

    @Before
    fun setUp() {
        Logger.mock()
        notificationManager = mockk(relaxUnitFun = true)
        useCase = CallNotificationUseCase(notificationManager)
    }

    @After
    fun tearDown() {
        Logger.unMock()
    }

    @Test
    fun `shows audio notification when both sides have audio but no video`() {
        val visitorMedia = mediaState(audio = mockk(), video = null)
        val operatorMedia = mediaState(audio = mockk(), video = null)

        useCase(visitorMedia, operatorMedia)

        verify { notificationManager.showAudioCallNotification() }
    }

    @Test
    fun `shows one-way video notification when operator has video and visitor does not`() {
        val visitorMedia = mediaState(audio = mockk(), video = null)
        val operatorMedia = mediaState(audio = mockk(), video = mockk())

        useCase(visitorMedia, operatorMedia)

        verify { notificationManager.showVideoCallNotification(isTwoWayVideo = false, hasAudio = true) }
    }

    @Test
    fun `shows two-way video notification when both sides have video`() {
        val visitorMedia = mediaState(audio = mockk(), video = mockk())
        val operatorMedia = mediaState(audio = mockk(), video = mockk())

        useCase(visitorMedia, operatorMedia)

        verify { notificationManager.showVideoCallNotification(isTwoWayVideo = true, hasAudio = true) }
    }

    @Test
    fun `shows two-way video without audio when both have video but no audio`() {
        val visitorMedia = mediaState(audio = null, video = mockk())
        val operatorMedia = mediaState(audio = null, video = mockk())

        useCase(visitorMedia, operatorMedia)

        verify { notificationManager.showVideoCallNotification(isTwoWayVideo = true, hasAudio = false) }
    }

    @Test
    fun `removes notification when both sides have no media`() {
        useCase(null, null)

        verify { notificationManager.removeCallNotification() }
    }

    @Test
    fun `removes notification when both have no audio and no video`() {
        val visitorMedia = mediaState(audio = null, video = null)
        val operatorMedia = mediaState(audio = null, video = null)

        useCase(visitorMedia, operatorMedia)

        verify { notificationManager.removeCallNotification() }
    }

    @Test
    fun `removeAllNotifications removes notification`() {
        useCase.removeAllNotifications()

        verify { notificationManager.removeCallNotification() }
    }

    @Test
    fun `does not crash on unsupported one-way visitor video`() {
        val visitorMedia = mediaState(audio = null, video = mockk())
        val operatorMedia = mediaState(audio = null, video = null)

        // Should not throw — logs and returns early
        useCase(visitorMedia, operatorMedia)

        verify(exactly = 0) { notificationManager.showVideoCallNotification(any(), any()) }
        verify(exactly = 0) { notificationManager.showAudioCallNotification() }
        verify(exactly = 0) { notificationManager.removeCallNotification() }
    }

    private fun mediaState(audio: Audio?, video: Video?): MediaState = mockk<MediaState>().also {
        every { it.audio } returns audio
        every { it.video } returns video
    }
}
