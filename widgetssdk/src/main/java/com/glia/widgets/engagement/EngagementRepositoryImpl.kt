package com.glia.widgets.engagement

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Engagement.MediaType
import com.glia.androidsdk.EngagementRequest
import com.glia.androidsdk.EngagementRequest.Outcome
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.IncomingEngagementRequest
import com.glia.androidsdk.Operator
import com.glia.androidsdk.chat.Chat
import com.glia.androidsdk.chat.OperatorTypingStatus
import com.glia.androidsdk.comms.CameraDevice
import com.glia.androidsdk.comms.Media
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.comms.OperatorMediaState
import com.glia.androidsdk.comms.VisitorMediaState
import com.glia.androidsdk.engagement.EngagementState
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
import com.glia.widgets.helper.isAudioOrVideo
import com.glia.widgets.helper.isQueueUnavailable
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.launcher.ConfigurationManager
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.processors.PublishProcessor
import java.util.function.Consumer

private const val TAG = "EngagementRepository"

@VisibleForTesting
internal const val MEDIA_PERMISSION_REQUEST_CODE = 0x3E9

@VisibleForTesting
internal const val SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE = 0x1995

internal class EngagementRepositoryImpl(
    private val core: GliaCore,
    private val operatorRepository: GliaOperatorRepository,
    private val queueRepository: QueueRepository,
    private val configurationManager: ConfigurationManager
) : EngagementRepository {
    private val engagementRequestCallback = Consumer<IncomingEngagementRequest>(::handleIncomingEngagementRequest)
    private val engagementOutcomeCallback = Consumer<Outcome>(::handleEngagementOutcome)

    private val omniCoreEngagementCallback = Consumer<OmnicoreEngagement>(::handleOmniCoreEngagement)
    private val callVisualizerEngagementCallback = Consumer<OmnibrowseEngagement>(::handleCallVisualizerEngagement)
    private val engagementStateCallback: Consumer<EngagementState> = Consumer(::handleEngagementState)
    private val engagementEndCallback: Runnable = Runnable(::handleEngagementEnd)

    private var currentEngagement: Engagement? = null

    private val _engagementRequest: BehaviorProcessor<IncomingEngagementRequest> = BehaviorProcessor.create()
    override val engagementRequest: Flowable<IncomingEngagementRequest> = _engagementRequest.onBackpressureLatest()

    private val _engagementOutcome: BehaviorProcessor<Outcome> = BehaviorProcessor.create()
    override val engagementOutcome: Flowable<Outcome> = _engagementOutcome.onBackpressureLatest()

    private val queueTicketCallback = Consumer<QueueTicket>(::handleQueueTicket)

    private val _engagementState: BehaviorProcessor<State> = BehaviorProcessor.createDefault(State.NoEngagement)
    override val engagementState: Flowable<State> = _engagementState.onBackpressureBuffer().distinctUntilChanged()

    private val _survey: PublishProcessor<SurveyState> = PublishProcessor.create()
    override val survey: Flowable<SurveyState> = _survey

    private val _currentOperator: BehaviorProcessor<Data<Operator>> = BehaviorProcessor.createDefault(Data.Empty)
    override val currentOperator: Flowable<Data<Operator>> = _currentOperator.onBackpressureLatest().distinctUntilChanged()
    override val currentOperatorValue: Operator? get() = with(_currentOperator.value as? Data.Value) { this?.result }
    override val isOperatorPresent: Boolean get() = currentOperatorValue != null
    private val currentState: State? get() = _engagementState.value

    //--Media--
    private val mediaUpgradeOfferCallback: Consumer<MediaUpgradeOffer> = Consumer(::handleMediaUpgradeOffer)
    private val operatorMediaStateUpdateCallback: Consumer<OperatorMediaState> = Consumer(::handleOperatorMediaStateUpdate)
    private val visitorMediaStateUpdateCallback: Consumer<VisitorMediaState> = Consumer(::handleVisitorMediaStateUpdate)

    private val _mediaUpgradeOffer: PublishProcessor<MediaUpgradeOffer> = PublishProcessor.create()
    override val mediaUpgradeOffer: Flowable<MediaUpgradeOffer> = _mediaUpgradeOffer.onBackpressureLatest()

    private val _mediaUpgradeOfferResult: PublishProcessor<Result<MediaUpgradeOffer>> = PublishProcessor.create()
    override val mediaUpgradeOfferAcceptResult: Flowable<Result<MediaUpgradeOffer>> = _mediaUpgradeOfferResult.onBackpressureLatest()

    private val _visitorMediaState: BehaviorProcessor<Data<MediaState>> = BehaviorProcessor.createDefault(Data.Empty)
    override val visitorMediaState: Flowable<Data<MediaState>> = _visitorMediaState
    override val visitorCurrentMediaState: MediaState? get() = _visitorMediaState.value?.let { it as? Data.Value }?.result

    private val _onHoldState: BehaviorProcessor<Boolean> = BehaviorProcessor.createDefault(false)
    override val onHoldState: Flowable<Boolean> = _onHoldState

    private val _operatorMediaState: BehaviorProcessor<Data<MediaState>> = BehaviorProcessor.createDefault(Data.Empty)
    override val operatorMediaState: Flowable<Data<MediaState>> = _operatorMediaState
    override val operatorCurrentMediaState: MediaState? get() = _operatorMediaState.value?.let { it as? Data.Value }?.result

    private val _visitorCameraState: BehaviorProcessor<VisitorCamera> = BehaviorProcessor.create()
    override val visitorCameraState: Flowable<VisitorCamera>
        get() = _visitorCameraState
    override val currentVisitorCamera: VisitorCamera
        get() = _visitorCameraState.value ?: VisitorCamera.NoCamera

    //--Chat--
    private val operatorTypingCallback: Consumer<OperatorTypingStatus> = Consumer(::handleOperatorTypingStatus)

    private val _operatorTypingStatus: PublishProcessor<Boolean> = PublishProcessor.create()
    override val operatorTypingStatus: Flowable<Boolean> = _operatorTypingStatus.distinctUntilChanged()

    //--Screen Sharing--
    private val screenSharingRequestCallback = Consumer<ScreenSharingRequest>(::handleScreenSharingRequest)
    private val screenSharingRequestResponseCallback = Consumer<GliaException>(::handleScreenSharingRequestResponse)
    private val screenSharingStateCallback = Consumer<VisitorScreenSharingState>(::handleScreenSharingState)

    private val _screenSharingState: BehaviorProcessor<ScreenSharingState> = BehaviorProcessor.create()
    override val screenSharingState: Flowable<ScreenSharingState> = _screenSharingState.onBackpressureLatest()

    private val readyToShareScreenProcessor: PublishProcessor<Unit> = PublishProcessor.create()
    private val mediaProjectionActivityResultProcessor: PublishProcessor<Pair<Int, Intent?>> = PublishProcessor.create()

    private var currentScreenSharingRequest: ScreenSharingRequest? = null
    private var currentScreenSharingScreen: LocalScreen? = null

    private val queueIngDisposable = CompositeDisposable()

    override val hasOngoingLiveEngagement: Boolean
        get() = currentEngagement != null && currentState?.isLiveEngagement == true

    override val isTransferredSecureConversation: Boolean
        get() = currentState is State.TransferredToSecureConversation

    override val isQueueing: Boolean
        get() = currentState?.isQueueing == true

    override val isQueueingForMedia: Boolean
        get() = currentState?.queueingMediaType?.isAudioOrVideo() == true

    override val isQueueingForAudio: Boolean
        get() = (currentState?.queueingMediaType == MediaType.AUDIO)

    override val isQueueingForVideo: Boolean
        get() = (currentState?.queueingMediaType == MediaType.VIDEO)

    override val isCallVisualizerEngagement: Boolean
        get() = currentEngagement is OmnibrowseEngagement

    override val isQueueingOrLiveEngagement: Boolean
        get() = isQueueing || hasOngoingLiveEngagement

    override val isSharingScreen: Boolean
        get() = currentEngagement != null && _screenSharingState.run { value == ScreenSharingState.Started || value == ScreenSharingState.RequestAccepted }

    private var _isSecureMessagingRequested: Boolean = false
    override val isSecureMessagingRequested: Boolean
        get() = _isSecureMessagingRequested

    override val cameras: List<CameraDevice>?
        get() = currentEngagement?.media?.cameraDevices

    override fun initialize() {
        core.on(Glia.Events.ENGAGEMENT, omniCoreEngagementCallback)
        core.on(Glia.Events.QUEUE_TICKET, queueTicketCallback)
        core.callVisualizer.on(Omnibrowse.Events.ENGAGEMENT, callVisualizerEngagementCallback)
        core.callVisualizer.on(Omnibrowse.Events.ENGAGEMENT_REQUEST, engagementRequestCallback)
        Flowable.zip(mediaProjectionActivityResultProcessor, readyToShareScreenProcessor) { result, _ ->
            /**
             * This function must be called only when both processors have emitted a value, in other words,
             * when the [onReadyToShareScreen] and the [onReadyToShareScreen] functions have been called
             */
            onActivityResult(SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE, result.first, result.second)
        }.subscribe()
    }

    override fun reset() {
        _isSecureMessagingRequested = false
        currentEngagement?.let { endEngagement(true) } ?: cancelQueuing()
    }

    override fun resetQueueing() {
        cancelQueuing()
    }

    private fun resetState() {
        _isSecureMessagingRequested = false
        _operatorMediaState.onNext(Data.Empty)
        _visitorMediaState.onNext(Data.Empty)
        _currentOperator.onNext(Data.Empty)
        _onHoldState.onNext(false)
        markScreenSharingEnded()
    }

    override fun endEngagement(silently: Boolean) {
        //This is required to not end the transferred engagement
        if (isTransferredSecureConversation) return

        currentEngagement?.also {
            currentEngagement = null
            Logger.i(TAG, "Engagement ended locally, silently:$silently")
            unsubscribeFromEvents(it)
            notifyEngagementEnded(it)
            it.end { ex -> ex?.also { Logger.d(TAG, "Ending engagement failed") } }
            if (silently || it is OmnibrowseEngagement) {
                _survey.onNext(SurveyState.Empty)
            } else {
                fetchSurvey(it, false)
            }
            resetState()
        }
    }

    override fun queueForEngagement(mediaType: MediaType) {
        if (isQueueingOrLiveEngagement) return

        _engagementState.onNext(State.PreQueuing(mediaType))

        Logger.i(TAG, "Start queueing for media engagement")

        queueIngDisposable.add(
            queueRepository.relevantQueueIds.subscribe { ids ->
                if (ids.isNotEmpty()) {
                    core.queueForEngagement(
                        ids,
                        mediaType,
                        configurationManager.visitorContextAssetId,
                        null,
                        MEDIA_PERMISSION_REQUEST_CODE
                    ) {
                        handleQueueingResponse(it)
                    }
                } else {
                    handleQueueingResponse(GliaException("relevant queues are empty", GliaException.Cause.INVALID_INPUT))
                }
            }
        )
    }

    override fun cancelQueuing() {
        (_engagementState.value as? State.PreQueuing)?.apply {
            queueIngDisposable.clear()
            _engagementState.onNext(State.QueueingCanceled)
            return
        }
        (_engagementState.value as? State.Queuing)?.apply {
            cancelQueueTicket(queueTicketId)
        }
    }

    private fun cancelQueueTicket(id: String) {
        Logger.i(TAG, "Cancel queue ticket")
        _engagementState.onNext(State.QueueingCanceled)
        core.cancelQueueTicket(id) {
            if (it == null) {
                Logger.d(TAG, "cancelQueueTicketSuccess")
            } else {
                Logger.e(TAG, "cancelQueueTicketError: $it")
            }
            _engagementState.onNext(State.NoEngagement)
        }
    }

    override fun acceptCurrentEngagementRequest(visitorContextAssetId: String) {
        _engagementRequest.firstOrError().flatMapCompletable { accept(it, visitorContextAssetId) }.unSafeSubscribe {
            Logger.i(TAG, "Incoming Call Visualizer engagement was accepted")
        }
    }

    private fun accept(request: IncomingEngagementRequest, visitorContextAssetId: String): Completable = Completable.create { emitter ->
        request.accept(visitorContextAssetId) { ex ->
            if (ex == null) {
                emitter.onComplete()
            } else {
                emitter.onError(ex)
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun declineCurrentEngagementRequest() {
        _engagementRequest.firstOrError().flatMapCompletable(::decline).subscribe({
            Logger.i(TAG, "Incoming Call Visualizer engagement was declined")
        }, {
            Logger.e(TAG, "Error during declining engagement request, reason" + it.message)
        })
    }

    private fun decline(request: IncomingEngagementRequest): Completable = Completable.create { emitter ->
        request.decline { ex ->
            if (ex == null) {
                emitter.onComplete()
            } else {
                emitter.onError(ex)
            }
        }
    }

    override fun acceptMediaUpgradeRequest(offer: MediaUpgradeOffer) {
        offer.accept {
            if (it == null) {
                Logger.d(TAG, "Media upgrade offer successfully accepted")
                _mediaUpgradeOfferResult.onNext(Result.success(offer))
            } else {
                Logger.d(TAG, "Failed to accept media upgrade offer")
                _mediaUpgradeOfferResult.onNext(Result.failure(it))
            }
        }
    }

    override fun declineMediaUpgradeRequest(offer: MediaUpgradeOffer) {
        offer.decline {
            if (it == null) {
                Logger.d(TAG, "Media upgrade offer successfully declined")
            } else {
                Logger.d(TAG, "Failed to decline media upgrade offer")
            }
        }
    }

    private fun fetchSurvey(engagement: Engagement, isOperator: Boolean) {
        engagement.getSurvey { survey, _ ->
            when {
                survey != null -> {
                    Logger.i(TAG, "Survey loaded")
                    _survey.onNext(SurveyState.Value(survey))
                }

                isOperator -> _survey.onNext(SurveyState.EmptyFromOperatorRequest)
                else -> _survey.onNext(SurveyState.Empty)
            }
        }
    }

    private fun handleIncomingEngagementRequest(engagementRequest: IncomingEngagementRequest) {
        engagementRequest.on(EngagementRequest.Events.OUTCOME, engagementOutcomeCallback)
        _engagementRequest.onNext(engagementRequest)
    }

    private fun handleEngagementOutcome(outcome: Outcome) {
        _engagementOutcome.onNext(outcome)
        _engagementRequest.firstOrError().unSafeSubscribe { request ->
            request.off(EngagementRequest.Events.OUTCOME, engagementOutcomeCallback)
        }
    }

    private fun handleQueueTicket(ticket: QueueTicket) {
        val currentState = _engagementState.value

        if (currentState is State.QueueingCanceled) {
            cancelQueueTicket(ticket.id)
            return
        }

        if (currentState is State.PreQueuing) {
            _engagementState.onNext(State.Queuing(ticket.id, currentState.mediaType))
            trackQueueTicketUpdates(ticket)
        }
    }

    private fun trackQueueTicketUpdates(ticket: QueueTicket) {
        core.subscribeToQueueTicketUpdates(ticket.id) { queueTicket, _ ->
            if (queueTicket?.state == QueueTicket.State.UNSTAFFED) {
                _engagementState.onNext(State.QueueUnstaffed)
                _engagementState.onNext(State.NoEngagement)
            }
        }
    }

    private fun handleQueueingResponse(exception: GliaException?) {
        when {
            exception == null || exception.cause == GliaException.Cause.ALREADY_QUEUED -> return

            exception.isQueueUnavailable -> {
                _engagementState.onNext(State.QueueUnstaffed)
                _engagementState.onNext(State.NoEngagement)
            }

            else -> {
                _engagementState.onNext(State.UnexpectedErrorHappened)
                _engagementState.onNext(State.NoEngagement)
            }
        }
    }

    private fun handleOmniCoreEngagement(engagement: OmnicoreEngagement) {
        Logger.i(TAG, "Omnicore Engagement started")

        when {
            currentEngagement != null -> {
                unsubscribeFromEvents(currentEngagement!!)
                currentEngagement = null
                resetState()
            }

            engagement.state.isLiveEngagementTransferredToSecureConversation -> _engagementState.onNext(State.TransferredToSecureConversation)

            else -> _engagementState.onNext(State.StartedOmniCore)
        }

        currentEngagement = engagement
        handleEngagementState(engagement.state)

        subscribeToEngagementEvents(engagement)
        subscribeToEngagementMediaEvents(engagement.media)
        subscribeToEngagementChatEvents(engagement.chat)
        subscribeToScreenSharingEvents(engagement.screenSharing)
        handleVisitorCamera(engagement.media)
    }

    private fun handleCallVisualizerEngagement(engagement: OmnibrowseEngagement) {
        Logger.i(TAG, "Call Visualizer Engagement started")

        currentEngagement?.also {
            unsubscribeFromEvents(it)
            currentEngagement = null
            resetState()
        } ?: _engagementState.onNext(State.StartedCallVisualizer)

        currentEngagement = engagement
        handleEngagementState(engagement.state)

        subscribeToEngagementEvents(engagement)
        subscribeToEngagementMediaEvents(engagement.media)
        subscribeToScreenSharingEvents(engagement.screenSharing)
        handleVisitorCamera(engagement.media)
        //No need for chat events here
    }

    private fun handleVisitorCamera(media: Media) {
        _visitorCameraState.onNext(
            media.currentCameraDevice?.let { VisitorCamera.Camera(it) } ?: VisitorCamera.NoCamera
        )
    }

    private fun subscribeToEngagementEvents(engagement: Engagement) {
        engagement.on(Engagement.Events.END, engagementEndCallback)
        engagement.on(Engagement.Events.STATE_UPDATE, engagementStateCallback)
    }

    private fun unsubscribeFromEngagementEvents(engagement: Engagement) {
        engagement.off(Engagement.Events.END, engagementEndCallback)
        engagement.off(Engagement.Events.STATE_UPDATE, engagementStateCallback)
    }

    private fun subscribeToEngagementMediaEvents(media: Media) {
        media.on(Media.Events.MEDIA_UPGRADE_OFFER, mediaUpgradeOfferCallback)
        media.on(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateUpdateCallback)
        media.on(Media.Events.VISITOR_STATE_UPDATE, visitorMediaStateUpdateCallback)
    }

    private fun unsubscribeFromEngagementMediaEvents(media: Media) {
        media.off(Media.Events.MEDIA_UPGRADE_OFFER, mediaUpgradeOfferCallback)
        media.off(Media.Events.OPERATOR_STATE_UPDATE, operatorMediaStateUpdateCallback)
        media.off(Media.Events.VISITOR_STATE_UPDATE, visitorMediaStateUpdateCallback)
    }

    private fun subscribeToEngagementChatEvents(chat: Chat) {
        chat.on(Chat.Events.OPERATOR_TYPING_STATUS, operatorTypingCallback)
    }

    private fun unsubscribeFromEngagementChatEvents(chat: Chat) {
        chat.off(Chat.Events.OPERATOR_TYPING_STATUS, operatorTypingCallback)
    }

    private fun subscribeToScreenSharingEvents(screenSharing: ScreenSharing) {
        screenSharing.on(ScreenSharing.Events.SCREEN_SHARING_REQUEST, screenSharingRequestCallback)
        screenSharing.on(ScreenSharing.Events.VISITOR_STATE, screenSharingStateCallback)
    }

    private fun unSubscribeFromScreenSharingEvents(screenSharing: ScreenSharing) {
        screenSharing.off(ScreenSharing.Events.SCREEN_SHARING_REQUEST, screenSharingRequestCallback)
        screenSharing.off(ScreenSharing.Events.VISITOR_STATE, screenSharingStateCallback)
    }

    private fun unsubscribeFromEvents(engagement: Engagement) {
        unsubscribeFromEngagementEvents(engagement)
        unsubscribeFromEngagementMediaEvents(engagement.media)
        unsubscribeFromEngagementChatEvents(engagement.chat)
        unSubscribeFromScreenSharingEvents(engagement.screenSharing)
    }

    private fun handleEngagementState(state: EngagementState) {
        // keeping the current operator value, to use inside this function,
        // because in some cases we need to globally have up to date operator before emitting new state
        val currentOperator: Operator? = currentOperatorValue

        operatorRepository.emit(state.operator)
        _currentOperator.onNext(Data.Value(state.operator))

        when {
            state.isLiveEngagementTransferredToSecureConversation -> {
                Logger.i(TAG, "Transfer to Secure Conversation")
                _engagementState.onNext(State.TransferredToSecureConversation)
            }

            state.visitorStatus == EngagementState.VisitorStatus.TRANSFERRING -> {
                Logger.i(TAG, "Transfer engagement")
                _engagementState.onNext(State.Update(state, EngagementUpdateState.Transferring))
            }

            currentOperator == null -> {
                Logger.i(TAG, "Operator connected")
                _engagementState.onNext(State.Update(state, EngagementUpdateState.OperatorConnected(state.operator)))
            }

            currentOperator.id != state.operator.id -> {
                Logger.i(TAG, "Operator changed")
                _engagementState.onNext(State.Update(state, EngagementUpdateState.OperatorChanged(state.operator)))
            }

            currentOperator != state.operator -> _engagementState.onNext(State.Update(state, EngagementUpdateState.Ongoing(state.operator)))
        }
    }

    private fun handleEngagementEnd() {
        Logger.i(TAG, "Engagement ended by Operator")
        currentEngagement?.also {
            currentEngagement = null
            unsubscribeFromEvents(it)
            notifyEngagementEnded(it)
            resetState()

            if (it is OmnicoreEngagement) {
                fetchSurvey(it, true)
            } else {
                _survey.onNext(SurveyState.Empty)
            }
        }
    }

    private fun notifyEngagementEnded(engagement: Engagement) {
        val state = if (engagement is OmnibrowseEngagement)
            State.FinishedCallVisualizer
        else
            State.FinishedOmniCore

        _engagementState.onNext(state)
    }

    //--Media
    private fun handleMediaUpgradeOffer(mediaUpgradeOffer: MediaUpgradeOffer) {
        Logger.d(TAG, "Media upgrade requested: $mediaUpgradeOffer")
        _mediaUpgradeOffer.onNext(mediaUpgradeOffer)
    }

    private fun handleOperatorMediaStateUpdate(operatorMediaState: OperatorMediaState) {
        _operatorMediaState.onNext(Data.Value(operatorMediaState))
    }

    private fun handleVisitorMediaStateUpdate(visitorMediaState: VisitorMediaState) {
        _visitorMediaState.onNext(Data.Value(visitorMediaState))
        subscribeToOnHoldChanges(visitorMediaState)
        currentEngagement?.also { handleVisitorCamera(it.media) }
    }

    private fun subscribeToOnHoldChanges(mediaState: MediaState) {
        mediaState.audio?.setOnHoldHandler(::onAudioHoldStateChanged)
        mediaState.video?.setOnHoldHandler(::onVideoHoldStateChanged)
    }

    private fun onAudioHoldStateChanged(onHold: Boolean) {
        updateOnHoldStateIfChanged(onHold)
    }

    private fun onVideoHoldStateChanged(onHold: Boolean) {
        updateOnHoldStateIfChanged(onHold)
    }

    private fun updateOnHoldStateIfChanged(onHold: Boolean) {
        if (_onHoldState.value == onHold) return
        _onHoldState.onNext(onHold)
    }

    override fun muteVisitorAudio() {
        val mediaState = visitorCurrentMediaState ?: return
        mediaState.audio?.mute() ?: return
        _visitorMediaState.onNext(Data.Value(mediaState))
    }

    override fun unMuteVisitorAudio() {
        val mediaState = visitorCurrentMediaState ?: return
        mediaState.audio?.unmute() ?: return
        _visitorMediaState.onNext(Data.Value(mediaState))
    }

    override fun pauseVisitorVideo() {
        val mediaState = visitorCurrentMediaState ?: return
        mediaState.video?.pause() ?: return
        _visitorMediaState.onNext(Data.Value(mediaState))
    }

    override fun resumeVisitorVideo() {
        val mediaState = visitorCurrentMediaState ?: return
        mediaState.video?.resume() ?: return
        _visitorMediaState.onNext(Data.Value(mediaState))
    }

    override fun setVisitorCamera(camera: CameraDevice) {
        _visitorCameraState.onNext(VisitorCamera.Switching)
        currentEngagement?.media?.setCameraDevice(camera)
    }

    //--Chat--
    private fun handleOperatorTypingStatus(operatorTypingStatus: OperatorTypingStatus) {
        _operatorTypingStatus.onNext(operatorTypingStatus.isTyping)
    }

    //--Screen Sharing--
    private fun handleScreenSharingRequest(request: ScreenSharingRequest) {
        Logger.d(TAG, "Received screen sharing request")
        currentScreenSharingRequest = request
        _screenSharingState.onNext(ScreenSharingState.Requested)
    }

    private fun handleScreenSharingState(state: VisitorScreenSharingState) {
        when (state.status ?: return) {
            ScreenSharing.Status.SHARING -> onScreenSharingStarted(state.localScreen ?: return)
            ScreenSharing.Status.NOT_SHARING -> onScreenSharingEnded()
        }
    }

    private fun handleScreenSharingRequestResponse(ex: GliaException?) {
        currentScreenSharingRequest = null
        if (ex == null) {
            _screenSharingState.onNext(ScreenSharingState.RequestAccepted)
            return
        }

        Logger.e(TAG, "Failed to accept screen sharing request", ex)
        _screenSharingState.onNext(ScreenSharingState.FailedToAcceptRequest(ex.debugMessage))
    }

    private fun onScreenSharingStarted(localScreen: LocalScreen) {
        if (_screenSharingState.value is ScreenSharingState.Started) return

        Logger.i(TAG, "Screen sharing started")
        _screenSharingState.onNext(ScreenSharingState.Started)
        currentScreenSharingScreen = localScreen
    }

    private fun onScreenSharingEnded() {
        Logger.i(TAG, "Screen sharing ended")
        markScreenSharingEnded()
    }

    private fun markScreenSharingEnded() {
        _screenSharingState.onNext(ScreenSharingState.Ended)
        currentScreenSharingScreen = null
    }

    override fun endScreenSharing() {
        Logger.i(TAG, "Screen sharing ended by visitor")
        currentScreenSharingScreen?.stopSharing()
        currentScreenSharingScreen = null
        _screenSharingState.onNext(ScreenSharingState.Ended)
    }

    override fun declineScreenSharingRequest() {
        Logger.i(TAG, "Screen sharing declined by visitor")
        // Pass RESULT_CANCELED to Core SDK to stop waiting for a permission result.
        // Otherwise, subsequent screen sharing requests won't be shown to the visitor.
        // Also see related bug ticket: MOB-2102
        onActivityResult(SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE, Activity.RESULT_CANCELED, null)
        currentScreenSharingRequest?.decline()
        currentScreenSharingRequest = null
        _screenSharingState.onNext(ScreenSharingState.RequestDeclined)
    }

    override fun acceptScreenSharingWithAskedPermission(activity: Activity, mode: ScreenSharing.Mode) {
        Logger.i(TAG, "Screen sharing accepted by visitor, permission asked")
        currentScreenSharingRequest?.accept(mode, activity, SKIP_ASKING_SCREEN_SHARING_PERMISSION_RESULT_CODE, screenSharingRequestResponseCallback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        currentEngagement?.onActivityResult(requestCode, resultCode, intent)
    }

    override fun onActivityResultSkipScreenSharingPermissionRequest(resultCode: Int, intent: Intent?) {
        mediaProjectionActivityResultProcessor.onNext(resultCode to intent)
    }

    override fun onReadyToShareScreen() {
        readyToShareScreenProcessor.onNext(Unit)
    }

    override fun updateIsSecureMessagingRequested(isSecureMessagingRequested: Boolean) {
        _isSecureMessagingRequested = isSecureMessagingRequested
    }
}
