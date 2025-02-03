package com.glia.widgets.engagement

import android.LOGGER_PATH
import android.app.Activity
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.EngagementRequest
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.IncomingEngagementRequest
import com.glia.androidsdk.Operator
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.OperatorTypingStatus
import com.glia.androidsdk.comms.Audio
import com.glia.androidsdk.comms.CameraDevice
import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.comms.OperatorMediaState
import com.glia.androidsdk.comms.Video
import com.glia.androidsdk.comms.VisitorMediaState
import com.glia.androidsdk.engagement.EngagementState
import com.glia.androidsdk.engagement.Survey
import com.glia.androidsdk.omnibrowse.Omnibrowse
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement
import com.glia.androidsdk.omnicore.OmnicoreEngagement
import com.glia.androidsdk.queuing.QueueTicket
import com.glia.androidsdk.screensharing.LocalScreen
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.androidsdk.screensharing.ScreenSharingRequest
import com.glia.androidsdk.screensharing.VisitorScreenSharingState
import com.glia.widgets.core.engagement.GliaOperatorRepository
import com.glia.widgets.core.queue.QueueRepository
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.formattedName
import com.glia.widgets.launcher.ConfigurationManager
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.function.Consumer

class EngagementRepositoryTest {
    @MockK(relaxUnitFun = true)
    private lateinit var core: GliaCore

    @MockK(relaxUnitFun = true)
    private lateinit var operatorRepository: GliaOperatorRepository

    @MockK(relaxUnitFun = true)
    private lateinit var callVisualizer: Omnibrowse

    //callbacks
    private lateinit var engagementRequestCallbackSlot: CapturingSlot<Consumer<IncomingEngagementRequest>>
    private lateinit var omniCoreEngagementCallbackSlot: CapturingSlot<Consumer<OmnicoreEngagement>>
    private lateinit var callVisualizerEngagementCallbackSlot: CapturingSlot<Consumer<OmnibrowseEngagement>>
    private lateinit var engagementStateCallbackSlot: CapturingSlot<Consumer<EngagementState>>
    private lateinit var engagementEndCallbackSlot: CapturingSlot<Runnable>
    private lateinit var queueTicketCallbackSlot: CapturingSlot<Consumer<QueueTicket>>
    private lateinit var mediaUpgradeOfferCallbackSlot: CapturingSlot<Consumer<MediaUpgradeOffer>>
    private lateinit var operatorMediaStateUpdateCallbackSlot: CapturingSlot<Consumer<OperatorMediaState>>
    private lateinit var visitorMediaStateUpdateCallbackSlot: CapturingSlot<Consumer<VisitorMediaState>>
    private lateinit var operatorTypingCallbackSlot: CapturingSlot<Consumer<OperatorTypingStatus>>
    private lateinit var screenSharingRequestCallbackSlot: CapturingSlot<Consumer<ScreenSharingRequest>>
    private lateinit var screenSharingRequestResponseCallbackSlot: CapturingSlot<Consumer<GliaException>>
    private lateinit var screenSharingStateCallbackSlot: CapturingSlot<Consumer<VisitorScreenSharingState>>

    private lateinit var engagement: Engagement
    private lateinit var media: Media
    private lateinit var chat: Chat
    private lateinit var screenSharing: ScreenSharing
    private lateinit var operator: Operator
    private lateinit var engagementState: EngagementState
    private lateinit var cameraDevice: CameraDevice

    private lateinit var queueRepository: QueueRepository

    private lateinit var repository: EngagementRepository
    private lateinit var configurationManager: ConfigurationManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockLogger()

        queueRepository = mockk(relaxUnitFun = true)

        engagementRequestCallbackSlot = slot()
        omniCoreEngagementCallbackSlot = slot()
        callVisualizerEngagementCallbackSlot = slot()
        engagementStateCallbackSlot = slot()
        engagementEndCallbackSlot = slot()
        queueTicketCallbackSlot = slot()
        mediaUpgradeOfferCallbackSlot = slot()
        operatorMediaStateUpdateCallbackSlot = slot()
        visitorMediaStateUpdateCallbackSlot = slot()
        operatorTypingCallbackSlot = slot()
        screenSharingRequestCallbackSlot = slot()
        screenSharingRequestResponseCallbackSlot = slot()
        screenSharingStateCallbackSlot = slot()

        every { core.callVisualizer } returns callVisualizer
        configurationManager = mockk<ConfigurationManager> {
            every { visitorContextAssetId } returns null
        }

        repository = EngagementRepositoryImpl(core, operatorRepository, queueRepository, configurationManager)
        initializeAndVerify()
    }

    private fun mockLogger() {
        Logger.setIsDebug(false)
        mockkStatic(LOGGER_PATH)
        every { Logger.i(any(), any()) } just Runs
        every { Logger.i(any(), any(), any()) } just Runs

        every { Logger.d(any(), any()) } just Runs
        every { Logger.d(any(), any(), any()) } just Runs

        every { Logger.e(any(), any()) } just Runs
        every { Logger.e(any(), any(), any()) } just Runs
    }

    private fun initializeAndVerify() {
        val engagementRequestTest = repository.engagementRequest.test()
        val engagementStateTest = repository.engagementState.test()
        val surveyTest = repository.survey.test()
        val currentOperatorTest = repository.currentOperator.test()
        val operatorTypingStatusTest = repository.operatorTypingStatus.test()
        val mediaUpgradeOfferTest = repository.mediaUpgradeOffer.test()
        val mediaUpgradeOfferAcceptResultTest = repository.mediaUpgradeOfferAcceptResult.test()
        val visitorMediaStateTest = repository.visitorMediaState.test()
        val onHoldStateTest = repository.onHoldState.test()
        val operatorMediaStateTest = repository.operatorMediaState.test()
        val screenSharingStateTest = repository.screenSharingState.test()
        val visitorCameraStateTest = repository.visitorCameraState.test()

        repository.initialize()
        verify { core.on(Glia.Events.ENGAGEMENT, capture(omniCoreEngagementCallbackSlot)) }
        verify { core.on(Glia.Events.QUEUE_TICKET, capture(queueTicketCallbackSlot)) }
        verify { callVisualizer.on(Omnibrowse.Events.ENGAGEMENT, capture(callVisualizerEngagementCallbackSlot)) }
        verify { callVisualizer.on(Omnibrowse.Events.ENGAGEMENT_REQUEST, capture(engagementRequestCallbackSlot)) }
        verify(exactly = 2) { core.callVisualizer }

        repository.apply {
            engagementRequestTest.assertNotComplete().assertNoValues()
            engagementStateTest.assertNotComplete().assertValue(State.NoEngagement).assertValueCount(1)
            surveyTest.assertNotComplete().assertNoValues()
            currentOperatorTest.assertNotComplete().assertValue(Data.Empty).assertValueCount(1)
            operatorTypingStatusTest.assertNotComplete().assertNoValues()
            mediaUpgradeOfferTest.assertNotComplete().assertNoValues()
            mediaUpgradeOfferAcceptResultTest.assertNotComplete().assertNoValues()
            visitorMediaStateTest.assertNotComplete().assertValue(Data.Empty).assertValueCount(1)
            assertNull(visitorCurrentMediaState)
            onHoldStateTest.assertNotComplete().assertValue(false).assertValueCount(1)
            operatorMediaStateTest.assertNotComplete().assertValue(Data.Empty).assertValueCount(1)
            assertNull(currentOperatorValue)
            assertNull(operatorCurrentMediaState)
            screenSharingStateTest.assertNotComplete().assertNoValues()
            assertFalse(isQueueingOrEngagement)
            assertFalse(hasOngoingEngagement)
            assertFalse(isQueueing)
            assertFalse(isQueueingForMedia)
            assertFalse(isCallVisualizerEngagement)
            assertFalse(isOperatorPresent)
            assertFalse(isSharingScreen)
            visitorCameraStateTest.assertNotComplete().assertNoValues()
        }
    }

    private fun mockEngagementAndStart(callVisualizer: Boolean = false) {
        engagement = if (callVisualizer) {
            mockk<OmnibrowseEngagement>(relaxUnitFun = true)
        } else {
            mockk<OmnicoreEngagement>(relaxUnitFun = true)
        }

        media = mockk(relaxUnitFun = true)
        chat = mockk(relaxUnitFun = true)
        screenSharing = mockk(relaxUnitFun = true)
        cameraDevice = mockk(relaxUnitFun = true)

        operator = mockk(relaxUnitFun = true)
        engagementState = mockk(relaxUnitFun = true)
        every { engagementState.operator } returns operator
        every { engagement.state } returns engagementState
        every { engagement.media } returns media
        every { engagement.chat } returns chat
        every { engagement.screenSharing } returns screenSharing
        every { media.currentCameraDevice } returns cameraDevice

        if (callVisualizer) {
            callVisualizerEngagementCallbackSlot.captured.accept(engagement as OmnibrowseEngagement)
            repository.engagementState.test().assertValue(State.StartedCallVisualizer).assertValueCount(1).assertNotComplete()
            verify(inverse = true) { chat.on(Chat.Events.OPERATOR_TYPING_STATUS, capture(operatorTypingCallbackSlot)) }
            assertTrue(repository.isCallVisualizerEngagement)
        } else {
            omniCoreEngagementCallbackSlot.captured.accept(engagement as OmnicoreEngagement)
            repository.engagementState.test().assertValue(State.StartedOmniCore).assertValueCount(1).assertNotComplete()
            verify { chat.on(Chat.Events.OPERATOR_TYPING_STATUS, capture(operatorTypingCallbackSlot)) }
            assertFalse(repository.isCallVisualizerEngagement)
        }

        assertTrue(repository.hasOngoingEngagement)
        assertTrue(repository.isQueueingOrEngagement)

        verify { Logger.i(any(), any()) }
        verify { operatorRepository.emit(operator) }

        verify { engagement.on(Engagement.Events.END, capture(engagementEndCallbackSlot)) }
        verify { engagement.on(Engagement.Events.STATE_UPDATE, capture(engagementStateCallbackSlot)) }

        verify { media.on(Media.Events.MEDIA_UPGRADE_OFFER, capture(mediaUpgradeOfferCallbackSlot)) }
        verify { media.on(Media.Events.OPERATOR_STATE_UPDATE, capture(operatorMediaStateUpdateCallbackSlot)) }
        verify { media.on(Media.Events.VISITOR_STATE_UPDATE, capture(visitorMediaStateUpdateCallbackSlot)) }
        verify { media.currentCameraDevice }

        verify { screenSharing.on(ScreenSharing.Events.SCREEN_SHARING_REQUEST, capture(screenSharingRequestCallbackSlot)) }
        verify { screenSharing.on(ScreenSharing.Events.VISITOR_STATE, capture(screenSharingStateCallbackSlot)) }

        verify { engagement.state }
        verify { engagementState.operator }
        verify { engagement.media }
        verify { engagement.screenSharing }

        if (!callVisualizer) {
            verify { engagement.chat }
        }

        confirmEngagementVerified()
    }

    private fun fillStates() {
        val operatorMediaStateTestObserver = repository.operatorMediaState.test()
        val visitorMediaStateTestObserver = repository.visitorMediaState.test()
        val onHoldTestObserver = repository.onHoldState.test()
        val visitorCameraTestObserver = repository.visitorCameraState.test()
        val operatorMediaState = mockk<OperatorMediaState>()
        operatorMediaStateUpdateCallbackSlot.captured.accept(operatorMediaState)
        assertEquals(operatorMediaState, repository.operatorCurrentMediaState)

        val mockVideo = mockk<Video>(relaxUnitFun = true)
        val mockAudio = mockk<Audio>(relaxUnitFun = true)
        val visitorMediaState = mockk<VisitorMediaState> {
            every { video } returns mockVideo
            every { audio } returns mockAudio
        }
        visitorMediaStateUpdateCallbackSlot.captured.accept(visitorMediaState)
        assertEquals(visitorMediaState, repository.visitorCurrentMediaState)
        verify { visitorMediaState.video }
        verify { visitorMediaState.audio }

        val videoOnHoldSlot = slot<Consumer<Boolean>>()
        val audioOnHoldSlot = slot<Consumer<Boolean>>()

        verify { mockVideo.setOnHoldHandler(capture(videoOnHoldSlot)) }
        verify { mockAudio.setOnHoldHandler(capture(audioOnHoldSlot)) }

        videoOnHoldSlot.captured.accept(true)
        audioOnHoldSlot.captured.accept(true)
        videoOnHoldSlot.captured.accept(false)

        operatorMediaStateTestObserver
            .assertNotComplete()
            .assertValueCount(2)
            .assertValuesOnly(
                Data.Empty,
                Data.Value(operatorMediaState)
            )

        visitorMediaStateTestObserver
            .assertNotComplete()
            .assertValueCount(2)
            .assertValuesOnly(
                Data.Empty,
                Data.Value(visitorMediaState)
            )

        onHoldTestObserver
            .assertNotComplete()
            .assertValueCount(3)
            .assertValuesOnly(
                false,
                true,
                false
            )

        visitorCameraTestObserver
            .assertNotComplete()
            .assertValueCount(2)
            .assertValuesOnly(
                VisitorCamera.Camera(cameraDevice),
                VisitorCamera.Camera(cameraDevice)
            )
    }

    private fun mockVisitorMediaState(testBody: (VisitorMediaState, Audio, Video) -> Unit) {
        val mockVideo = mockk<Video>(relaxUnitFun = true)
        val mockAudio = mockk<Audio>(relaxUnitFun = true)
        val visitorMediaState = mockk<VisitorMediaState> {
            every { video } returns mockVideo
            every { audio } returns mockAudio
        }
        visitorMediaStateUpdateCallbackSlot.captured.accept(visitorMediaState)
        assertEquals(visitorMediaState, repository.visitorCurrentMediaState)
        verify { visitorMediaState.video }
        verify { visitorMediaState.audio }
        verify { mockVideo.setOnHoldHandler(any()) }
        verify { mockAudio.setOnHoldHandler(any()) }
        clearMocks(visitorMediaState, answers = false, childMocks = false)

        testBody(visitorMediaState, mockAudio, mockVideo)
        confirmVerified(mockAudio, mockVideo, visitorMediaState)
    }

    private fun verifyEngagementEnd(ongoingEngagement: Boolean = true, endedLocally: Boolean = true) {
        repository.apply {
            currentOperator.test().assertNotComplete().assertValue(Data.Empty)
            visitorMediaState.test().assertNotComplete().assertValue(Data.Empty)
            onHoldState.test().assertNotComplete().assertValue(false)
            operatorMediaState.test().assertNotComplete().assertValue(Data.Empty)
            if (ongoingEngagement) {
                screenSharingState.test().assertNotComplete().assertValue(ScreenSharingState.Ended)
            }
            assertNull(operatorCurrentMediaState)
            assertNull(currentOperatorValue)
        }

        if (ongoingEngagement) {
            if (endedLocally) {
                verify { engagement.end(any()) }
            }

            verifyUnsubscribedFromEngagement()

            val state = if (engagement is OmnicoreEngagement) State.FinishedOmniCore else State.FinishedCallVisualizer
            repository.engagementState.test().assertNotComplete().assertValue(state)
        }
    }

    private fun verifyUnsubscribedFromEngagement() {
        verify { engagement.off(Engagement.Events.END, any()) }
        verify { engagement.off(Engagement.Events.STATE_UPDATE, any()) }

        verify { media.off(Media.Events.MEDIA_UPGRADE_OFFER, any()) }
        verify { media.off(Media.Events.OPERATOR_STATE_UPDATE, any()) }
        verify { media.off(Media.Events.VISITOR_STATE_UPDATE, any()) }
        verify { media.currentCameraDevice }

        verify { screenSharing.off(ScreenSharing.Events.SCREEN_SHARING_REQUEST, any()) }
        verify { screenSharing.off(ScreenSharing.Events.VISITOR_STATE, any()) }

        verify { engagement.state }
        verify { engagementState.operator }
        verify { engagement.media }
        verify { engagement.screenSharing }
        verify { chat.off(Chat.Events.OPERATOR_TYPING_STATUS, any()) }
        verify { engagement.chat }

        confirmEngagementVerified()
    }

    private fun confirmEngagementVerified() {
        confirmVerified(engagement, engagementState, operator, chat, media, screenSharing)
    }

    private fun requestScreenSharing(testBody: (ScreenSharingRequest) -> Unit) {
        val screenSharingRequest: ScreenSharingRequest = mockk(relaxUnitFun = true)
        mockEngagementAndStart()
        val operatorName = "operator_name"

        val operator1: Operator = mockk(relaxed = true) {
            every { id } returns "1"
            every { formattedName } returns operatorName
        }

        val state1: EngagementState = mockk(relaxed = true) {
            every { operator } returns operator1
            every { visitorStatus } returns EngagementState.VisitorStatus.ENGAGED
            every { id } returns "s_1"
        }

        engagementStateCallbackSlot.captured.accept(state1)
        verify { operatorRepository.emit(operator1) }

        screenSharingRequestCallbackSlot.captured.accept(screenSharingRequest)

        repository.screenSharingState.test().assertNotComplete().assertValue(ScreenSharingState.Requested)
        testBody(screenSharingRequest)
        confirmVerified(screenSharingRequest)
    }

    @After
    fun tearDown() {
        confirmVerified(core, operatorRepository, callVisualizer, queueRepository)
        unmockkStatic(LOGGER_PATH)
    }

    @Test
    fun `engagementRequest will emit value when engagement is requested`() {
        val incomingEngagementRequest: IncomingEngagementRequest = mockk()
        every { incomingEngagementRequest.on(EngagementRequest.Events.OUTCOME, any()) } returns Unit
        val incomingEngagementRequestTest = repository.engagementRequest.test()

        engagementRequestCallbackSlot.captured.accept(incomingEngagementRequest)
        incomingEngagementRequestTest.assertNoErrors().assertValue(incomingEngagementRequest)
    }

    @Test
    fun `acceptCurrentEngagementRequest will accept current IncomingEngagementRequest`() {
        val exceptionCallbackSlot = slot<Consumer<GliaException?>>()
        val incomingEngagementRequest: IncomingEngagementRequest = mockk(relaxUnitFun = true)
        every { incomingEngagementRequest.on(EngagementRequest.Events.OUTCOME, any()) } returns Unit
        val incomingEngagementRequestTest = repository.engagementRequest.test()

        engagementRequestCallbackSlot.captured.accept(incomingEngagementRequest)
        incomingEngagementRequestTest.assertNoErrors().assertValue(incomingEngagementRequest)

        verify { incomingEngagementRequest.on(EngagementRequest.Events.OUTCOME, any()) }

        val contextAssetId = "asset_id"

        repository.acceptCurrentEngagementRequest(contextAssetId)


        verify { incomingEngagementRequest.accept(contextAssetId, capture(exceptionCallbackSlot)) }
        exceptionCallbackSlot.captured.accept(null)

        verify { Logger.i(any(), any()) }

        confirmVerified(incomingEngagementRequest)
    }

    @Test
    fun `declineCurrentEngagementRequest() will decline current IncomingEngagementRequest and log info if succeed`() {
        val exceptionCallbackSlot = slot<Consumer<GliaException?>>()
        val incomingEngagementRequest: IncomingEngagementRequest = mockk(relaxUnitFun = true)
        every { incomingEngagementRequest.on(EngagementRequest.Events.OUTCOME, any()) } returns Unit
        val incomingEngagementRequestTest = repository.engagementRequest.test()

        engagementRequestCallbackSlot.captured.accept(incomingEngagementRequest)
        incomingEngagementRequestTest.assertNoErrors().assertValue(incomingEngagementRequest)

        verify { incomingEngagementRequest.on(EngagementRequest.Events.OUTCOME, any()) }

        repository.declineCurrentEngagementRequest()
        verify { incomingEngagementRequest.decline(capture(exceptionCallbackSlot)) }

        exceptionCallbackSlot.captured.accept(null)

        verify { Logger.i(any(), any()) }

        confirmVerified(incomingEngagementRequest)
    }

    @Test
    fun `declineCurrentEngagementRequest() will decline current IncomingEngagementRequest and log error if failed`() {
        val exceptionCallbackSlot = slot<Consumer<GliaException?>>()
        val incomingEngagementRequest: IncomingEngagementRequest = mockk(relaxUnitFun = true)
        every { incomingEngagementRequest.on(EngagementRequest.Events.OUTCOME, any()) } returns Unit
        val incomingEngagementRequestTest = repository.engagementRequest.test()

        engagementRequestCallbackSlot.captured.accept(incomingEngagementRequest)
        incomingEngagementRequestTest.assertNoErrors().assertValue(incomingEngagementRequest)

        verify { incomingEngagementRequest.on(EngagementRequest.Events.OUTCOME, any()) }

        repository.declineCurrentEngagementRequest()
        verify { incomingEngagementRequest.decline(capture(exceptionCallbackSlot)) }

        val ex: GliaException = mockk {
            every { message } returns ""
        }

        exceptionCallbackSlot.captured.accept(ex)

        verify { Logger.e(any(), any()) }

        confirmVerified(incomingEngagementRequest)
    }

    @Test
    fun `engagementOutcome will emit value when outcome is requested`() {
        val incomingEngagementRequest: IncomingEngagementRequest = mockk()
        val outcomeSlot = slot<Consumer<EngagementRequest.Outcome>>()
        every { incomingEngagementRequest.on(EngagementRequest.Events.OUTCOME, capture(outcomeSlot)) } returns Unit
        val engagementOutcomeTest = repository.engagementOutcome.test()

        engagementRequestCallbackSlot.captured.accept(incomingEngagementRequest)
        outcomeSlot.captured.accept(EngagementRequest.Outcome.ACCEPTED)

        engagementOutcomeTest.assertNoErrors().assertValue(EngagementRequest.Outcome.ACCEPTED)
    }

    @Test
    fun `unsubscribe from engagement outcome event will happen when the outcome is come`() {
        val incomingEngagementRequest: IncomingEngagementRequest = mockk()
        val outcomeSlot = slot<Consumer<EngagementRequest.Outcome>>()
        every { incomingEngagementRequest.on(EngagementRequest.Events.OUTCOME, capture(outcomeSlot)) } returns Unit

        engagementRequestCallbackSlot.captured.accept(incomingEngagementRequest)
        outcomeSlot.captured.accept(EngagementRequest.Outcome.ACCEPTED)

        verify { incomingEngagementRequest.off(EngagementRequest.Events.OUTCOME, any()) }
    }

    @Test
    fun `endEngagement() will do nothing when no ongoing engagement`() {
        repository.endEngagement(false)
        verifyEngagementEnd(ongoingEngagement = false)
    }

    @Test
    fun `endEngagement() will not fetch survey when silently = true`() {
        val surveyStateTestSubscriber = repository.survey.test()
        mockEngagementAndStart()
        fillStates()
        repository.endEngagement(silently = true)
        verifyEngagementEnd()
        surveyStateTestSubscriber.assertNotComplete().assertValueCount(1).assertValue(SurveyState.Empty)
    }

    @Test
    fun `endEngagement() will not fetch survey when CallVisualizer`() {
        val surveyStateTestSubscriber = repository.survey.test()
        mockEngagementAndStart(callVisualizer = true)
        fillStates()
        repository.endEngagement(silently = true)
        verifyEngagementEnd()
        surveyStateTestSubscriber.assertNotComplete().assertValueCount(1).assertValue(SurveyState.Empty)
    }

    @Test
    fun `endEngagement() will update survey when it present`() {
        val survey: Survey = mockk()
        val surveyCallbackSlot = slot<RequestCallback<Survey>>()
        val surveyStateTestSubscriber = repository.survey.test()
        mockEngagementAndStart()
        fillStates()
        repository.endEngagement(silently = false)
        verify { engagement.getSurvey(capture(surveyCallbackSlot)) }

        verifyEngagementEnd()
        surveyCallbackSlot.captured.onResult(survey, null)
        surveyStateTestSubscriber.assertNotComplete().assertValueCount(1).assertValue(SurveyState.Value(survey))
    }

    @Test
    fun `endEngagement() will not update survey when it is not present`() {
        val exception: GliaException = mockk()
        val surveyCallbackSlot = slot<RequestCallback<Survey>>()
        val surveyStateTestSubscriber = repository.survey.test()
        mockEngagementAndStart()
        fillStates()
        repository.endEngagement(silently = false)
        verify { engagement.getSurvey(capture(surveyCallbackSlot)) }

        verifyEngagementEnd()
        surveyCallbackSlot.captured.onResult(null, exception)
        surveyStateTestSubscriber.assertNotComplete().assertValueCount(1).assertValue(SurveyState.Empty)
    }

    @Test
    fun `end engagement event will not fetch survey when CallVisualizer`() {
        val surveyStateTestSubscriber = repository.survey.test()
        mockEngagementAndStart(callVisualizer = true)
        fillStates()
        engagementEndCallbackSlot.captured.run()
        verifyEngagementEnd(endedLocally = false)
        surveyStateTestSubscriber.assertNotComplete().assertValueCount(1).assertValue(SurveyState.Empty)
    }

    @Test
    fun `end engagement event will update survey when it present`() {
        val survey: Survey = mockk()
        val surveyCallbackSlot = slot<RequestCallback<Survey>>()
        val surveyStateTestSubscriber = repository.survey.test()
        mockEngagementAndStart()
        fillStates()
        engagementEndCallbackSlot.captured.run()
        verify { engagement.getSurvey(capture(surveyCallbackSlot)) }

        verifyEngagementEnd(endedLocally = false)
        surveyCallbackSlot.captured.onResult(survey, null)
        surveyStateTestSubscriber.assertNotComplete().assertValueCount(1).assertValue(SurveyState.Value(survey))
    }

    @Test
    fun `end engagement event will not update survey when it is not present`() {
        val exception: GliaException = mockk()
        val surveyCallbackSlot = slot<RequestCallback<Survey>>()
        val surveyStateTestSubscriber = repository.survey.test()
        mockEngagementAndStart()
        fillStates()
        engagementEndCallbackSlot.captured.run()
        verify { engagement.getSurvey(capture(surveyCallbackSlot)) }

        verifyEngagementEnd(endedLocally = false)
        surveyCallbackSlot.captured.onResult(null, exception)
        surveyStateTestSubscriber.assertNotComplete().assertValueCount(1).assertValue(SurveyState.EmptyFromOperatorRequest)
    }

    @Test
    fun `engagement state handled properly when updated`() {
        val currentOperatorTestObserver = repository.currentOperator.test()
        val engagementStateTestObserver = repository.engagementState.test()
        val operatorTypingStatusTestObserver = repository.operatorTypingStatus.test()

        val operator1: Operator = mockk(relaxed = true) {
            every { id } returns "1"
        }

        val operator2: Operator = mockk(relaxed = true) {
            every { id } returns "2"
        }
        val state1: EngagementState = mockk(relaxed = true) {
            every { operator } returns operator1
            every { visitorStatus } returns EngagementState.VisitorStatus.ENGAGED
            every { id } returns "s_1"
        }

        val state2: EngagementState = mockk(relaxed = true) {
            every { operator } returns operator1
            every { visitorStatus } returns EngagementState.VisitorStatus.TRANSFERRING
            every { id } returns "s_2"
        }

        val state3: EngagementState = mockk(relaxed = true) {
            every { operator } returns operator2
            every { visitorStatus } returns EngagementState.VisitorStatus.ENGAGED
            every { id } returns "s_3"
        }

        val state4: EngagementState = mockk(relaxed = true) {
            every { operator } returns operator2
            every { visitorStatus } returns EngagementState.VisitorStatus.ENGAGED
            every { id } returns "s_4"
        }

        mockEngagementAndStart()

        engagementStateCallbackSlot.captured.accept(state1)
        assertTrue(repository.isOperatorPresent)
        assertEquals(operator1, repository.currentOperatorValue)
        engagementStateCallbackSlot.captured.accept(state2)
        assertEquals(operator1, repository.currentOperatorValue)
        engagementStateCallbackSlot.captured.accept(state3)
        assertEquals(operator2, repository.currentOperatorValue)
        engagementStateCallbackSlot.captured.accept(state4)
        assertEquals(operator2, repository.currentOperatorValue)

        operatorTypingCallbackSlot.captured.apply {
            accept(OperatorTypingStatus { false })
            accept(OperatorTypingStatus { true })
            accept(OperatorTypingStatus { false })
        }

        verify(exactly = 2) { operatorRepository.emit(operator1) }
        verify(exactly = 2) { operatorRepository.emit(operator2) }

        operatorTypingStatusTestObserver
            .assertNotComplete()
            .assertValueCount(3)
            .assertValuesOnly(
                false,
                true,
                false
            )

        currentOperatorTestObserver
            .assertNotComplete()
            .assertValueCount(5)
            .assertValuesOnly(
                Data.Empty,
                Data.Value(operator1),
                Data.Value(operator1),
                Data.Value(operator2),
                Data.Value(operator2)
            )


        engagementStateTestObserver
            .assertNotComplete()
            .assertValueCount(6)
            .assertValuesOnly(
                State.NoEngagement,
                State.StartedOmniCore,
                State.Update(state1, EngagementUpdateState.OperatorConnected(operator1)),
                State.Update(state2, EngagementUpdateState.Transferring),
                State.Update(state3, EngagementUpdateState.OperatorChanged(operator2)),
                State.Update(state4, EngagementUpdateState.Ongoing(operator2))
            )

        repository.endEngagement(silently = true)
        verifyEngagementEnd()

    }

    @Test
    fun `acceptMediaUpgradeRequest triggers mediaUpgradeOfferAcceptResult with success when successful`() {
        val mediaUpgradeOffer: MediaUpgradeOffer = mockk(relaxUnitFun = true)

        mockEngagementAndStart()

        val mediaUpgradeOfferTestSubscriber = repository.mediaUpgradeOffer.test()
        mediaUpgradeOfferCallbackSlot.captured.accept(mediaUpgradeOffer)
        mediaUpgradeOfferTestSubscriber.assertNotComplete().assertValueCount(1).assertValue(mediaUpgradeOffer)

        repository.acceptMediaUpgradeRequest(mediaUpgradeOffer)
        val mediaUpgradeOfferAcceptCallbackSlot = slot<Consumer<GliaException?>>()
        verify { mediaUpgradeOffer.accept(capture(mediaUpgradeOfferAcceptCallbackSlot)) }
        val resultTestSubscriber = repository.mediaUpgradeOfferAcceptResult.test()

        mediaUpgradeOfferAcceptCallbackSlot.captured.accept(null)

        resultTestSubscriber.assertNotComplete().assertValue { it.isSuccess }
    }

    @Test
    fun `acceptMediaUpgradeRequest triggers mediaUpgradeOfferAcceptResult with failure when exception is received`() {
        val mediaUpgradeOffer: MediaUpgradeOffer = mockk(relaxUnitFun = true)

        mockEngagementAndStart()

        val mediaUpgradeOfferTestSubscriber = repository.mediaUpgradeOffer.test()
        mediaUpgradeOfferCallbackSlot.captured.accept(mediaUpgradeOffer)
        mediaUpgradeOfferTestSubscriber.assertNotComplete().assertValueCount(1).assertValue(mediaUpgradeOffer)

        repository.acceptMediaUpgradeRequest(mediaUpgradeOffer)
        val mediaUpgradeOfferAcceptCallbackSlot = slot<Consumer<GliaException?>>()
        verify { mediaUpgradeOffer.accept(capture(mediaUpgradeOfferAcceptCallbackSlot)) }
        val resultTestSubscriber = repository.mediaUpgradeOfferAcceptResult.test()

        mediaUpgradeOfferAcceptCallbackSlot.captured.accept(mockk())

        resultTestSubscriber.assertNotComplete().assertValue { !it.isSuccess }
    }

    @Test
    fun `declineMediaUpgradeRequest does not trigger mediaUpgradeOfferAcceptResult when exception is received`() {
        val mediaUpgradeOffer: MediaUpgradeOffer = mockk(relaxUnitFun = true)

        mockEngagementAndStart()

        val mediaUpgradeOfferTestSubscriber = repository.mediaUpgradeOffer.test()
        mediaUpgradeOfferCallbackSlot.captured.accept(mediaUpgradeOffer)
        mediaUpgradeOfferTestSubscriber.assertNotComplete().assertValueCount(1).assertValue(mediaUpgradeOffer)

        repository.declineMediaUpgradeRequest(mediaUpgradeOffer)
        val mediaUpgradeOfferAcceptCallbackSlot = slot<Consumer<GliaException?>>()
        verify { mediaUpgradeOffer.decline(capture(mediaUpgradeOfferAcceptCallbackSlot)) }
        val resultTestSubscriber = repository.mediaUpgradeOfferAcceptResult.test()

        mediaUpgradeOfferAcceptCallbackSlot.captured.accept(mockk())

        resultTestSubscriber.assertNotComplete().assertNoValues()
    }

    @Test
    fun `declineMediaUpgradeRequest does not trigger mediaUpgradeOfferAcceptResult when successfully declined`() {
        val mediaUpgradeOffer: MediaUpgradeOffer = mockk(relaxUnitFun = true)

        mockEngagementAndStart()

        val mediaUpgradeOfferTestSubscriber = repository.mediaUpgradeOffer.test()
        mediaUpgradeOfferCallbackSlot.captured.accept(mediaUpgradeOffer)
        mediaUpgradeOfferTestSubscriber.assertNotComplete().assertValueCount(1).assertValue(mediaUpgradeOffer)

        repository.declineMediaUpgradeRequest(mediaUpgradeOffer)
        val mediaUpgradeOfferAcceptCallbackSlot = slot<Consumer<GliaException?>>()
        verify { mediaUpgradeOffer.decline(capture(mediaUpgradeOfferAcceptCallbackSlot)) }
        val resultTestSubscriber = repository.mediaUpgradeOfferAcceptResult.test()

        mediaUpgradeOfferAcceptCallbackSlot.captured.accept(null)

        resultTestSubscriber.assertNotComplete().assertNoValues()
    }

    @Test
    fun `queueForEngagement produces PreQueueing`() {
        val queueId = "queue_id"
        every { queueRepository.relevantQueueIds } returns Single.just(listOf(queueId))
        val queueForEngagementCallbackSlot = slot<Consumer<GliaException?>>()

        repository.queueForEngagement(MediaType.TEXT)

        verify(exactly = 1) { core.queueForEngagement(listOf(queueId), MediaType.TEXT, null, any(), any(), capture(queueForEngagementCallbackSlot)) }
        queueForEngagementCallbackSlot.captured.accept(null)

        assertTrue(repository.isQueueing)
        assertFalse(repository.isQueueingForMedia)

        repository.engagementState.test().assertNotComplete().assertValue(State.PreQueuing(MediaType.TEXT))

        repository.queueForEngagement(MediaType.TEXT)

        verify { queueRepository.relevantQueueIds }
        verify(exactly = 0) { core.queueForEngagement(listOf(queueId), MediaType.TEXT, "url", any(), any(), capture(queueForEngagementCallbackSlot)) }
    }

    @Test
    fun `queueForEngagement produces Error when relevant queues are empty`() {
        every { queueRepository.relevantQueueIds } returns Single.just(emptyList())
        val testSubscriber = repository.engagementState.test()

        repository.queueForEngagement(MediaType.TEXT)

        verify { queueRepository.relevantQueueIds }
        verify(exactly = 0) { core.queueForEngagement(any(), MediaType.TEXT, null, any(), any(), any()) }

        assertFalse(repository.isQueueing)
        assertFalse(repository.isQueueingForMedia)

        testSubscriber.assertNotComplete().assertValuesOnly(
            State.NoEngagement,
            State.PreQueuing(MediaType.TEXT),
            State.UnexpectedErrorHappened,
            State.NoEngagement
        )
    }

    @Test
    fun `queueForEngagement will do nothing when already queued`() {
        val queueId = "queue_id"
        val mediaType = MediaType.AUDIO
        every { queueRepository.relevantQueueIds } returns Single.just(listOf(queueId))
        val queueForEngagementCallbackSlot = slot<Consumer<GliaException?>>()
        repository.queueForEngagement(mediaType)

        repository.engagementState.test().assertNotComplete().assertValue(State.PreQueuing(mediaType))
        verify { queueRepository.relevantQueueIds }
        verify(exactly = 1) { core.queueForEngagement(listOf(queueId), mediaType, null, any(), any(), capture(queueForEngagementCallbackSlot)) }
        queueForEngagementCallbackSlot.captured.accept(GliaException("message", GliaException.Cause.ALREADY_QUEUED))

        assertTrue(repository.isQueueing)
        assertTrue(repository.isQueueingForMedia)

        repository.engagementState.test().assertNotComplete().assertValue(State.PreQueuing(mediaType))

        repository.queueForEngagement(MediaType.VIDEO)

        verify(exactly = 0) { core.queueForEngagement(listOf(queueId), MediaType.VIDEO, null, any(), any(), capture(queueForEngagementCallbackSlot)) }
    }

    @Test
    fun `queueForEngagement produces QueueUnstaffed when queue is unavailable`() {
        val queueId = "queue_id"
        val mediaType = MediaType.AUDIO
        every { queueRepository.relevantQueueIds } returns Single.just(listOf(queueId))
        val queueForEngagementCallbackSlot = slot<Consumer<GliaException?>>()
        val testSubscriber = repository.engagementState.test()
        repository.queueForEngagement(mediaType)

        verify { queueRepository.relevantQueueIds }
        verify { core.queueForEngagement(listOf(queueId), mediaType, null, any(), any(), capture(queueForEngagementCallbackSlot)) }
        queueForEngagementCallbackSlot.captured.accept(GliaException("message", GliaException.Cause.QUEUE_CLOSED))

        testSubscriber.assertNotComplete().assertValuesOnly(
            State.NoEngagement,
            State.PreQueuing(mediaType = mediaType),
            State.QueueUnstaffed,
            State.NoEngagement
        )
    }

    @Test
    fun `queueForEngagement produces UnexpectedError when queueing failed`() {
        val queueId = "queue_id"
        val mediaType = MediaType.AUDIO
        every { queueRepository.relevantQueueIds } returns Single.just(listOf(queueId))
        val queueForEngagementCallbackSlot = slot<Consumer<GliaException?>>()
        val testSubscriber = repository.engagementState.test()
        repository.queueForEngagement(mediaType)

        verify { queueRepository.relevantQueueIds }
        verify { core.queueForEngagement(listOf(queueId), mediaType, null, any(), any(), capture(queueForEngagementCallbackSlot)) }
        queueForEngagementCallbackSlot.captured.accept(GliaException("message", GliaException.Cause.NETWORK_TIMEOUT))

        testSubscriber.assertNotComplete().assertValuesOnly(
            State.NoEngagement,
            State.PreQueuing(mediaType = mediaType),
            State.UnexpectedErrorHappened,
            State.NoEngagement
        )
    }

    @Test
    fun `enqueuing is canceling when unstaffed queue ticked is received`() {
        val queueId = "queue_id"
        val mediaType = MediaType.TEXT
        every { queueRepository.relevantQueueIds } returns Single.just(listOf(queueId))
        val queueForEngagementCallbackSlot = slot<Consumer<GliaException?>>()
        val subscribeQueueTicketCallbackSlot = slot<RequestCallback<QueueTicket?>>()
        val ticketId = "ticket_id"
        val queueTicket: QueueTicket = mockk(relaxed = true) {
            every { id } returns ticketId
        }
        repository.queueForEngagement(mediaType)

        verify { queueRepository.relevantQueueIds }
        verify(exactly = 1) { core.queueForEngagement(listOf(queueId), mediaType, null, any(), any(), capture(queueForEngagementCallbackSlot)) }
        queueForEngagementCallbackSlot.captured.accept(null)

        assertTrue(repository.isQueueing)
        assertFalse(repository.isQueueingForMedia)

        repository.engagementState.test().assertNotComplete().assertValue(State.PreQueuing(mediaType))

        queueTicketCallbackSlot.captured.accept(queueTicket)

        verify { core.subscribeToQueueTicketUpdates(ticketId, capture(subscribeQueueTicketCallbackSlot)) }

        repository.engagementState.test().apply {
            val unstaffedTicket: QueueTicket = mockk(relaxed = true) {
                every { state } returns QueueTicket.State.UNSTAFFED
            }
            subscribeQueueTicketCallbackSlot.captured.onResult(unstaffedTicket, null)

            assertNotComplete().assertValuesOnly(
                State.Queuing(ticketId, mediaType),
                State.QueueUnstaffed,
                State.NoEngagement
            )
        }
    }

    @Test
    fun `cancelQueuing sends cancel queue request immediately when current state is Queueing`() {
        val queueId = "queue_id"
        val mediaType = MediaType.TEXT
        every { queueRepository.relevantQueueIds } returns Single.just(listOf(queueId))
        val queueForEngagementCallbackSlot = slot<Consumer<GliaException?>>()
        val subscribeQueueTicketCallbackSlot = slot<RequestCallback<QueueTicket?>>()
        val cancelQueueTicketCallbackSlot = slot<Consumer<GliaException?>>()
        val ticketId = "ticket_id"
        val queueTicket: QueueTicket = mockk(relaxed = true) {
            every { id } returns ticketId
        }
        repository.queueForEngagement(mediaType)

        verify { queueRepository.relevantQueueIds }
        verify(exactly = 1) { core.queueForEngagement(listOf(queueId), mediaType, null, any(), any(), capture(queueForEngagementCallbackSlot)) }
        queueForEngagementCallbackSlot.captured.accept(null)

        assertTrue(repository.isQueueing)
        assertFalse(repository.isQueueingForMedia)

        repository.engagementState.test().assertNotComplete().assertValue(State.PreQueuing(mediaType))

        queueTicketCallbackSlot.captured.accept(queueTicket)

        verify { core.subscribeToQueueTicketUpdates(ticketId, capture(subscribeQueueTicketCallbackSlot)) }

        repository.engagementState.test().apply {
            repository.cancelQueuing()
            verify { core.cancelQueueTicket(ticketId, capture(cancelQueueTicketCallbackSlot)) }
            cancelQueueTicketCallbackSlot.captured.accept(null)
            assertNotComplete()
                .assertValuesOnly(
                    State.Queuing(ticketId, mediaType),
                    State.QueueingCanceled,
                    State.NoEngagement
                )
        }
    }

    @Test
    fun `cancelQueuing sends cancel queue request after queue ticket update when current state is PreQueueing`() {
        val queueId = "queue_id"
        val mediaType = MediaType.TEXT
        every { queueRepository.relevantQueueIds } returns Single.just(listOf(queueId))
        val queueForEngagementCallbackSlot = slot<Consumer<GliaException?>>()
        val subscribeQueueTicketCallbackSlot = slot<RequestCallback<QueueTicket?>>()
        val cancelQueueTicketCallbackSlot = slot<Consumer<GliaException?>>()
        val ticketId = "ticket_id"
        val queueTicket: QueueTicket = mockk(relaxed = true) {
            every { id } returns ticketId
        }
        repository.queueForEngagement(mediaType)

        verify { queueRepository.relevantQueueIds }
        verify(exactly = 1) { core.queueForEngagement(listOf(queueId), mediaType, null, any(), any(), capture(queueForEngagementCallbackSlot)) }
        queueForEngagementCallbackSlot.captured.accept(null)

        assertTrue(repository.isQueueing)
        assertFalse(repository.isQueueingForMedia)

        repository.engagementState.test().assertNotComplete().assertValue(State.PreQueuing(mediaType))
        repository.cancelQueuing()

        queueTicketCallbackSlot.captured.accept(queueTicket)

        verify(inverse = true) { core.subscribeToQueueTicketUpdates(ticketId, capture(subscribeQueueTicketCallbackSlot)) }

        repository.engagementState.test().apply {
            verify { core.cancelQueueTicket(ticketId, capture(cancelQueueTicketCallbackSlot)) }
            cancelQueueTicketCallbackSlot.captured.accept(null)
            assertNotComplete()
                .assertValuesOnly(
                    State.QueueingCanceled,
                    State.NoEngagement
                )
        }
    }

    @Test
    fun `reset should call cancelQueuing when no ongoing engagement`() {
        val repositorySpy = spyk(repository)

        repositorySpy.reset()

        verify { repositorySpy.cancelQueuing() }
        verify(inverse = true) { repositorySpy.endEngagement(true) }
    }

    @Test
    fun `reset should call endEngagement when engagement is  ongoing`() {
        mockEngagementAndStart()
        val repositorySpy = spyk(repository)

        repositorySpy.reset()

        verify { repositorySpy.endEngagement(true) }
        verify(inverse = true) { repositorySpy.cancelQueuing() }
    }

    @Test
    fun `muteVisitorAudio will mute audio when it is present`() {
        mockEngagementAndStart()
        repository.muteVisitorAudio()
        assertNull(repository.visitorCurrentMediaState)

        mockVisitorMediaState { visitorMediaState, audio, _ ->
            repository.muteVisitorAudio()
            verify(exactly = 1) { visitorMediaState.audio }
            verify(inverse = true) { visitorMediaState.video }
            verify(exactly = 1) { audio.mute() }
        }
    }

    @Test
    fun `unMuteVisitorAudio will unMute audio when it is present`() {
        mockEngagementAndStart()
        repository.muteVisitorAudio()
        assertNull(repository.visitorCurrentMediaState)

        mockVisitorMediaState { visitorMediaState, audio, _ ->
            repository.unMuteVisitorAudio()
            verify(exactly = 1) { visitorMediaState.audio }
            verify(inverse = true) { visitorMediaState.video }
            verify(exactly = 1) { audio.unmute() }
        }
    }

    @Test
    fun `pauseVisitorVideo will pause video when it is present`() {
        mockEngagementAndStart()
        repository.pauseVisitorVideo()
        assertNull(repository.visitorCurrentMediaState)

        mockVisitorMediaState { visitorMediaState, _, video ->
            repository.pauseVisitorVideo()
            verify(exactly = 0) { visitorMediaState.audio }
            verify(exactly = 1) { visitorMediaState.video }
            verify(exactly = 1) { video.pause() }
        }
    }

    @Test
    fun `resumeVisitorVideo will resume video when it is present`() {
        mockEngagementAndStart()
        repository.resumeVisitorVideo()
        assertNull(repository.visitorCurrentMediaState)

        mockVisitorMediaState { visitorMediaState, _, video ->
            repository.resumeVisitorVideo()
            verify(exactly = 0) { visitorMediaState.audio }
            verify(exactly = 1) { visitorMediaState.video }
            verify(exactly = 1) { video.resume() }
        }
    }

    @Test
    fun `declineScreenSharingRequest will send CANCELED result to core`() {
        requestScreenSharing {
            repository.declineScreenSharingRequest()
            verify { engagement.onActivityResult(SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE, Activity.RESULT_CANCELED, null) }
            verify { it.decline() }
            repository.screenSharingState.test().assertNotComplete().assertValue(ScreenSharingState.RequestDeclined)
            assertFalse(repository.isSharingScreen)
        }
    }

    @Test
    fun `acceptScreenSharingWithAskedPermission will accept request with SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE result code`() {
        val activity: Activity = mockk()
        val screenSharingMode = ScreenSharing.Mode.APP_BOUNDED
        requestScreenSharing {
            repository.acceptScreenSharingWithAskedPermission(activity, screenSharingMode)
            val onAcceptResultSlot = slot<Consumer<GliaException?>>()

            verify { it.accept(screenSharingMode, activity, SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE, capture(onAcceptResultSlot)) }
            val debugMessageText = "debuuugg meeeeeesage"

            onAcceptResultSlot.captured.accept(GliaException(debugMessageText, GliaException.Cause.NETWORK_TIMEOUT))

            repository.screenSharingState.test().assertNotComplete().assertValue(ScreenSharingState.FailedToAcceptRequest(debugMessageText))
            assertFalse(repository.isSharingScreen)
        }
    }

    @Test
    fun `onActivityResult will call appropriate engagement function when ongoing engagement`() {
        mockEngagementAndStart()
        repository.onActivityResult(1, 2, null)
        verify { engagement.onActivityResult(1, 2, null) }
    }

    @Test
    fun `onActivityResultSkipScreenSharingPermissionRequest will call appropriate engagement function with special request code when ongoing engagement`() {
        mockEngagementAndStart()
        repository.onActivityResultSkipScreenSharingPermissionRequest(1, null)
        repository.onReadyToShareScreen()
        repository.onReadyToShareScreen()
        verify { engagement.onActivityResult(SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE, 1, null) }
    }

    @Test
    fun `endScreenSharing will end screen sharing`() {
        val screenSharingStateTestSubscriber = repository.screenSharingState.test()
        val localScreen: LocalScreen = mockk(relaxed = true)
        mockEngagementAndStart()

        screenSharingStateCallbackSlot.captured.accept(VisitorScreenSharingState(ScreenSharing.Status.SHARING, localScreen))
        screenSharingStateCallbackSlot.captured.accept(VisitorScreenSharingState(ScreenSharing.Status.SHARING, localScreen))
        screenSharingStateCallbackSlot.captured.accept(VisitorScreenSharingState(ScreenSharing.Status.SHARING, localScreen))
        assertTrue(repository.isSharingScreen)
        repository.endScreenSharing()

        verify { localScreen.stopSharing() }
        assertFalse(repository.isSharingScreen)
        screenSharingStateTestSubscriber
            .assertValueCount(2)
            .assertValuesOnly(
                ScreenSharingState.Started,
                ScreenSharingState.Ended
            )

        confirmVerified(localScreen)

    }

    @Test
    fun `NOT_SHARING status will mark screen sharing ended`() {
        val screenSharingStateTestSubscriber = repository.screenSharingState.test()
        val localScreen: LocalScreen = mockk(relaxed = true)
        mockEngagementAndStart()

        screenSharingStateCallbackSlot.captured.accept(VisitorScreenSharingState(ScreenSharing.Status.SHARING, localScreen))
        assertTrue(repository.isSharingScreen)
        screenSharingStateCallbackSlot.captured.accept(VisitorScreenSharingState(ScreenSharing.Status.NOT_SHARING, localScreen))

        verify(inverse = true) { localScreen.stopSharing() }
        assertFalse(repository.isSharingScreen)
        screenSharingStateTestSubscriber
            .assertValueCount(2)
            .assertValuesOnly(
                ScreenSharingState.Started,
                ScreenSharingState.Ended
            )

        confirmVerified(localScreen)

    }

    @Test
    fun `unsubscribe from the old engagement will happen when new omnicore engagement received`() {
        mockEngagementAndStart()
        //emit new state start
        val operator1: Operator = mockk(relaxed = true) {
            every { id } returns "1"
        }

        val state1: EngagementState = mockk(relaxed = true) {
            every { operator } returns operator1
            every { visitorStatus } returns EngagementState.VisitorStatus.ENGAGED
            every { id } returns "s_1"
        }
        engagementStateCallbackSlot.captured.accept(state1)
        repository.engagementState.test().assertNotComplete().assertValues(
            State.Update(state1, EngagementUpdateState.OperatorConnected(operator1))
        )
        verify { operatorRepository.emit(operator1) }
        //emit a new state end

        //emit new engagement start
        val newEngagement = mockk<OmnicoreEngagement>(relaxUnitFun = true)

        val newMedia: Media = mockk(relaxUnitFun = true)
        val newChat: Chat = mockk(relaxUnitFun = true)
        val newScreenSharing: ScreenSharing = mockk(relaxUnitFun = true)

        val newOperator: Operator = mockk(relaxUnitFun = true)
        val newEngagementState: EngagementState = mockk(relaxUnitFun = true)
        every { newEngagementState.operator } returns newOperator
        every { newEngagement.state } returns newEngagementState
        every { newEngagement.media } returns newMedia
        every { newEngagement.chat } returns newChat
        every { newEngagement.screenSharing } returns newScreenSharing
        every { newMedia.currentCameraDevice } returns null

        omniCoreEngagementCallbackSlot.captured.accept(newEngagement)
        repository.engagementState.test().assertNotComplete().assertValue(State.Update(state1, EngagementUpdateState.OperatorConnected(operator1)))
        assertTrue(repository.hasOngoingEngagement)

        verify { operatorRepository.emit(newOperator) }

        verify { newEngagement.on(Engagement.Events.END, any()) }
        verify { newEngagement.on(Engagement.Events.STATE_UPDATE, any()) }

        verify { newMedia.on(Media.Events.MEDIA_UPGRADE_OFFER, any()) }
        verify { newMedia.on(Media.Events.OPERATOR_STATE_UPDATE, any()) }
        verify { newMedia.on(Media.Events.VISITOR_STATE_UPDATE, any()) }
        verify { newMedia.currentCameraDevice }

        verify { newScreenSharing.on(ScreenSharing.Events.SCREEN_SHARING_REQUEST, any()) }
        verify { newScreenSharing.on(ScreenSharing.Events.VISITOR_STATE, any()) }

        verify { newChat.on(Chat.Events.OPERATOR_TYPING_STATUS, any()) }

        verify { newEngagement.state }
        verify { newEngagementState.operator }
        verify { newEngagement.media }
        verify { newEngagement.screenSharing }
        verify { newEngagement.chat }
        //emit new engagement end

        verifyUnsubscribedFromEngagement()
        confirmVerified(newEngagement, newMedia, newChat, newScreenSharing, newOperator, newEngagementState)
    }


    @Test
    fun `unsubscribe from the old engagement will happen when new omnibrowse engagement received`() {
        mockEngagementAndStart(true)
        //emit new state start
        val operator1: Operator = mockk(relaxed = true) {
            every { id } returns "1"
        }

        val state1: EngagementState = mockk(relaxed = true) {
            every { operator } returns operator1
            every { visitorStatus } returns EngagementState.VisitorStatus.ENGAGED
            every { id } returns "s_1"
        }
        engagementStateCallbackSlot.captured.accept(state1)
        repository.engagementState.test().assertNotComplete().assertValues(
            State.Update(state1, EngagementUpdateState.OperatorConnected(operator1))
        )
        verify { operatorRepository.emit(operator1) }
        //emit a new state end

        //emit new engagement start
        val newEngagement = mockk<OmnibrowseEngagement>(relaxUnitFun = true)

        val newMedia: Media = mockk(relaxUnitFun = true)
        val newChat: Chat = mockk(relaxUnitFun = true)
        val newScreenSharing: ScreenSharing = mockk(relaxUnitFun = true)

        val newOperator: Operator = mockk(relaxUnitFun = true)
        val newEngagementState: EngagementState = mockk(relaxUnitFun = true)
        every { newEngagementState.operator } returns newOperator
        every { newEngagement.state } returns newEngagementState
        every { newEngagement.media } returns newMedia
        every { newEngagement.chat } returns newChat
        every { newEngagement.screenSharing } returns newScreenSharing
        every { newMedia.currentCameraDevice } returns mockk()

        callVisualizerEngagementCallbackSlot.captured.accept(newEngagement)
        repository.engagementState.test().assertNotComplete().assertValue(State.Update(state1, EngagementUpdateState.OperatorConnected(operator1)))
        assertTrue(repository.hasOngoingEngagement)

        verify { operatorRepository.emit(newOperator) }

        verify { newEngagement.on(Engagement.Events.END, any()) }
        verify { newEngagement.on(Engagement.Events.STATE_UPDATE, any()) }

        verify { newMedia.on(Media.Events.MEDIA_UPGRADE_OFFER, any()) }
        verify { newMedia.on(Media.Events.OPERATOR_STATE_UPDATE, any()) }
        verify { newMedia.on(Media.Events.VISITOR_STATE_UPDATE, any()) }
        verify { newMedia.currentCameraDevice }

        verify { newScreenSharing.on(ScreenSharing.Events.SCREEN_SHARING_REQUEST, any()) }
        verify { newScreenSharing.on(ScreenSharing.Events.VISITOR_STATE, any()) }

        verify(exactly = 0) { newChat.on(Chat.Events.OPERATOR_TYPING_STATUS, any()) }

        verify { newEngagement.state }
        verify { newEngagementState.operator }
        verify { newEngagement.media }
        verify { newEngagement.screenSharing }
        verify(exactly = 0) { newEngagement.chat }
        //emit new engagement end

        verifyUnsubscribedFromEngagement()
        confirmVerified(newEngagement, newMedia, newChat, newScreenSharing, newOperator, newEngagementState)
    }

    @Test
    fun `setVisitorCamera will set camera device`() {
        mockEngagementAndStart()
        val cameraDevices = listOf<CameraDevice>(mockk(), mockk())
        every { media.cameraDevices } returns cameraDevices

        assertEquals(cameraDevices, repository.cameras)
    }

    @Test
    fun `setVisitorCamera will set camera device toggling`() {
        mockEngagementAndStart()

        val camera = mockk<CameraDevice>()
        repository.setVisitorCamera(camera)

        verify { media.setCameraDevice(camera) }
        assertEquals(VisitorCamera.Switching, repository.currentVisitorCamera)
    }

    @Test
    fun `queueForEngagement uses visitorContextAssetId from configurationManager`() {
        val queueId = "queue_id"
        val visitorContextAssetId = "visitor_context_asset_id"
        every { queueRepository.relevantQueueIds } returns Single.just(listOf(queueId))
        every { configurationManager.visitorContextAssetId } returns visitorContextAssetId
        val queueForEngagementCallbackSlot = slot<Consumer<GliaException?>>()

        repository.queueForEngagement(MediaType.TEXT)

        verify(exactly = 1) { core.queueForEngagement(listOf(queueId), MediaType.TEXT, visitorContextAssetId, any(), any(), capture(queueForEngagementCallbackSlot)) }
        queueForEngagementCallbackSlot.captured.accept(null)

        assertTrue(repository.isQueueing)
        assertFalse(repository.isQueueingForMedia)

        repository.queueForEngagement(MediaType.TEXT)

        verify { queueRepository.relevantQueueIds }
        verify(exactly = 1) { core.queueForEngagement(listOf(queueId), MediaType.TEXT, visitorContextAssetId, any(), any(), capture(queueForEngagementCallbackSlot)) }
    }

    @Test
    fun `queueForEngagement uses null visitorContextAssetId when configurationManager returns null`() {
        val queueId = "queue_id"
        every { queueRepository.relevantQueueIds } returns Single.just(listOf(queueId))
        every { configurationManager.visitorContextAssetId } returns null
        val queueForEngagementCallbackSlot = slot<Consumer<GliaException?>>()

        repository.queueForEngagement(MediaType.TEXT)

        verify(exactly = 1) { core.queueForEngagement(listOf(queueId), MediaType.TEXT, null, any(), any(), capture(queueForEngagementCallbackSlot)) }
        queueForEngagementCallbackSlot.captured.accept(null)

        assertTrue(repository.isQueueing)
        assertFalse(repository.isQueueingForMedia)
        repository.queueForEngagement(MediaType.TEXT)

        verify { queueRepository.relevantQueueIds }
        verify(exactly = 1) { core.queueForEngagement(listOf(queueId), MediaType.TEXT, null, any(), any(), capture(queueForEngagementCallbackSlot)) }
    }
}
