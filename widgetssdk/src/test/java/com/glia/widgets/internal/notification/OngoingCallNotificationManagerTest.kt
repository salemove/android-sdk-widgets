package com.glia.widgets.internal.notification

import android.Manifest
import android.content.pm.PackageManager
import android.mock
import android.unMock
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.comms.Audio
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.comms.Video
import com.glia.widgets.engagement.EndAction
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCaseImpl
import com.glia.widgets.engagement.domain.EngagementStateUseCaseImpl
import com.glia.widgets.engagement.domain.OperatorMediaUseCaseImpl
import com.glia.widgets.engagement.domain.VisitorMediaUseCaseImpl
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.Logger
import com.glia.widgets.internal.notification.device.INotificationManager
import com.glia.widgets.internal.notification.domain.CallNotification
import com.glia.widgets.internal.notification.domain.CallNotification.Phase.CONNECTING
import com.glia.widgets.internal.notification.domain.CallNotification.Phase.LIVE
import com.glia.widgets.internal.notification.domain.CallNotification.Phase.WAITING
import com.glia.widgets.internal.notification.domain.CallNotificationUseCase
import com.glia.widgets.internal.permissions.PermissionManager
import io.mockk.Runs
import io.mockk.andThenJust
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.processors.PublishProcessor
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.function.Consumer

class OngoingCallNotificationManagerTest {
    private val engagementState: BehaviorProcessor<State> = BehaviorProcessor.create()
    private val visitorMedia: BehaviorProcessor<Data<MediaState>> = BehaviorProcessor.createDefault(Data.Empty)
    private val operatorMedia: BehaviorProcessor<Data<MediaState>> = BehaviorProcessor.createDefault(Data.Empty)
    private val upgradeAcceptResult: PublishProcessor<Result<MediaUpgradeOffer>> = PublishProcessor.create()
    private val grantedPermissions = mutableSetOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)

    private lateinit var notificationManager: INotificationManager
    private lateinit var manager: OngoingCallNotificationManager

    private val audioCall = CallNotification(LIVE, MediaDirection.NONE, hasAudio = true)

    @Before
    fun setUp() {
        Logger.mock()
        notificationManager = mockk(relaxUnitFun = true)
        every { notificationManager.startCallService(any(), any(), any()) } returns true
    }

    @After
    fun tearDown() {
        Logger.unMock()
    }

    @Test
    fun `starts the microphone service with the waiting text when the visitor queues for audio`() = runManagerTest {
        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))
        engagementState.onNext(State.Queuing("ticket", MediaType.AUDIO))

        verify(exactly = 1) { notificationManager.startCallService(any(), any(), any()) }
        verify { notificationManager.startCallService(CallNotification(WAITING, MediaDirection.NONE, true), true, false) }
    }

    @Test
    fun `starts the microphone and camera service when the visitor queues for video`() = runManagerTest {
        engagementState.onNext(State.PreQueuing(MediaType.VIDEO))

        verify(exactly = 1) {
            notificationManager.startCallService(CallNotification(WAITING, MediaDirection.TWO_WAY, true), true, true)
        }
    }

    @Test
    fun `shows nothing when the visitor queues for chat`() = runManagerTest {
        engagementState.onNext(State.PreQueuing(MediaType.TEXT))
        engagementState.onNext(State.EngagementStarted(isCallVisualizer = false))

        verifyNothingShown()
    }

    @Test
    fun `stops the service when the visitor leaves the queue`() = runManagerTest {
        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))

        engagementState.onNext(State.QueueingCanceled)
        engagementState.onNext(State.NoEngagement)

        verify(exactly = 1) { notificationManager.startCallService(any(), any(), any()) }
        verify(exactly = 1) { notificationManager.removeCallNotification() }
    }

    @Test
    fun `stops the service when the queue is unstaffed`() = runManagerTest {
        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))

        engagementState.onNext(State.QueueUnstaffed)

        verify(exactly = 1) { notificationManager.removeCallNotification() }
    }

    @Test
    fun `updates the text through connecting to live without restarting the service`() = runManagerTest {
        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))
        setAppVisible(false)

        engagementState.onNext(State.EngagementStarted(isCallVisualizer = false))
        startAudioMedia()

        verify(exactly = 1) { notificationManager.startCallService(any(), any(), any()) }
        verify { notificationManager.showCallNotification(CallNotification(CONNECTING, MediaDirection.NONE, true)) }
        verify { notificationManager.showCallNotification(audioCall) }
    }

    @Test
    fun `starts the service with the connecting text when the visitor accepts an audio upgrade`() = runManagerTest {
        engagementState.onNext(State.EngagementStarted(isCallVisualizer = false))

        upgradeAcceptResult.onNext(Result.success(offer(audio = MediaDirection.TWO_WAY, video = MediaDirection.NONE)))

        verify(exactly = 1) {
            notificationManager.startCallService(CallNotification(CONNECTING, MediaDirection.NONE, true), true, false)
        }
    }

    @Test
    fun `adds the camera type when the visitor accepts a two-way video upgrade during an audio call`() = runManagerTest {
        startAudioMedia()

        upgradeAcceptResult.onNext(Result.success(offer(audio = MediaDirection.TWO_WAY, video = MediaDirection.TWO_WAY)))
        setAppVisible(false)
        startVideoMedia(visitorVideo = true)

        verify(exactly = 1) { notificationManager.startCallService(audioCall, true, false) }
        verify(exactly = 1) { notificationManager.startCallService(audioCall, true, true) }
        verify { notificationManager.showCallNotification(CallNotification(LIVE, MediaDirection.TWO_WAY, true)) }
    }

    @Test
    fun `does not start the service when accepting the upgrade fails`() = runManagerTest {
        engagementState.onNext(State.EngagementStarted(isCallVisualizer = false))

        upgradeAcceptResult.onNext(Result.failure(RuntimeException("failed")))

        verifyNothingShown()
    }

    @Test
    fun `does not ask for the microphone type in a Call Visualizer video call without audio`() = runManagerTest {
        engagementState.onNext(State.EngagementStarted(isCallVisualizer = true))

        upgradeAcceptResult.onNext(Result.success(offer(audio = MediaDirection.NONE, video = MediaDirection.ONE_WAY)))
        operatorMedia.onNext(Data.Value(mediaState(audio = null, video = mockk())))

        verify(exactly = 0) { notificationManager.startCallService(any(), any(), any()) }
        verify { notificationManager.showCallNotification(CallNotification(LIVE, MediaDirection.ONE_WAY, false)) }
    }

    @Test
    fun `uses only the camera type in a two-way Call Visualizer video call without audio`() = runManagerTest {
        engagementState.onNext(State.EngagementStarted(isCallVisualizer = true))

        visitorMedia.onNext(Data.Value(mediaState(audio = null, video = mockk())))
        operatorMedia.onNext(Data.Value(mediaState(audio = null, video = mockk())))

        verify(exactly = 1) { notificationManager.startCallService(any(), any(), any()) }
        verify { notificationManager.startCallService(CallNotification(LIVE, MediaDirection.TWO_WAY, false), false, true) }
    }

    @Test
    fun `does not start the service without the microphone permission`() = runManagerTest {
        grantedPermissions.clear()

        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))

        verify(exactly = 0) { notificationManager.startCallService(any(), any(), any()) }
        verify { notificationManager.showCallNotification(CallNotification(WAITING, MediaDirection.NONE, true)) }
    }

    @Test
    fun `starts the service for a restored call once the app is visible`() = runManagerTest {
        setAppVisible(false)

        startAudioMedia()

        verify(exactly = 0) { notificationManager.startCallService(any(), any(), any()) }

        appBecomesVisible()

        verify(exactly = 1) { notificationManager.startCallService(audioCall, true, false) }
    }

    @Test
    fun `does not restart the service when the app becomes visible again`() = runManagerTest {
        startAudioMedia()

        appBecomesVisible()

        verify(exactly = 1) { notificationManager.startCallService(any(), any(), any()) }
    }

    @Test
    fun `stops the service when the call ends while the app is in the background`() = runManagerTest {
        startAudioMedia()
        setAppVisible(false)

        endCall()

        verify(exactly = 1) { notificationManager.removeCallNotification() }
    }

    @Test
    fun `stops the service when the call is downgraded to chat`() = runManagerTest {
        engagementState.onNext(State.EngagementStarted(isCallVisualizer = false))
        startAudioMedia()

        visitorMedia.onNext(Data.Value(mediaState(audio = null, video = null)))
        operatorMedia.onNext(Data.Value(mediaState(audio = null, video = null)))

        verify(exactly = 1) { notificationManager.removeCallNotification() }
    }

    @Test
    fun `does not start the service when the call ends before the app is visible again`() = runManagerTest {
        setAppVisible(false)
        startAudioMedia()
        endCall()

        appBecomesVisible()

        verify(exactly = 0) { notificationManager.startCallService(any(), any(), any()) }
    }

    @Test
    fun `ignores media combinations that only exist while one side is still updating`() = runManagerTest {
        startAudioMedia()

        operatorMedia.onNext(Data.Empty)

        verify(exactly = 0) { notificationManager.removeCallNotification() }
    }

    @Test
    fun `waits for the next activity start when the last activity stopped before the request`() = runManagerTest {
        setAppVisible(true)
        setAppVisible(false)
        setAppVisible(false)

        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))

        verify(exactly = 0) { notificationManager.startCallService(any(), any(), any()) }

        appBecomesVisible()

        verify(exactly = 1) { notificationManager.startCallService(CallNotification(WAITING, MediaDirection.NONE, true), true, false) }
    }

    @Test
    fun `retries a refused start on the next activity start`() = runManagerTest {
        every { notificationManager.startCallService(any(), any(), any()) } returnsMany listOf(false, true)
        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))

        appBecomesVisible()

        verify(exactly = 2) { notificationManager.startCallService(CallNotification(WAITING, MediaDirection.NONE, true), true, false) }
    }

    @Test
    fun `does not retry a refused start until an activity starts`() = runManagerTest {
        every { notificationManager.startCallService(any(), any(), any()) } returns false
        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))

        engagementState.onNext(State.Queuing("ticket", MediaType.AUDIO))
        engagementState.onNext(State.EngagementStarted(isCallVisualizer = false))

        verify(exactly = 1) { notificationManager.startCallService(any(), any(), any()) }
        verify { notificationManager.showCallNotification(CallNotification(CONNECTING, MediaDirection.NONE, true)) }
    }

    @Test
    fun `starts the service again on the next activity start after it stopped on its own`() = runManagerTest {
        startAudioMedia()

        manager.onCallServiceStopped()
        appBecomesVisible()

        verify(exactly = 2) { notificationManager.startCallService(audioCall, true, false) }
    }

    @Test
    fun `does not treat a service that stopped on its own as running`() = runManagerTest {
        startAudioMedia()
        manager.onCallServiceStopped()
        setAppVisible(false)

        startVideoMedia(visitorVideo = false)

        verify(exactly = 1) { notificationManager.startCallService(any(), any(), any()) }
        verify { notificationManager.showCallNotification(CallNotification(LIVE, MediaDirection.ONE_WAY, true)) }
    }

    @Test
    fun `starts the service while one of two started activities is still visible`() = runManagerTest {
        setAppVisible(true)
        setAppVisible(false)

        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))

        verify(exactly = 1) { notificationManager.startCallService(CallNotification(WAITING, MediaDirection.NONE, true), true, false) }
    }

    @Test
    fun `an extra activity stop does not hide the next started activity`() = runManagerTest {
        setAppVisible(false)
        setAppVisible(false)
        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))

        verify(exactly = 0) { notificationManager.startCallService(any(), any(), any()) }

        appBecomesVisible()

        verify(exactly = 1) { notificationManager.startCallService(CallNotification(WAITING, MediaDirection.NONE, true), true, false) }
    }

    @Test
    fun `tries to start the service for a new call after the refused one ended`() = runManagerTest {
        every { notificationManager.startCallService(any(), any(), any()) } returnsMany listOf(false, true)
        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))
        engagementState.onNext(State.QueueingCanceled)

        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))

        verify(exactly = 2) { notificationManager.startCallService(any(), any(), any()) }
        verify(exactly = 1) { notificationManager.removeCallNotification() }
    }

    // Core keeps the audio while it rejoins, so the media either stays or is sent again unchanged.
    @Test
    fun `keeps the service and the notification through a rejoin`() = runManagerTest {
        startAudioMedia()
        setAppVisible(false)

        startAudioMedia()

        verify(exactly = 1) { notificationManager.startCallService(any(), any(), any()) }
        verify(exactly = 0) { notificationManager.showCallNotification(any()) }
        verify(exactly = 0) { notificationManager.removeCallNotification() }
    }

    @Test
    fun `keeps following the call after the notification manager throws`() = runManagerTest {
        grantedPermissions.clear()
        every { notificationManager.showCallNotification(any()) } throws SecurityException("refused") andThenJust Runs
        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))

        engagementState.onNext(State.EngagementStarted(isCallVisualizer = false))
        engagementState.onNext(State.EngagementEnded(EndAction.ClearStateRegular))

        verify(exactly = 1) { notificationManager.showCallNotification(CallNotification(CONNECTING, MediaDirection.NONE, true)) }
        // The connecting text was shown, so the end of the engagement removes it.
        verify(exactly = 1) { notificationManager.removeCallNotification() }
        verify(exactly = 0) { notificationManager.startCallService(any(), any(), any()) }
    }

    @Test
    fun `starts the service on the next activity start once the microphone permission is granted`() = runManagerTest {
        grantedPermissions.clear()
        engagementState.onNext(State.PreQueuing(MediaType.AUDIO))
        grantedPermissions.add(Manifest.permission.RECORD_AUDIO)

        verify(exactly = 0) { notificationManager.startCallService(any(), any(), any()) }

        appBecomesVisible()

        verify(exactly = 1) { notificationManager.startCallService(CallNotification(WAITING, MediaDirection.NONE, true), true, false) }
    }

    @Test
    fun `shows nothing when the service reports a stop after the call ended`() = runManagerTest {
        manager.onCallServiceStopped()

        verifyNothingShown()
    }

    private fun verifyNothingShown() {
        verify(exactly = 0) { notificationManager.startCallService(any(), any(), any()) }
        verify(exactly = 0) { notificationManager.showCallNotification(any()) }
        verify(exactly = 0) { notificationManager.removeCallNotification() }
    }

    // The manager's flow runs in the test's background scope, which ends with the test.
    // The unconfined dispatcher handles every event before the call that sent it returns.
    private fun runManagerTest(testBody: suspend TestScope.() -> Unit): TestResult = runTest(UnconfinedTestDispatcher()) {
        val engagementRepository: EngagementRepository = mockk {
            every { engagementState } returns this@OngoingCallNotificationManagerTest.engagementState
            every { visitorMediaState } returns visitorMedia
            every { operatorMediaState } returns operatorMedia
            every { onHoldState } returns Flowable.never()
            every { mediaUpgradeOfferAcceptResult } returns upgradeAcceptResult
        }
        val permissionManager = PermissionManager(mockk(), { _, permission ->
            if (permission in grantedPermissions) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
        }, mockk(), sdkInt = 34)
        manager = OngoingCallNotificationManager(
            EngagementStateUseCaseImpl(engagementRepository),
            VisitorMediaUseCaseImpl(engagementRepository),
            OperatorMediaUseCaseImpl(engagementRepository),
            AcceptMediaUpgradeOfferUseCaseImpl(engagementRepository),
            CallNotificationUseCase(),
            permissionManager,
            notificationManager,
            backgroundScope
        )
        setAppVisible(true)
        testBody()
    }

    // The manager never reads the activity, so a bare mock stands in for it.
    private fun setAppVisible(isVisible: Boolean) {
        if (isVisible) manager.onActivityStarted(mockk()) else manager.onActivityStopped(mockk())
    }

    private fun appBecomesVisible() = setAppVisible(true)

    private fun startAudioMedia() {
        visitorMedia.onNext(Data.Value(mediaState(audio = mockk(), video = null)))
        operatorMedia.onNext(Data.Value(mediaState(audio = mockk(), video = null)))
    }

    private fun startVideoMedia(visitorVideo: Boolean) {
        operatorMedia.onNext(Data.Value(mediaState(audio = mockk(), video = mockk())))
        visitorMedia.onNext(Data.Value(mediaState(audio = mockk(), video = if (visitorVideo) mockk() else null)))
    }

    private fun endCall() {
        operatorMedia.onNext(Data.Empty)
        visitorMedia.onNext(Data.Empty)
        engagementState.onNext(State.EngagementEnded(EndAction.ClearStateRegular))
    }

    private fun offer(audio: MediaDirection, video: MediaDirection): MediaUpgradeOffer = object : MediaUpgradeOffer("offer", audio, video) {
        override fun accept(callback: Consumer<GliaException>) = Unit
        override fun decline(callback: Consumer<GliaException>) = Unit
    }

    private fun mediaState(audio: Audio?, video: Video?): MediaState = mockk<MediaState>().also {
        every { it.audio } returns audio
        every { it.video } returns video
    }
}
