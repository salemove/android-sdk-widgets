package com.glia.widgets.launcher

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.widgets.chat.Intention
import com.glia.widgets.core.secureconversations.domain.HasOngoingSecureConversationUseCase
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.engagement.domain.EndEngagementUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
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
    private lateinit var isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase

    @MockK(relaxUnitFun = true)
    private lateinit var endEngagementUseCase: EndEngagementUseCase

    @MockK(relaxUnitFun = true)
    private lateinit var controllerFactory: ControllerFactory

    @MockK
    private lateinit var activity: Activity

    @MockK(relaxUnitFun = true)
    private lateinit var configurationManager: ConfigurationManager

    private lateinit var engagementLauncher: EngagementLauncherImpl

    private val visitorContextAssetId = "visitor_context_asset_id"


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        engagementLauncher = EngagementLauncherImpl(
            activityLauncher,
            hasOngoingSecureConversationUseCase,
            isQueueingOrLiveEngagementUseCase,
            endEngagementUseCase,
            configurationManager,
            controllerFactory
        )
    }

    @Test
    fun `startChat launches live chat when no pending secure conversations`() {
        mockOngoingInteractionCallback(false)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns false

        engagementLauncher.startChat(activity)

        verify { activityLauncher.launchChat(activity, Intention.LIVE_CHAT) }
        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startChat launches secure conversation dialog when there are pending secure conversations`() {
        mockOngoingInteractionCallback(true)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns false

        engagementLauncher.startChat(activity, visitorContextAssetId)

        verify { activityLauncher.launchChat(activity, Intention.SC_DIALOG_ENQUEUE_FOR_TEXT) }
        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
    }

    @Test
    fun `startAudioCall launches audio call when no pending secure conversations`() {
        mockOngoingInteractionCallback(false)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns false

        engagementLauncher.startAudioCall(activity)

        verify { activityLauncher.launchCall(activity, Engagement.MediaType.AUDIO, false) }
        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startAudioCall launches secure conversation audio dialog when there are pending secure conversations`() {
        mockOngoingInteractionCallback(true)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns false

        engagementLauncher.startAudioCall(activity, visitorContextAssetId)

        verify { activityLauncher.launchChat(activity, Intention.SC_DIALOG_START_AUDIO) }
        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
    }

    @Test
    fun `startVideoCall launches video call when no pending secure conversations`() {
        mockOngoingInteractionCallback(false)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns false

        engagementLauncher.startVideoCall(activity)

        verify { activityLauncher.launchCall(activity, Engagement.MediaType.VIDEO, false) }
        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startVideoCall launches secure conversation video dialog when there are pending secure conversations`() {
        mockOngoingInteractionCallback(true)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns false

        engagementLauncher.startVideoCall(activity, visitorContextAssetId)

        verify { activityLauncher.launchChat(activity, Intention.SC_DIALOG_START_VIDEO) }
        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
    }

    @Test
    fun `startSecureMessaging launches secure messaging welcome screen when no pending secure conversations`() {
        mockOngoingInteractionCallback(false)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns false

        engagementLauncher.startSecureMessaging(activity)

        verify { activityLauncher.launchSecureMessagingWelcomeScreen(activity) }
        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(any()) }
    }

    @Test
    fun `startSecureMessaging launches secure chat when there are pending secure conversations`() {
        mockOngoingInteractionCallback(true)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns false

        engagementLauncher.startSecureMessaging(activity, visitorContextAssetId)

        verify { activityLauncher.launchChat(activity, Intention.SC_CHAT) }
        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
    }

    private fun mockOngoingInteractionCallback(hasOngoingInteraction: Boolean) {
        every { hasOngoingSecureConversationUseCase(captureLambda()) } answers {
            firstArg<(Boolean) -> Unit>().invoke(hasOngoingInteraction)
        }
    }

    @Test
    fun `startChat ends engagement and destroys chat controller when queueing`() {
        mockOngoingInteractionCallback(false)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns true

        engagementLauncher.startChat(activity)

        verify { endEngagementUseCase() }
        verify { controllerFactory.destroyChatController() }
        verify { activityLauncher.launchChat(activity, Intention.LIVE_CHAT) }
    }

    @Test
    fun `startAudioCall ends engagement and destroys call controller when queueing`() {
        mockOngoingInteractionCallback(false)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns true

        engagementLauncher.startAudioCall(activity)

        verify { endEngagementUseCase() }
        verify { controllerFactory.destroyCallController() }
        verify { activityLauncher.launchCall(activity, Engagement.MediaType.AUDIO, false) }
    }

    @Test
    fun `startVideoCall ends engagement and destroys call controller when queueing`() {
        mockOngoingInteractionCallback(false)
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns true

        engagementLauncher.startVideoCall(activity)

        verify { endEngagementUseCase() }
        verify { controllerFactory.destroyCallController() }
        verify { activityLauncher.launchCall(activity, Engagement.MediaType.VIDEO, false) }
    }
}
