package com.glia.widgets.internal.notification

import android.app.Application
import android.app.Notification
import android.os.Build
import com.glia.androidsdk.comms.MediaDirection
import com.glia.widgets.R
import com.glia.widgets.di.Dependencies
import com.glia.widgets.internal.notification.domain.CallNotification
import com.glia.widgets.internal.notification.domain.CallNotification.Phase.CONNECTING
import com.glia.widgets.internal.notification.domain.CallNotification.Phase.LIVE
import com.glia.widgets.internal.notification.domain.CallNotification.Phase.WAITING
import com.glia.widgets.locale.LocaleProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
class NotificationFactoryTest {
    private lateinit var application: Application

    @Before
    fun setUp() {
        application = RuntimeEnvironment.getApplication()
        // Returns the resource ID as text, so each test can check which string the notification uses.
        Dependencies.localeProvider = mockk<LocaleProvider> {
            every { getString(any(), *anyVararg()) } answers { firstArg<Int>().toString() }
        }
    }

    @Test
    fun `waiting for an audio call tells the visitor an operator is coming`() {
        assertTexts(
            CallNotification(WAITING, MediaDirection.NONE, hasAudio = true),
            R.string.engagement_audio_title,
            R.string.android_call_queue_message
        )
    }

    @Test
    fun `waiting for a video call tells the visitor an operator is coming`() {
        assertTexts(
            CallNotification(WAITING, MediaDirection.TWO_WAY, hasAudio = true),
            R.string.engagement_video_title,
            R.string.android_call_queue_message
        )
    }

    @Test
    fun `connecting an audio call says the call is connecting`() {
        assertTexts(
            CallNotification(CONNECTING, MediaDirection.NONE, hasAudio = true),
            R.string.engagement_audio_title,
            R.string.glia_notification_call_connecting_message
        )
    }

    @Test
    fun `connecting a video call says the call is connecting`() {
        assertTexts(
            CallNotification(CONNECTING, MediaDirection.ONE_WAY, hasAudio = false),
            R.string.engagement_video_title,
            R.string.glia_notification_call_connecting_message
        )
    }

    @Test
    fun `live audio call shows the audio call text`() {
        assertTexts(
            CallNotification(LIVE, MediaDirection.NONE, hasAudio = true),
            R.string.android_notification_audio_call_title,
            R.string.android_notification_audio_call_message
        )
    }

    @Test
    fun `live one-way video call without audio shows the one-way video text`() {
        assertTexts(
            CallNotification(LIVE, MediaDirection.ONE_WAY, hasAudio = false),
            R.string.android_notification_one_way_video_title,
            R.string.android_notification_one_way_video_no_audio_message
        )
    }

    @Test
    fun `live two-way video call shows the two-way video text`() {
        assertTexts(
            CallNotification(LIVE, MediaDirection.TWO_WAY, hasAudio = true),
            R.string.android_notification_two_way_video_title,
            R.string.android_notification_two_way_video_message
        )
    }

    private fun assertTexts(call: CallNotification, title: Int, message: Int) {
        val notification = NotificationFactory.createCallNotification(application, call)

        assertEquals(title.toString(), notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString())
        assertEquals(message.toString(), notification.extras.getCharSequence(Notification.EXTRA_TEXT).toString())
    }
}
