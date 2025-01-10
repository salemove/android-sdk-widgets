package com.glia.widgets.launcher

import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.widgets.callvisualizer.controller.CallVisualizerContract
import com.glia.widgets.chat.Intention
import com.glia.widgets.core.secureconversations.domain.HasOngoingSecureConversationUseCase
import com.glia.widgets.engagement.domain.EndEngagementUseCase
import com.glia.widgets.engagement.domain.EngagementTypeUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.helper.Logger
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(FeatureUnderConsiderationForRemoval::class)
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
    private lateinit var engagementTypeUseCase: EngagementTypeUseCase

    @MockK(relaxUnitFun = true)
    private lateinit var callVisualizerController: CallVisualizerContract.Controller

    @MockK
    private lateinit var activity: Activity

    @MockK(relaxUnitFun = true)
    private lateinit var configurationManager: ConfigurationManager

    private lateinit var destroyChatControllerCallback: () -> Unit
    private lateinit var destroyCallControllerCallback: () -> Unit

    private lateinit var engagementLauncher: EngagementLauncherImpl

    private val visitorContextAssetId = "visitor_context_asset_id"


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        destroyChatControllerCallback = mockk(relaxed = true)
        destroyCallControllerCallback = mockk(relaxed = true)
        engagementLauncher = EngagementLauncherImpl(
            activityLauncher,
            hasOngoingSecureConversationUseCase,
            isQueueingOrLiveEngagementUseCase,
            endEngagementUseCase,
            configurationManager,
            engagementTypeUseCase,
            callVisualizerController,
            destroyChatControllerCallback,
            destroyCallControllerCallback
        )
        Logger.setIsDebug(false)
    }

    @After
    fun tearDown() {
        confirmVerified(
            activityLauncher,
            hasOngoingSecureConversationUseCase,
            isQueueingOrLiveEngagementUseCase,
            endEngagementUseCase,
            engagementTypeUseCase,
            callVisualizerController,
            configurationManager
        )
    }

    //Start Chat

    @Test
    fun `startChat shows already in call snackBar when CV is ongoing`() {
        mockConditions(isQueueing = true, isCallVisualizer = true, hasOngoingLiveEngagement = true)

        engagementLauncher.startChat(activity, visitorContextAssetId)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify { endEngagementUseCase() }
        verify { destroyChatControllerCallback() }

        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify { callVisualizerController.showAlreadyInCallSnackBar() }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify(exactly = 0) { activityLauncher.launchChat(any(), any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
    }

    @Test
    fun `startChat restores chat screen when has ongoing Live engagement`() {
        mockConditions(hasOngoingLiveEngagement = true)

        engagementLauncher.startChat(activity)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify(exactly = 0) { endEngagementUseCase() }
        verify(exactly = 0) { destroyChatControllerCallback() }

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { activityLauncher.launchChat(eq(activity), eq(Intention.RETURN_TO_CHAT)) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
    }

    @Test
    fun `startChat launches secure conversation dialog when has pending secure conversation`() {
        mockConditions(hasOngoingInteraction = true)

        engagementLauncher.startChat(activity)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify(exactly = 0) { endEngagementUseCase() }
        verify(exactly = 0) { destroyChatControllerCallback() }

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify { activityLauncher.launchChat(eq(activity), eq(Intention.SC_DIALOG_ENQUEUE_FOR_TEXT)) }
    }

    @Test
    fun `startChat launches live chat when no pending secure conversations`() {
        mockConditions(hasOngoingInteraction = false)

        engagementLauncher.startChat(activity)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify(exactly = 0) { endEngagementUseCase() }
        verify(exactly = 0) { destroyChatControllerCallback() }

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify { activityLauncher.launchChat(eq(activity), eq(Intention.LIVE_CHAT)) }
    }

    //Start Audio Call
    @Test
    fun `startAudioCall shows already in call snackBar when CV is ongoing`() {
        mockConditions(isQueueing = true, isCallVisualizer = true, hasOngoingLiveEngagement = true)

        engagementLauncher.startAudioCall(activity, visitorContextAssetId)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify { endEngagementUseCase() }
        verify { destroyCallControllerCallback() }

        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify { callVisualizerController.showAlreadyInCallSnackBar() }
        verify(exactly = 0) { engagementTypeUseCase.isMediaEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify(exactly = 0) { activityLauncher.launchCall(any(), any(), any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
    }

    @Test
    fun `startAudioCall restores call screen when has ongoing Media engagement`() {
        mockConditions(isMediaEngagement = true)

        engagementLauncher.startAudioCall(activity)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify(exactly = 0) { endEngagementUseCase() }
        verify(exactly = 0) { destroyChatControllerCallback() }

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { activityLauncher.launchCall(eq(activity), isNull(), eq(false)) }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
    }

    @Test
    fun `startAudioCall restores chat screen when has ongoing Live engagement`() {
        mockConditions(hasOngoingLiveEngagement = true)

        engagementLauncher.startAudioCall(activity)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify(exactly = 0) { endEngagementUseCase() }
        verify(exactly = 0) { destroyChatControllerCallback() }

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { activityLauncher.launchChat(eq(activity), eq(Intention.RETURN_TO_CHAT)) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
    }

    @Test
    fun `startAudioCall launches secure conversation dialog when has pending secure conversation`() {
        mockConditions(hasOngoingInteraction = true)

        engagementLauncher.startAudioCall(activity)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify(exactly = 0) { endEngagementUseCase() }
        verify(exactly = 0) { destroyChatControllerCallback() }

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify { activityLauncher.launchChat(eq(activity), eq(Intention.SC_DIALOG_START_AUDIO)) }
    }

    @Test
    fun `startAudioCall launches call screen when no pending secure conversations`() {
        mockConditions(hasOngoingInteraction = false)

        engagementLauncher.startAudioCall(activity)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify(exactly = 0) { endEngagementUseCase() }
        verify(exactly = 0) { destroyChatControllerCallback() }

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify { activityLauncher.launchCall(eq(activity), eq(Engagement.MediaType.AUDIO), eq(false)) }
    }

    //Start Video Call
    @Test
    fun `startVideoCall shows already in call snackBar when CV is ongoing`() {
        mockConditions(isQueueing = true, isCallVisualizer = true, hasOngoingLiveEngagement = true)

        engagementLauncher.startVideoCall(activity, visitorContextAssetId)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify { endEngagementUseCase() }
        verify { destroyCallControllerCallback() }

        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify { engagementTypeUseCase.hasVideo }
        verify { callVisualizerController.showAlreadyInCallSnackBar() }
        verify(exactly = 0) { engagementTypeUseCase.isMediaEngagement }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify(exactly = 0) { activityLauncher.launchCall(any(), any(), any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
    }

    @Test
    fun `startVideoCall restores call screen when has ongoing Media engagement`() {
        mockConditions(isMediaEngagement = true, isCallVisualizer = true, hasVideo = true)

        engagementLauncher.startVideoCall(activity)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify(exactly = 0) { endEngagementUseCase() }
        verify(exactly = 0) { destroyChatControllerCallback() }

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify { engagementTypeUseCase.hasVideo }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { activityLauncher.launchCall(eq(activity), isNull(), eq(false)) }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
    }

    @Test
    fun `startVideoCall restores chat screen when has ongoing Live engagement`() {
        mockConditions(hasOngoingLiveEngagement = true)

        engagementLauncher.startVideoCall(activity)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify(exactly = 0) { endEngagementUseCase() }
        verify(exactly = 0) { destroyChatControllerCallback() }

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { activityLauncher.launchChat(eq(activity), eq(Intention.RETURN_TO_CHAT)) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
    }

    @Test
    fun `startVideoCall launches secure conversation dialog when has pending secure conversation`() {
        mockConditions(hasOngoingInteraction = true)

        engagementLauncher.startVideoCall(activity)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify(exactly = 0) { endEngagementUseCase() }
        verify(exactly = 0) { destroyChatControllerCallback() }

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify { activityLauncher.launchChat(eq(activity), eq(Intention.SC_DIALOG_START_VIDEO)) }
    }

    @Test
    fun `startVideoCall launches call screen when no pending secure conversations`() {
        mockConditions(hasOngoingInteraction = false)

        engagementLauncher.startVideoCall(activity)

        verify { isQueueingOrLiveEngagementUseCase.isQueueing }

        verify(exactly = 0) { endEngagementUseCase() }
        verify(exactly = 0) { destroyChatControllerCallback() }

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { engagementTypeUseCase.isMediaEngagement }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify { activityLauncher.launchCall(eq(activity), eq(Engagement.MediaType.VIDEO), eq(false)) }
    }


    @Test
    fun `startSecureMessaging shows already in call snackBar when CV is ongoing`() {
        mockConditions(isCallVisualizer = true, hasOngoingLiveEngagement = true)

        engagementLauncher.startSecureMessaging(activity, visitorContextAssetId)

        verify { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify { callVisualizerController.showAlreadyInCallSnackBar() }
        verify(exactly = 0) { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify(exactly = 0) { activityLauncher.launchChat(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchSecureMessagingWelcomeScreen(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
    }

    @Test
    fun `startSecureMessaging restores chat screen when has ongoing Live engagement`() {
        mockConditions(hasOngoingLiveEngagement = true)

        engagementLauncher.startSecureMessaging(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { activityLauncher.launchChat(eq(activity), eq(Intention.RETURN_TO_CHAT)) }
        verify(exactly = 0) { activityLauncher.launchSecureMessagingWelcomeScreen(any()) }
        verify(exactly = 0) { hasOngoingSecureConversationUseCase(any(), any()) }
    }

    @Test
    fun `startSecureMessaging launches secure conversation screen when has pending secure conversation`() {
        mockConditions(hasOngoingInteraction = true)

        engagementLauncher.startSecureMessaging(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify { activityLauncher.launchChat(eq(activity), eq(Intention.SC_CHAT)) }
        verify(exactly = 0) { activityLauncher.launchSecureMessagingWelcomeScreen(any()) }
    }

    @Test
    fun `startSecureMessaging launches SC welcome screen when no pending secure conversations`() {
        mockConditions(hasOngoingInteraction = false)

        engagementLauncher.startSecureMessaging(activity)

        verify(exactly = 0) { configurationManager.setVisitorContextAssetId(eq(visitorContextAssetId)) }

        verify { engagementTypeUseCase.isCallVisualizer }
        verify(exactly = 0) { callVisualizerController.showAlreadyInCallSnackBar() }
        verify { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement }
        verify { hasOngoingSecureConversationUseCase(any(), any()) }
        verify(exactly = 0) { activityLauncher.launchChat(eq(activity), eq(Intention.SC_CHAT)) }
        verify { activityLauncher.launchSecureMessagingWelcomeScreen(eq(activity)) }
    }

    private fun mockConditions(
        isQueueing: Boolean = false,
        hasOngoingLiveEngagement: Boolean = false,
        isCallVisualizer: Boolean = false,
        isMediaEngagement: Boolean = false,
        hasVideo: Boolean = false,
        hasOngoingInteraction: Boolean? = null
    ) {
        every { isQueueingOrLiveEngagementUseCase.isQueueing } returns isQueueing
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns hasOngoingLiveEngagement
        every { engagementTypeUseCase.isCallVisualizer } returns isCallVisualizer
        every { engagementTypeUseCase.isMediaEngagement } returns isMediaEngagement
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
