package com.glia.widgets.launcher

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.widgets.chat.Intention
import com.glia.widgets.core.secureconversations.domain.HasPendingSecureConversationsWithTimeoutUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test

class EngagementLauncherImplTest {

    @MockK
    private lateinit var activityLauncher: ActivityLauncher

    @MockK
    private lateinit var hasPendingSecureConversationsWithTimeoutUseCase: HasPendingSecureConversationsWithTimeoutUseCase

    @MockK
    private lateinit var activity: Activity

    private lateinit var engagementLauncher: EngagementLauncherImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        engagementLauncher = EngagementLauncherImpl(activityLauncher, hasPendingSecureConversationsWithTimeoutUseCase)
    }

    @Test
    fun `startChat launches live chat when no pending secure conversations`() {
        every { hasPendingSecureConversationsWithTimeoutUseCase() } returns Single.just(false)

        engagementLauncher.startChat(activity)

        verify { activityLauncher.launchChat(activity, Intention.LIVE_CHAT) }
    }

    @Test
    fun `startChat launches secure conversation dialog when there are pending secure conversations`() {
        every { hasPendingSecureConversationsWithTimeoutUseCase() } returns Single.just(true)

        engagementLauncher.startChat(activity)

        verify { activityLauncher.launchChat(activity, Intention.SC_DIALOG_ENQUEUE_FOR_TEXT) }
    }

    @Test
    fun `startAudioCall launches audio call when no pending secure conversations`() {
        every { hasPendingSecureConversationsWithTimeoutUseCase() } returns Single.just(false)

        engagementLauncher.startAudioCall(activity)

        verify { activityLauncher.launchCall(activity, Engagement.MediaType.AUDIO, false) }
    }

    @Test
    fun `startAudioCall launches secure conversation audio dialog when there are pending secure conversations`() {
        every { hasPendingSecureConversationsWithTimeoutUseCase() } returns Single.just(true)

        engagementLauncher.startAudioCall(activity)

        verify { activityLauncher.launchChat(activity, Intention.SC_DIALOG_START_AUDIO) }
    }

    @Test
    fun `startVideoCall launches video call when no pending secure conversations`() {
        every { hasPendingSecureConversationsWithTimeoutUseCase() } returns Single.just(false)

        engagementLauncher.startVideoCall(activity)

        verify { activityLauncher.launchCall(activity, Engagement.MediaType.VIDEO, false) }
    }

    @Test
    fun `startVideoCall launches secure conversation video dialog when there are pending secure conversations`() {
        every { hasPendingSecureConversationsWithTimeoutUseCase() } returns Single.just(true)

        engagementLauncher.startVideoCall(activity)

        verify { activityLauncher.launchChat(activity, Intention.SC_DIALOG_START_VIDEO) }
    }

    @Test
    fun `startSecureMessaging launches secure messaging welcome screen when no pending secure conversations`() {
        every { hasPendingSecureConversationsWithTimeoutUseCase() } returns Single.just(false)

        engagementLauncher.startSecureMessaging(activity)

        verify { activityLauncher.launchSecureMessagingWelcomeScreen(activity) }
    }

    @Test
    fun `startSecureMessaging launches secure chat when there are pending secure conversations`() {
        every { hasPendingSecureConversationsWithTimeoutUseCase() } returns Single.just(true)

        engagementLauncher.startSecureMessaging(activity)

        verify { activityLauncher.launchChat(activity, Intention.SC_CHAT) }
    }
}
