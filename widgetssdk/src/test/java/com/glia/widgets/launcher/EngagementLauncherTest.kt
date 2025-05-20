package com.glia.widgets.launcher

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.widgets.chat.Intention
import com.glia.widgets.internal.secureconversations.domain.HasOngoingSecureConversationUseCase
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.Logger
import com.glia.widgets.view.dialog.UiComponentsDispatcher
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.After
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
    private lateinit var engagementTypeUseCase: EngagementTypeUseCase

    @MockK(relaxUnitFun = true)
    private lateinit var uiComponentsDispatcher: UiComponentsDispatcher

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
            configurationManager,
            uiComponentsDispatcher,
            hasOngoingSecureConversationUseCase,
            isQueueingOrLiveEngagementUseCase,
            engagementTypeUseCase,
        )
        Logger.setIsDebug(false)
    }

    @After
    fun tearDown() {
        confirmVerified(
            activityLauncher,
            hasOngoingSecureConversationUseCase,
            isQueueingOrLiveEngagementUseCase,
            engagementTypeUseCase,
            configurationManager
        )
    }

    //Start Chat

    @Test
    fun `startChat shows already in call snackBar when CV is ongoing`() {
        mockConditions(isCallVisualizer = true)

        engagementLauncher.startChat(activity, visitorContextAssetId)

        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 1) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { engagementTypeUseCase.isMediaEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueing }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_ENQUEUE_FOR_TEXT) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.LIVE_CHAT) }
    }

    @Test
    fun `startChat restores chat screen when has ongoing Live engagement`() {
        mockConditions(hasOngoingLiveEngagement = true)

        engagementLauncher.startChat(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { isQueueingOrLiveEngagementUseCase.isQueueing }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_ENQUEUE_FOR_TEXT) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.LIVE_CHAT) }
    }

    @Test
    fun `startChat restores chat screen when has queueing for Live chat`() {
        mockConditions(isQueueing = true)

        engagementLauncher.startChat(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { isQueueingOrLiveEngagementUseCase.isQueueing }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_ENQUEUE_FOR_TEXT) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.LIVE_CHAT) }
    }

    @Test
    fun `startChat launches secure conversation dialog when has pending secure conversation`() {
        mockConditions(hasOngoingInteraction = true)

        engagementLauncher.startChat(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { isQueueingOrLiveEngagementUseCase.isQueueing }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify { activityLauncher.launchChat(any(), Intention.SC_DIALOG_ENQUEUE_FOR_TEXT) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.LIVE_CHAT) }
    }

    @Test
    fun `startChat launches live chat when no pending secure conversations`() {
        mockConditions(hasOngoingInteraction = false)

        engagementLauncher.startChat(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { isQueueingOrLiveEngagementUseCase.isQueueing }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_ENQUEUE_FOR_TEXT) }
        verify { activityLauncher.launchChat(any(), Intention.LIVE_CHAT) }
    }

    //Start Audio Call
    @Test
    fun `startAudioCall shows already in call snackBar when CV is ongoing`() {
        mockConditions(isCallVisualizer = true)

        engagementLauncher.startAudioCall(activity, visitorContextAssetId)

        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 1) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { engagementTypeUseCase.isMediaEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { activityLauncher.launchCall(any(), null, false) }
        verify(exactly = 0) { engagementTypeUseCase.isChatEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_AUDIO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.AUDIO, false) }
    }

    @Test
    fun `startAudioCall restores call screen when has ongoing Media engagement`() {
        mockConditions(isMediaEngagement = true)

        engagementLauncher.startAudioCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify { activityLauncher.launchCall(any(), null, false) }
        verify(exactly = 0) { engagementTypeUseCase.isChatEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_AUDIO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.AUDIO, false) }
    }

    @Test
    fun `startAudioCall restores call screen when queueing for audio`() {
        mockConditions(isQueueingForMedia = true)

        engagementLauncher.startAudioCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify { activityLauncher.launchCall(any(), null, false) }
        verify(exactly = 0) { engagementTypeUseCase.isChatEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_AUDIO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.AUDIO, false) }
    }

    @Test
    fun `startAudioCall shows already in CV SnackBar when has ongoing Live engagement`() {
        mockConditions(isChatEngagement = true)

        engagementLauncher.startAudioCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { activityLauncher.launchCall(any(), null, false) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 1) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_AUDIO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.AUDIO, false) }
    }

    @Test
    fun `startAudioCall shows already in CV SnackBar when queueing for a live engagement`() {
        mockConditions(isQueueingForLiveChat = true)

        engagementLauncher.startAudioCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { activityLauncher.launchCall(any(), null, false) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 1) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_AUDIO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.AUDIO, false) }
    }

    @Test
    fun `startAudioCall launches secure conversation dialog when has pending secure conversation`() {
        mockConditions(hasOngoingInteraction = true)

        engagementLauncher.startAudioCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { activityLauncher.launchCall(any(), null, false) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_AUDIO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.AUDIO, false) }
    }

    @Test
    fun `startAudioCall launches call screen when no pending secure conversations`() {
        mockConditions(hasOngoingInteraction = false)

        engagementLauncher.startAudioCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { activityLauncher.launchCall(any(), null, false) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_AUDIO) }
        verify { activityLauncher.launchCall(any(), Engagement.MediaType.AUDIO, false) }
    }

    //Start Video Call
    @Test
    fun `startVideoCall shows already in call snackBar when CV is ongoing`() {
        mockConditions(isCallVisualizer = true)

        engagementLauncher.startVideoCall(activity, visitorContextAssetId)

        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify { engagementTypeUseCase.hasVideo }
        verify(exactly = 1) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { engagementTypeUseCase.isMediaEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { activityLauncher.launchCall(any(), null, false) }
        verify(exactly = 0) { engagementTypeUseCase.isChatEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_VIDEO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.VIDEO, false) }
    }

    @Test
    fun `startVideoCall opens Call screen when CV with video is ongoing`() {
        mockConditions(isCallVisualizer = true, hasVideo = true, hasOngoingInteraction = false)

        engagementLauncher.startVideoCall(activity, visitorContextAssetId)

        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify { engagementTypeUseCase.hasVideo }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { activityLauncher.launchCall(any(), null, false) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_VIDEO) }
        verify { activityLauncher.launchCall(any(), Engagement.MediaType.VIDEO, false) }
    }

    @Test
    fun `startVideoCall opens call screen when has ongoing Media engagement`() {
        mockConditions(isMediaEngagement = true)

        engagementLauncher.startVideoCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { engagementTypeUseCase.hasVideo }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify { activityLauncher.launchCall(any(), null, false) }
        verify(exactly = 0) { engagementTypeUseCase.isChatEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_VIDEO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.VIDEO, false) }
    }

    @Test
    fun `startVideoCall opens call screen when queueing for video`() {
        mockConditions(isQueueingForMedia = true)

        engagementLauncher.startVideoCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { engagementTypeUseCase.hasVideo }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify { activityLauncher.launchCall(any(), null, false) }
        verify(exactly = 0) { engagementTypeUseCase.isChatEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_VIDEO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.VIDEO, false) }
    }

    @Test
    fun `startVideoCall restores chat screen when has ongoing Live engagement`() {
        mockConditions(isChatEngagement = true)

        engagementLauncher.startVideoCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { engagementTypeUseCase.hasVideo }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { activityLauncher.launchCall(any(), null, false) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 1) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_VIDEO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.VIDEO, false) }
    }

    @Test
    fun `startVideoCall restores chat screen when queueing for chat`() {
        mockConditions(isQueueingForLiveChat = true)

        engagementLauncher.startVideoCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { engagementTypeUseCase.hasVideo }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { activityLauncher.launchCall(any(), null, false) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 1) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_VIDEO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.VIDEO, false) }
    }

    @Test
    fun `startVideoCall launches secure conversation dialog when has pending secure conversation`() {
        mockConditions(hasOngoingInteraction = true)

        engagementLauncher.startVideoCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { engagementTypeUseCase.hasVideo }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { activityLauncher.launchCall(any(), null, false) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_VIDEO) }
        verify(exactly = 0) { activityLauncher.launchCall(any(), Engagement.MediaType.VIDEO, false) }
    }

    @Test
    fun `startVideoCall launches call screen when no pending secure conversations`() {
        mockConditions(hasOngoingInteraction = false)

        engagementLauncher.startVideoCall(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { engagementTypeUseCase.hasVideo }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { activityLauncher.launchCall(any(), null, false) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_DIALOG_START_VIDEO) }
        verify { activityLauncher.launchCall(any(), Engagement.MediaType.VIDEO, false) }
    }

    //Start Secure Messaging
    @Test
    fun `startSecureMessaging shows already in call snackBar when CV is ongoing`() {
        mockConditions(isCallVisualizer = true)

        engagementLauncher.startSecureMessaging(activity, visitorContextAssetId)

        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 1) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { engagementTypeUseCase.isChatEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify(exactly = 0) { engagementTypeUseCase.isMediaEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_CHAT) }
        verify(exactly = 0) { activityLauncher.launchSecureMessagingWelcomeScreen(any()) }
    }

    @Test
    fun `startSecureMessaging restores chat screen when has ongoing Live engagement`() {
        mockConditions(isChatEngagement = true)

        engagementLauncher.startSecureMessaging(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify(exactly = 0) { engagementTypeUseCase.isMediaEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_CHAT) }
        verify(exactly = 0) { activityLauncher.launchSecureMessagingWelcomeScreen(any()) }
    }

    @Test
    fun `startSecureMessaging restores chat screen when is queueing for Live chat`() {
        mockConditions(isQueueingForLiveChat = true)

        engagementLauncher.startSecureMessaging(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify(exactly = 0) { engagementTypeUseCase.isMediaEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_CHAT) }
        verify(exactly = 0) { activityLauncher.launchSecureMessagingWelcomeScreen(any()) }
    }

    @Test
    fun `startSecureMessaging shows snackbar when queueing for media engagement`() {
        mockConditions(isQueueingForMedia = true)

        engagementLauncher.startSecureMessaging(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 1) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_CHAT) }
        verify(exactly = 0) { activityLauncher.launchSecureMessagingWelcomeScreen(any()) }
    }

    @Test
    fun `startSecureMessaging shows snackbar when has media engagement`() {
        mockConditions(isMediaEngagement = true)

        engagementLauncher.startSecureMessaging(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 1) { uiComponentsDispatcher.showSnackBar(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_CHAT) }
        verify(exactly = 0) { activityLauncher.launchSecureMessagingWelcomeScreen(any()) }
    }

    @Test
    fun `startSecureMessaging launches secure conversation screen when has pending secure conversation`() {
        mockConditions(hasOngoingInteraction = true)

        engagementLauncher.startSecureMessaging(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify { activityLauncher.launchChat(any(), Intention.SC_CHAT) }
        verify(exactly = 0) { activityLauncher.launchSecureMessagingWelcomeScreen(any()) }
    }

    @Test
    fun `startSecureMessaging launches SC welcome screen when no pending secure conversations`() {
        mockConditions(hasOngoingInteraction = false)

        engagementLauncher.startSecureMessaging(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }
        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { engagementTypeUseCase.isChatEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.RETURN_TO_CHAT) }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.isQueueingForMedia }
        verify(exactly = 0) { uiComponentsDispatcher.showSnackBar(any()) }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(any(), Intention.SC_CHAT) }
        verify { activityLauncher.launchSecureMessagingWelcomeScreen(any()) }
    }

    private fun mockConditions(
        isQueueingForLiveChat: Boolean = false,
        isQueueingForMedia: Boolean = false,
        isQueueing: Boolean = false,
        hasOngoingLiveEngagement: Boolean = false,
        isChatEngagement: Boolean = false,
        isMediaEngagement: Boolean = false,
        hasOngoingInteraction: Boolean? = null,
        isCallVisualizer: Boolean = false,
        hasVideo: Boolean = false,
    ) {
        every { isQueueingOrLiveEngagementUseCase.isQueueingForLiveChat } returns isQueueingForLiveChat
        every { isQueueingOrLiveEngagementUseCase.isQueueingForMedia } returns isQueueingForMedia
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns isQueueing
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns hasOngoingLiveEngagement
        every { engagementTypeUseCase.isCallVisualizer } returns isCallVisualizer
        every { engagementTypeUseCase.isMediaEngagement } returns isMediaEngagement
        every { engagementTypeUseCase.isChatEngagement } returns isChatEngagement
        every { engagementTypeUseCase.hasVideo } returns hasVideo

        if (hasOngoingInteraction ?: return) {
            every { hasOngoingSecureConversationUseCase(captureLambda(), any()) } answers {
                firstArg<() -> Unit>().invoke()
            }
        } else {
            every { hasOngoingSecureConversationUseCase(any(), captureLambda()) } answers {
                secondArg<() -> Unit>().invoke()
            }
        }
    }
}
