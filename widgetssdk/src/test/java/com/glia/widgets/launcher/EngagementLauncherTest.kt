package com.glia.widgets.launcher

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.widgets.chat.Intention
import com.glia.widgets.core.secureconversations.domain.HasOngoingSecureConversationUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class EngagementLauncherImplTest {

    @MockK(relaxUnitFun = true)
    private lateinit var activityLauncher: ActivityLauncher

    @MockK
    private lateinit var hasOngoingSecureConversationUseCase: HasOngoingSecureConversationUseCase

    @MockK
    private lateinit var activity: Activity

    @MockK(relaxUnitFun = true)
    private lateinit var configurationManager: ConfigurationManager

    private lateinit var engagementLauncher: EngagementLauncherImpl

    private val visitorContextAssetId = "visitor_context_asset_id"


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        engagementLauncher = EngagementLauncherImpl(activityLauncher, hasOngoingSecureConversationUseCase, configurationManager)
    }

    @Test
    fun `startChat launches live chat when no pending secure conversations`() {
        mockOngoingInteractionCallback(false)

        engagementLauncher.startChat(activity)

        verify { activityLauncher.launchChat(activity, Intention.LIVE_CHAT) }
        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startChat launches secure conversation dialog when there are pending secure conversations`() {
        mockOngoingInteractionCallback(true)

        engagementLauncher.startChat(activity, visitorContextAssetId)

        verify { activityLauncher.launchChat(activity, Intention.SC_DIALOG_ENQUEUE_FOR_TEXT) }
        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
    }

    @Test
    fun `startAudioCall launches audio call when no pending secure conversations`() {
        mockOngoingInteractionCallback(false)

        engagementLauncher.startAudioCall(activity)

        verify { activityLauncher.launchCall(activity, Engagement.MediaType.AUDIO, false) }
        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startAudioCall launches secure conversation audio dialog when there are pending secure conversations`() {
        mockOngoingInteractionCallback(true)

        engagementLauncher.startAudioCall(activity, visitorContextAssetId)

        verify { activityLauncher.launchChat(activity, Intention.SC_DIALOG_START_AUDIO) }
        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
    }

    @Test
    fun `startVideoCall launches video call when no pending secure conversations`() {
        mockOngoingInteractionCallback(false)

        engagementLauncher.startVideoCall(activity)

        verify { activityLauncher.launchCall(activity, Engagement.MediaType.VIDEO, false) }
        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startVideoCall launches secure conversation video dialog when there are pending secure conversations`() {
        mockOngoingInteractionCallback(true)

        engagementLauncher.startVideoCall(activity, visitorContextAssetId)

        verify { activityLauncher.launchChat(activity, Intention.SC_DIALOG_START_VIDEO) }
        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
    }

    @Test
    fun `startSecureMessaging launches secure messaging welcome screen when no pending secure conversations`() {
        mockOngoingInteractionCallback(false)

        engagementLauncher.startSecureMessaging(activity)

        verify { activityLauncher.launchSecureMessagingWelcomeScreen(activity) }
        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startSecureMessaging launches secure chat when there are pending secure conversations`() {
        mockOngoingInteractionCallback(true)

        engagementLauncher.startSecureMessaging(activity, visitorContextAssetId)

        verify { activityLauncher.launchChat(activity, Intention.SC_CHAT) }
        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
    }

    private fun mockOngoingInteractionCallback(hasOngoingInteraction: Boolean) {
        every { hasOngoingSecureConversationUseCase(captureLambda()) } answers {
            firstArg<(Boolean) -> Unit>().invoke(hasOngoingInteraction)
        }
    }
}
