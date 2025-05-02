package com.glia.widgets.engagement.domain

import android.COMMON_EXTENSIONS_CLASS_PATH
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.glia.androidsdk.Operator
import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.comms.Video
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.core.screensharing.MEDIA_PROJECTION_SERVICE_ACTION_START
import com.glia.widgets.di.Dependencies
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.engagement.EndedBy
import com.glia.widgets.engagement.EngagementRepository
import com.glia.widgets.engagement.ScreenSharingState
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.formattedName
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.fileupload.FileAttachmentRepository
import com.glia.widgets.internal.notification.domain.CallNotificationUseCase
import com.glia.widgets.internal.permissions.PermissionManager
import com.glia.widgets.internal.secureconversations.SecureConversationsRepository
import com.glia.widgets.launcher.ConfigurationManager
import com.glia.widgets.permissions.Permissions
import com.glia.widgets.permissions.PermissionsGrantedCallback
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class EngagementDomainTest {

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `AcceptMediaUpgradeOfferUseCase will acceptMediaUpgradeRequest when invoked`() {
        val repository: EngagementRepository = mockk(relaxed = true)
        val mediaUpgradeOffer: MediaUpgradeOffer = mockk(relaxed = true)

        val useCase: AcceptMediaUpgradeOfferUseCase = AcceptMediaUpgradeOfferUseCaseImpl(engagementRepository = repository)
        verify { repository.mediaUpgradeOfferAcceptResult }
        useCase(mediaUpgradeOffer)

        verify { repository.acceptMediaUpgradeRequest(mediaUpgradeOffer) }
        confirmVerified(repository, mediaUpgradeOffer)
    }

    @Test
    fun `AcceptMediaUpgradeOfferUseCase result will emit result only when result is successful`() {
        val resultFlow = BehaviorProcessor.create<Result<MediaUpgradeOffer>>()
        val repository: EngagementRepository = mockk(relaxUnitFun = true) {
            every { mediaUpgradeOfferAcceptResult } returns resultFlow
        }
        val mediaUpgradeOffer: MediaUpgradeOffer = mockk(relaxed = true)

        val useCase: AcceptMediaUpgradeOfferUseCase = AcceptMediaUpgradeOfferUseCaseImpl(engagementRepository = repository)
        verify { repository.mediaUpgradeOfferAcceptResult }

        val resultTest = useCase.result.test()
        resultFlow.onNext(Result.success(mediaUpgradeOffer))
        resultFlow.onNext(Result.failure(RuntimeException()))

        resultTest.assertNotComplete().assertValue(mediaUpgradeOffer)

        confirmVerified(repository, mediaUpgradeOffer)
    }

    @Test
    fun `CheckMediaUpgradePermissionsUseCase will invoke callback when it invoked in PermissionManager`() {
        val callback = mockk<(granted: Boolean) -> Unit>(relaxed = true)

        val additionalPermissions = listOf(Manifest.permission.BLUETOOTH_CONNECT)
        val requiredPermissions = listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        val permissions = Permissions(requiredPermissions, additionalPermissions)
        val offer: MediaUpgradeOffer = mockk(relaxed = true)
        val permissionManager: PermissionManager = mockk {
            every { getPermissionsForMediaUpgradeOffer(offer) } returns permissions
            every {
                handlePermissions(
                    necessaryPermissions = requiredPermissions,
                    additionalPermissions = additionalPermissions,
                    necessaryPermissionsGrantedCallback = captureLambda()
                )
            } answers {
                thirdArg<PermissionsGrantedCallback>().invoke(true)
            }
        }
        val useCase: CheckMediaUpgradePermissionsUseCase = CheckMediaUpgradePermissionsUseCaseImpl(permissionManager)

        useCase.invoke(offer, callback)

        verify { permissionManager.getPermissionsForMediaUpgradeOffer(offer) }

        verify {
            permissionManager.handlePermissions(
                necessaryPermissions = requiredPermissions,
                additionalPermissions = additionalPermissions,
                necessaryPermissionsGrantedCallback = any()
            )
        }

        verify { callback.invoke(true) }

        confirmVerified(permissionManager, callback)
    }

    @Test
    fun `CurrentOperatorUseCase invoke will emit value only when data is present`() {
        mockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
        val operatorName = "OperatorName"

        val operator = mockk<Operator>(relaxUnitFun = true)

        every { any<Operator>().formattedName } returns operatorName

        val operatorSubject = BehaviorProcessor.create<Data<Operator>>()

        val repository: EngagementRepository = mockk(relaxUnitFun = true) {
            every { currentOperator } returns operatorSubject
            every { currentOperatorValue } answers { (operatorSubject.value as? Data.Value<Operator>)?.result }
        }

        val useCase: CurrentOperatorUseCase = CurrentOperatorUseCaseImpl(engagementRepository = repository)
        val currentOperator = useCase()
        val operatorNameFlow = useCase.formattedName

        verify { repository.currentOperator }
        verify(exactly = 0) { repository.currentOperatorValue }

        operatorSubject.onNext(Data.Empty)
        operatorSubject.onNext(Data.Empty)
        operatorSubject.onNext(Data.Empty)

        currentOperator.test().assertNoValues()
        operatorNameFlow.test().assertNoValues()
        assertNull(useCase.currentOperatorValue)
        assertNull(useCase.formattedNameValue)

        operatorSubject.onNext(Data.Value(operator))

        currentOperator.test().assertValue(operator)

        operatorNameFlow.test().assertValue(operatorName)

        assertEquals(operator, useCase.currentOperatorValue)
        assertEquals(operatorName, useCase.formattedNameValue)
        verify(exactly = 4) { repository.currentOperatorValue }

        confirmVerified(repository)
        unmockkStatic(COMMON_EXTENSIONS_CLASS_PATH)
    }

    @Test
    fun `EndEngagementUseCase invoke will call cancelQueuing when isQueueing is true`() {
        val repository: EngagementRepository = mockk(relaxed = true) {
            every { isQueueing } returns true
        }

        val useCase: EndEngagementUseCase = EndEngagementUseCaseImpl(engagementRepository = repository)

        useCase(EndedBy.OPERATOR)

        verify { repository.isQueueing }
        verify { repository.cancelQueuing() }
        verify(exactly = 0) { repository.endEngagement(any()) }

        confirmVerified(repository)
    }

    @Test
    fun `EndEngagementUseCase invoke will call endEngagement when isQueueing is false`() {
        val repository: EngagementRepository = mockk(relaxed = true) {
            every { isQueueing } returns false
        }

        val useCase: EndEngagementUseCase = EndEngagementUseCaseImpl(engagementRepository = repository)

        useCase(EndedBy.OPERATOR)

        verify { repository.isQueueing }
        verify { repository.endEngagement(eq(EndedBy.OPERATOR)) }
        verify(exactly = 0) { repository.cancelQueuing() }

        confirmVerified(repository)
    }

    @Test
    fun `EngagementTypeUseCase isCallVisualizerScreenSharing returns true when current engagement is call visualizer`() {
        val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase = mockk(relaxUnitFun = true)
        val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase = mockk(relaxUnitFun = true)
        val screenSharingUseCase: ScreenSharingUseCase = mockk(relaxUnitFun = true)
        val operatorMediaUseCase: OperatorMediaUseCase = mockk(relaxUnitFun = true)
        val visitorMediaUseCase: VisitorMediaUseCase = mockk(relaxUnitFun = true)
        val isOperatorPresentUseCase: IsOperatorPresentUseCase = mockk(relaxUnitFun = true)

        every { isCurrentEngagementCallVisualizerUseCase() } returns true
        every { visitorMediaUseCase.hasMedia } returns false
        every { operatorMediaUseCase.hasMedia } returns false
        every { screenSharingUseCase.isSharing } returns true

        val useCase: EngagementTypeUseCase = EngagementTypeUseCaseImpl(
            isQueueingOrLiveEngagementUseCase = isQueueingOrLiveEngagementUseCase,
            isCurrentEngagementCallVisualizerUseCase = isCurrentEngagementCallVisualizerUseCase,
            screenSharingUseCase = screenSharingUseCase,
            operatorMediaUseCase = operatorMediaUseCase,
            visitorMediaUseCase = visitorMediaUseCase,
            isOperatorPresentUseCase = isOperatorPresentUseCase
        )

        assertTrue(useCase.isCallVisualizerScreenSharing)
    }

    @Test
    fun `EngagementTypeUseCase isCallVisualizerScreenSharing returns true even when engagement has media`() {
        val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase = mockk(relaxUnitFun = true)
        val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase = mockk(relaxUnitFun = true)
        val screenSharingUseCase: ScreenSharingUseCase = mockk(relaxUnitFun = true)
        val operatorMediaUseCase: OperatorMediaUseCase = mockk(relaxUnitFun = true)
        val visitorMediaUseCase: VisitorMediaUseCase = mockk(relaxUnitFun = true)
        val isOperatorPresentUseCase: IsOperatorPresentUseCase = mockk(relaxUnitFun = true)

        every { isCurrentEngagementCallVisualizerUseCase() } returns true
        every { visitorMediaUseCase.hasMedia } returns true
        every { operatorMediaUseCase.hasMedia } returns true
        every { screenSharingUseCase.isSharing } returns true

        val useCase: EngagementTypeUseCase = EngagementTypeUseCaseImpl(
            isQueueingOrLiveEngagementUseCase = isQueueingOrLiveEngagementUseCase,
            isCurrentEngagementCallVisualizerUseCase = isCurrentEngagementCallVisualizerUseCase,
            screenSharingUseCase = screenSharingUseCase,
            operatorMediaUseCase = operatorMediaUseCase,
            visitorMediaUseCase = visitorMediaUseCase,
            isOperatorPresentUseCase = isOperatorPresentUseCase
        )

        assertTrue(useCase.isCallVisualizerScreenSharing)
    }

    @Test
    fun `EngagementTypeUseCase isChatEngagement returns true when engagement has no media, is not a cv and operator is present`() {
        val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase = mockk(relaxUnitFun = true)
        val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase = mockk(relaxUnitFun = true)
        val screenSharingUseCase: ScreenSharingUseCase = mockk(relaxUnitFun = true)
        val operatorMediaUseCase: OperatorMediaUseCase = mockk(relaxUnitFun = true)
        val visitorMediaUseCase: VisitorMediaUseCase = mockk(relaxUnitFun = true)
        val isOperatorPresentUseCase: IsOperatorPresentUseCase = mockk(relaxUnitFun = true)

        every { isCurrentEngagementCallVisualizerUseCase() } returns false
        every { visitorMediaUseCase.hasMedia } returns false
        every { operatorMediaUseCase.hasMedia } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        every { isOperatorPresentUseCase() } returns true

        val useCase: EngagementTypeUseCase = EngagementTypeUseCaseImpl(
            isQueueingOrLiveEngagementUseCase = isQueueingOrLiveEngagementUseCase,
            isCurrentEngagementCallVisualizerUseCase = isCurrentEngagementCallVisualizerUseCase,
            screenSharingUseCase = screenSharingUseCase,
            operatorMediaUseCase = operatorMediaUseCase,
            visitorMediaUseCase = visitorMediaUseCase,
            isOperatorPresentUseCase = isOperatorPresentUseCase
        )

        assertTrue(useCase.isChatEngagement)
    }

    @Test
    fun `EngagementTypeUseCase isMediaEngagement returns true when engagement has  media and operator is present`() {
        val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase = mockk(relaxUnitFun = true)
        val isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase = mockk(relaxUnitFun = true)
        val screenSharingUseCase: ScreenSharingUseCase = mockk(relaxUnitFun = true)
        val operatorMediaUseCase: OperatorMediaUseCase = mockk(relaxUnitFun = true)
        val visitorMediaUseCase: VisitorMediaUseCase = mockk(relaxUnitFun = true)
        val isOperatorPresentUseCase: IsOperatorPresentUseCase = mockk(relaxUnitFun = true)

        every { visitorMediaUseCase.hasMedia } returns true
        every { operatorMediaUseCase.hasMedia } returns false
        every { isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement } returns true
        every { isOperatorPresentUseCase() } returns true

        val useCase: EngagementTypeUseCase = EngagementTypeUseCaseImpl(
            isQueueingOrLiveEngagementUseCase = isQueueingOrLiveEngagementUseCase,
            isCurrentEngagementCallVisualizerUseCase = isCurrentEngagementCallVisualizerUseCase,
            screenSharingUseCase = screenSharingUseCase,
            operatorMediaUseCase = operatorMediaUseCase,
            visitorMediaUseCase = visitorMediaUseCase,
            isOperatorPresentUseCase = isOperatorPresentUseCase
        )

        assertTrue(useCase.isMediaEngagement)
    }

    @Test
    fun `EnqueueForEngagementUseCase invoke enqueues with selected type engagement when media type is present`() {
        val engagementRepository: EngagementRepository = mockk(relaxUnitFun = true) {
            every { isTransferredSecureConversation } returns false
        }
        val secureConversationsRepository: SecureConversationsRepository = mockk(relaxUnitFun = true) {
            every { hasPendingSecureConversations } returns false
        }
        val mediaType: MediaType = mockk(relaxUnitFun = true)

        val useCase: EnqueueForEngagementUseCase = EnqueueForEngagementUseCaseImpl(
            engagementRepository = engagementRepository,
            secureConversationsRepository = secureConversationsRepository
        )

        useCase(mediaType)

        verify { secureConversationsRepository.hasPendingSecureConversations }
        verify { engagementRepository.isTransferredSecureConversation }
        verify { engagementRepository.queueForEngagement(mediaType, eq(false)) }
    }

    @Test
    fun `EnqueueForEngagementUseCase invoke enqueues with replaceExisting = true when is transferred SC`() {
        val engagementRepository: EngagementRepository = mockk(relaxUnitFun = true) {
            every { isTransferredSecureConversation } returns true
        }
        val secureConversationsRepository: SecureConversationsRepository = mockk(relaxUnitFun = true) {
            every { hasPendingSecureConversations } returns false
        }
        val mediaType: MediaType = mockk(relaxUnitFun = true)

        val useCase: EnqueueForEngagementUseCase = EnqueueForEngagementUseCaseImpl(
            engagementRepository = engagementRepository,
            secureConversationsRepository = secureConversationsRepository
        )

        useCase(mediaType)

        verify { secureConversationsRepository.hasPendingSecureConversations }
        verify { engagementRepository.isTransferredSecureConversation }
        verify { engagementRepository.queueForEngagement(mediaType, eq(true)) }
    }

    @Test
    fun `EnqueueForEngagementUseCase invoke enqueues with replaceExisting = true when has pending SC`() {
        val engagementRepository: EngagementRepository = mockk(relaxUnitFun = true) {
            every { isTransferredSecureConversation } returns false
        }
        val secureConversationsRepository: SecureConversationsRepository = mockk(relaxUnitFun = true) {
            every { hasPendingSecureConversations } returns true
        }
        val mediaType: MediaType = mockk(relaxUnitFun = true)

        val useCase: EnqueueForEngagementUseCase = EnqueueForEngagementUseCaseImpl(
            engagementRepository = engagementRepository,
            secureConversationsRepository = secureConversationsRepository
        )

        useCase(mediaType)

        verify { secureConversationsRepository.hasPendingSecureConversations }
        verify(inverse = true) { engagementRepository.isTransferredSecureConversation }
        verify { engagementRepository.queueForEngagement(mediaType, eq(true)) }
    }

    @Test
    fun `IsQueueingOrEngagementUseCase isQueueingForChat returns true when is not queueing for media`() {
        val repository: EngagementRepository = mockk(relaxUnitFun = true) {
            every { hasOngoingLiveEngagement } returns false
            every { isQueueing } returns true
            every { isQueueingForMedia } returns false
            every { isQueueingOrLiveEngagement } returns true
        }

        val useCase: IsQueueingOrLiveEngagementUseCase = IsQueueingOrLiveEngagementUseCaseImpl(engagementRepository = repository)

        assertTrue(useCase.isQueueingForLiveChat)
        assertTrue(useCase())
        assertFalse(useCase.isQueueingForMedia)
        assertFalse(useCase.hasOngoingLiveEngagement)
    }

    @Test
    fun `StartMediaProjectionServiceUseCase invoke sets proper action to intent`() {
        val context: Context = mockk(relaxed = true)
        val useCase: StartMediaProjectionServiceUseCase = StartMediaProjectionServiceUseCaseImpl(context)

        useCase()

        val intentSlot = slot<Intent>()

        verify { context.startForegroundService(capture(intentSlot)) }

        assertEquals(MEDIA_PROJECTION_SERVICE_ACTION_START, intentSlot.captured.action)
    }

    @Test
    fun `OperatorMediaUpgradeOfferUseCase invoke will emit data only when data is present`() {
        val operatorName = "OperatorName"
        val mediaUpgradeOffer = mockk<MediaUpgradeOffer>(relaxUnitFun = true)

        val operatorNameSubject = BehaviorProcessor.createDefault(operatorName)
        val mediaUpgradeOfferSubject = BehaviorProcessor.create<MediaUpgradeOffer>()

        val engagementRepository: EngagementRepository = mockk(relaxUnitFun = true) {
            every { this@mockk.mediaUpgradeOffer } returns mediaUpgradeOfferSubject
        }

        val currentOperatorUseCase: CurrentOperatorUseCase = mockk(relaxUnitFun = true) {
            every { formattedName } returns operatorNameSubject
        }
        val operatorMediaUpgradeOfferUseCase: OperatorMediaUpgradeOfferUseCase = OperatorMediaUpgradeOfferUseCaseImpl(
            engagementRepository = engagementRepository,
            currentOperatorUseCase = currentOperatorUseCase
        )

        val mediaUpgradeOfferDataFlow = operatorMediaUpgradeOfferUseCase()

        verify { engagementRepository.mediaUpgradeOffer }
        verify { currentOperatorUseCase.formattedName }

        mediaUpgradeOfferDataFlow.test().assertNoValues()

        mediaUpgradeOfferSubject.onNext(mediaUpgradeOffer)

        mediaUpgradeOfferDataFlow.test().assertValue(MediaUpgradeOfferData(mediaUpgradeOffer, operatorName))

        confirmVerified(engagementRepository)
    }

    @Test
    fun `OperatorMediaUseCase invoke will emit empty media state even when data is absent`() {
        val video = mockk<Video>(relaxUnitFun = true)

        val mediaState = mockk<MediaState>(relaxUnitFun = true) {
            every { this@mockk.video } returns video
            every { audio } returns null
        }

        val mediaStateSubject = BehaviorProcessor.create<Data<MediaState>>()

        val engagementRepository: EngagementRepository = mockk(relaxUnitFun = true) {
            every { operatorMediaState } returns mediaStateSubject
            every { operatorCurrentMediaState } answers { (mediaStateSubject.value as? Data.Value<MediaState>)?.result }
        }

        val operatorMediaUseCase: OperatorMediaUseCase = OperatorMediaUseCaseImpl(engagementRepository = engagementRepository)

        val mediaStateFlow = operatorMediaUseCase()

        verify { engagementRepository.operatorMediaState }

        assertFalse(operatorMediaUseCase.hasMedia)

        mediaStateSubject.onNext(Data.Empty)
        mediaStateSubject.onNext(Data.Empty)
        mediaStateSubject.onNext(Data.Empty)

        assertFalse(operatorMediaUseCase.hasMedia)

        mediaStateSubject.onNext(Data.Value(mediaState))

        mediaStateFlow.test().assertValue(mediaState)
        assertTrue(operatorMediaUseCase.hasMedia)

        verify { engagementRepository.operatorCurrentMediaState }

        confirmVerified(engagementRepository)
    }

    @Test
    fun `VisitorMediaUseCase invoke will emit empty media state even when data is absent`() {
        val video = mockk<Video>(relaxUnitFun = true)

        val mediaState = mockk<MediaState>(relaxUnitFun = true) {
            every { this@mockk.video } returns video
            every { audio } returns null
        }

        val mediaStateSubject = BehaviorProcessor.create<Data<MediaState>>()

        val engagementRepository: EngagementRepository = mockk(relaxUnitFun = true) {
            every { visitorMediaState } returns mediaStateSubject
            every { visitorCurrentMediaState } answers { (mediaStateSubject.value as? Data.Value<MediaState>)?.result }
            every { onHoldState } returns Flowable.just(false)
        }

        val visitorMediaUseCase: VisitorMediaUseCase = VisitorMediaUseCaseImpl(engagementRepository = engagementRepository)

        val mediaStateFlow = visitorMediaUseCase()

        verify { engagementRepository.visitorMediaState }

        assertFalse(visitorMediaUseCase.hasMedia)

        mediaStateSubject.onNext(Data.Empty)
        mediaStateSubject.onNext(Data.Empty)
        mediaStateSubject.onNext(Data.Empty)

        assertFalse(visitorMediaUseCase.hasMedia)

        mediaStateSubject.onNext(Data.Value(mediaState))

        mediaStateFlow.test().assertValue(mediaState)
        assertTrue(visitorMediaUseCase.hasMedia)
        visitorMediaUseCase.onHoldState.test().assertValue(false)

        verify { engagementRepository.visitorCurrentMediaState }
        verify { engagementRepository.onHoldState }

        confirmVerified(engagementRepository)
    }

    @Test
    fun `ReleaseResourcesUseCase invoke will release all the resources when called`() {
        mockkStatic(Dependencies::class)

        every { Dependencies.destroyControllers() } just Runs
        val releaseScreenSharingResourcesUseCase: ReleaseScreenSharingResourcesUseCase = mockk(relaxUnitFun = true)
        val callNotificationUseCase: CallNotificationUseCase = mockk(relaxUnitFun = true)
        val fileAttachmentRepository: FileAttachmentRepository = mockk(relaxUnitFun = true)
        val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase = mockk(relaxUnitFun = true)
        val dialogController: DialogContract.Controller = mockk(relaxUnitFun = true)

        val useCase: ReleaseResourcesUseCase = ReleaseResourcesUseCaseImpl(
            releaseScreenSharingResourcesUseCase = releaseScreenSharingResourcesUseCase,
            callNotificationUseCase = callNotificationUseCase,
            fileAttachmentRepository = fileAttachmentRepository,
            updateFromCallScreenUseCase = updateFromCallScreenUseCase,
            dialogController = dialogController
        )

        useCase()

        verifyOrder {
            dialogController.dismissDialogs()
            fileAttachmentRepository.detachAllFiles()
            releaseScreenSharingResourcesUseCase()
            callNotificationUseCase.removeAllNotifications()
            updateFromCallScreenUseCase(false)
            Dependencies.destroyControllers()
        }

        unmockkStatic(Dependencies::class)
    }

    @Test
    fun `ScreenSharingUseCase test`() {
        val mode = ScreenSharing.Mode.UNBOUNDED
        val engagementRepository: EngagementRepository = mockk(relaxUnitFun = true)
        val releaseScreenSharingResourcesUseCase: ReleaseScreenSharingResourcesUseCase = mockk(relaxUnitFun = true)

        val configurationManager = mockk<ConfigurationManager> {
            every { screenSharingMode } returns mode
        }
        val useCase: ScreenSharingUseCase = ScreenSharingUseCaseImpl(
            engagementRepository = engagementRepository,
            releaseScreenSharingResourcesUseCase = releaseScreenSharingResourcesUseCase,
            configurationManager
        )

        every { engagementRepository.isSharingScreen } returns true
        every { engagementRepository.screenSharingState } returns Flowable.just(ScreenSharingState.Started)

        assertTrue(useCase.isSharing)
        assertEquals(ScreenSharingState.Started, useCase().blockingFirst())

        verify { engagementRepository.isSharingScreen }
        verify { engagementRepository.screenSharingState }

        useCase.end()
        verify { engagementRepository.endScreenSharing() }
        verify { releaseScreenSharingResourcesUseCase() }

        useCase.declineRequest()
        verify { engagementRepository.declineScreenSharingRequest() }

        val activity: Activity = mockk(relaxUnitFun = true)
        useCase.acceptRequestWithAskedPermission(activity)
        verify { engagementRepository.acceptScreenSharingWithAskedPermission(activity, mode) }

        val resultCode = 1
        val intent: Intent = mockk(relaxUnitFun = true)
        useCase.onActivityResultSkipPermissionRequest(resultCode, intent)
        verify { engagementRepository.onActivityResultSkipScreenSharingPermissionRequest(resultCode, intent) }

        confirmVerified(engagementRepository, releaseScreenSharingResourcesUseCase)
    }

    @Test
    fun `ToggleVisitorAudioMediaStateUseCase invoke mutes visitor audio when audio is playing`() {
        val repository: EngagementRepository = mockk(relaxed = true) {
            every { visitorCurrentMediaState?.audio?.status } returns Media.Status.PLAYING
        }

        val useCase: ToggleVisitorAudioMediaStateUseCase = ToggleVisitorAudioMediaStateUseCaseImpl(repository)

        useCase()

        verify { repository.visitorCurrentMediaState }
        verify { repository.muteVisitorAudio() }
        verify(exactly = 0) { repository.unMuteVisitorAudio() }

        confirmVerified(repository)
    }

    @Test
    fun `ToggleVisitorAudioMediaStateUseCase invoke resumes visitor audio when audio is paused`() {
        val repository: EngagementRepository = mockk(relaxed = true) {
            every { visitorCurrentMediaState?.audio?.status } returns Media.Status.PAUSED
        }

        val useCase: ToggleVisitorAudioMediaStateUseCase = ToggleVisitorAudioMediaStateUseCaseImpl(repository)

        useCase()

        verify { repository.visitorCurrentMediaState }
        verify { repository.unMuteVisitorAudio() }
        verify(exactly = 0) { repository.muteVisitorAudio() }

        confirmVerified(repository)
    }

    @Test
    fun `ToggleVisitorAudioMediaStateUseCase invoke does nothing when audio state is null`() {
        val repository: EngagementRepository = mockk(relaxed = true) {
            every { visitorCurrentMediaState?.audio?.status } returns null
        }

        val useCase: ToggleVisitorAudioMediaStateUseCase = ToggleVisitorAudioMediaStateUseCaseImpl(repository)

        useCase()

        verify { repository.visitorCurrentMediaState }
        verify(exactly = 0) { repository.unMuteVisitorAudio() }
        verify(exactly = 0) { repository.muteVisitorAudio() }

        confirmVerified(repository)
    }

    @Test
    fun `ToggleVisitorVideoMediaStateUseCase invoke pauses visitor video when video is playing`() {
        val repository: EngagementRepository = mockk(relaxed = true) {
            every { visitorCurrentMediaState?.video?.status } returns Media.Status.PLAYING
        }

        val useCase: ToggleVisitorVideoMediaStateUseCase = ToggleVisitorVideoMediaStateUseCaseImpl(repository)

        useCase()

        verify { repository.visitorCurrentMediaState }
        verify { repository.pauseVisitorVideo() }
        verify(exactly = 0) { repository.resumeVisitorVideo() }

        confirmVerified(repository)
    }

    @Test
    fun `ToggleVisitorVideoMediaStateUseCase invoke resumes visitor video when video is paused`() {
        val repository: EngagementRepository = mockk(relaxed = true) {
            every { visitorCurrentMediaState?.video?.status } returns Media.Status.PAUSED
        }

        val useCase: ToggleVisitorVideoMediaStateUseCase = ToggleVisitorVideoMediaStateUseCaseImpl(repository)

        useCase()

        verify { repository.visitorCurrentMediaState }
        verify { repository.resumeVisitorVideo() }
        verify(exactly = 0) { repository.pauseVisitorVideo() }

        confirmVerified(repository)
    }

    @Test
    fun `ToggleVisitorVideoMediaStateUseCase invoke does nothing when video state is null`() {
        val repository: EngagementRepository = mockk(relaxed = true) {
            every { visitorCurrentMediaState?.video?.status } returns null
        }

        val useCase: ToggleVisitorVideoMediaStateUseCase = ToggleVisitorVideoMediaStateUseCaseImpl(repository)

        useCase()

        verify { repository.visitorCurrentMediaState }
        verify(exactly = 0) { repository.resumeVisitorVideo() }
        verify(exactly = 0) { repository.pauseVisitorVideo() }

        confirmVerified(repository)
    }
}
