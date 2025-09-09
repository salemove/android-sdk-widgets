package com.glia.widgets.engagement

import android.annotation.SuppressLint
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.glia.androidsdk.Engagement
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
import com.glia.widgets.di.GliaCore
import com.glia.widgets.helper.Data
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.isAudioOrVideo
import com.glia.widgets.helper.isCallVisualizer
import com.glia.widgets.helper.isQueueUnavailable
import com.glia.widgets.helper.isRetain
import com.glia.widgets.helper.isShowEndDialog
import com.glia.widgets.helper.isSurvey
import com.glia.widgets.internal.engagement.GliaOperatorRepository
import com.glia.widgets.internal.queue.QueueRepository
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

    private var _isSecureMessagingRequested: Boolean = false
    override val isSecureMessagingRequested: Boolean
        get() = _isSecureMessagingRequested

    override val isRetainAfterEnd: Boolean
        get() = currentEngagement?.state?.actionOnEnd.isRetain

    override val cameras: List<CameraDevice>?
        get() = currentEngagement?.media?.cameraDevices

    override fun initialize() {
        core.on(Glia.Events.ENGAGEMENT, omniCoreEngagementCallback)
        core.on(Glia.Events.QUEUE_TICKET, queueTicketCallback)
        core.callVisualizer.on(Omnibrowse.Events.ENGAGEMENT, callVisualizerEngagementCallback)
        core.callVisualizer.on(Omnibrowse.Events.ENGAGEMENT_REQUEST, engagementRequestCallback)
    }

    override fun reset() {
        _isSecureMessagingRequested = false

        if (currentState?.isQueueing == true) {
            cancelQueuing()
        } else {
            ensureNotScTransferredEngagement(::clearState)
        }
    }

    private fun resetState(retainSecureMessagingState: Boolean = false) {
        _isSecureMessagingRequested = retainSecureMessagingState
        _operatorMediaState.onNext(Data.Empty)
        _visitorMediaState.onNext(Data.Empty)
        _currentOperator.onNext(Data.Empty)
        _onHoldState.onNext(false)
        _operatorTypingStatus.onNext(false)
    }

    private fun ensureNotScTransferredEngagement(callback: Engagement.() -> Unit) = currentEngagement
        //This is required to not end the transferred engagement
        ?.takeUnless { isTransferredSecureConversation }
        ?.run(callback)

    private fun unsubscribeAndResetState(engagement: Engagement, retainSecureMessagingState: Boolean = false) {
        unsubscribeFromEvents(engagement)
        resetState(retainSecureMessagingState)
    }

    private fun clearState(engagement: Engagement) {
        currentEngagement = null

        unsubscribeAndResetState(engagement)

        val endAction = if (engagement.isCallVisualizer) {
            EndAction.ClearStateCallVisualizer
        } else {
            EndAction.ClearStateRegular
        }

        _engagementState.onNext(State.EngagementEnded(endAction = endAction))
    }

    override fun terminateEngagement() {
        ensureNotScTransferredEngagement {
            end { ex -> ex?.also { Logger.d(TAG, "Ending engagement failed") } }

            Logger.i(TAG, "Engagement ended locally, ended by:integrator")

            clearState(this)
        }
    }

    override fun endEngagement() {
        ensureNotScTransferredEngagement {
            end { ex -> ex?.also { Logger.d(TAG, "Ending engagement failed") } }

            Logger.i(TAG, "Engagement ended locally, ended by:visitor")
            currentEngagement = null

            unsubscribeAndResetState(this)

            //Since this function is called only when engagement is ended by visitor, it won't be a Call Visualizer
            //Sending [ClearStateRegular] action to close all the activities and release resources until the survey is loaded
            _engagementState.onNext(State.EngagementEnded(endAction = EndAction.ClearStateRegular))

            if (state.actionOnEnd.isSurvey) {
                getSurvey { survey, _ ->
                    if (survey != null) {
                        Logger.i(TAG, "Survey loaded")
                        _engagementState.onNext(State.EngagementEnded(endAction = EndAction.ShowSurvey(survey)))
                    }
                }
            }
        }
    }

    override fun queueForEngagement(mediaType: MediaType, replaceExisting: Boolean) {
        if (isQueueingOrLiveEngagement) return

        _engagementState.onNext(State.PreQueuing(mediaType))

        Logger.i(TAG, "Start queueing for media engagement")

        queueIngDisposable.add(
            queueRepository.relevantQueueIds.subscribe { ids ->
                if (ids.isNotEmpty()) {
                    core.queueForEngagement(
                        queueIds = ids,
                        mediaType = mediaType,
                        visitorContextAssetId = configurationManager.visitorContextAssetId,
                        engagementOptions = null,
                        mediaPermissionRequestCode = MEDIA_PERMISSION_REQUEST_CODE,
                        replaceExisting = replaceExisting
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


    @SuppressLint("CheckResult")
    override fun acceptCurrentEngagementRequest(visitorContextAssetId: String) {
        _engagementRequest.firstOrError().flatMapCompletable { accept(it, visitorContextAssetId) }.subscribe(
            {
                Logger.i(TAG, "Incoming Call Visualizer engagement was accepted")
            }
        ) {
            Logger.w(TAG, "Error during accepting engagement request, reason: ${it.message}")
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

    private fun handleIncomingEngagementRequest(engagementRequest: IncomingEngagementRequest) {
        engagementRequest.on(EngagementRequest.Events.OUTCOME, engagementOutcomeCallback)
        _engagementRequest.onNext(engagementRequest)
    }

    @SuppressLint("CheckResult")
    private fun handleEngagementOutcome(outcome: Outcome) {
        _engagementOutcome.onNext(outcome)
        _engagementRequest.firstOrError().subscribe { request ->
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

            else -> _engagementState.onNext(State.EngagementStarted(false))
        }

        currentEngagement = engagement
        handleEngagementState(engagement.state)

        subscribeToEngagementEvents(engagement)
        subscribeToEngagementMediaEvents(engagement.media)
        subscribeToEngagementChatEvents(engagement.chat)
        handleVisitorCamera(engagement.media)
    }

    private fun handleCallVisualizerEngagement(engagement: OmnibrowseEngagement) {
        Logger.i(TAG, "Call Visualizer Engagement started")

        currentEngagement?.also {
            unsubscribeFromEvents(it)
            currentEngagement = null
            resetState()
        } ?: _engagementState.onNext(State.EngagementStarted(true))

        currentEngagement = engagement
        handleEngagementState(engagement.state)

        subscribeToEngagementEvents(engagement)
        subscribeToEngagementMediaEvents(engagement.media)
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

    private fun unsubscribeFromEvents(engagement: Engagement) {
        unsubscribeFromEngagementEvents(engagement)
        unsubscribeFromEngagementMediaEvents(engagement.media)
        unsubscribeFromEngagementChatEvents(engagement.chat)
    }

    private fun handleEngagementState(state: EngagementState) {
        // keeping the current operator value, to use inside this function,
        // because in some cases we need to globally have up to date operator before emitting new state
        val currentOperator: Operator? = currentOperatorValue

        operatorRepository.emit(state.operator)
        //since there is no need to update operator data in case of transferred SC, we postpone it to do inside the when branches
        val updateCurrentOperator = { _currentOperator.onNext(Data.Value(state.operator)) }

        when {
            state.isLiveEngagementTransferredToSecureConversation -> {
                resetState() // reset state to avoid any side effects like operator typing during SC etc.
                Logger.i(TAG, "Transfer to Secure Conversation")
                _engagementState.onNext(State.TransferredToSecureConversation)
            }

            state.visitorStatus == EngagementState.VisitorStatus.TRANSFERRING -> {
                updateCurrentOperator()

                Logger.i(TAG, "Transfer engagement")
                _engagementState.onNext(State.Update(state, EngagementUpdateState.Transferring))
            }

            currentOperator == null -> {
                updateCurrentOperator()

                Logger.i(TAG, "Operator connected")
                _engagementState.onNext(State.Update(state, EngagementUpdateState.OperatorConnected(state.operator)))
            }

            currentOperator.id != state.operator.id -> {
                updateCurrentOperator()

                Logger.i(TAG, "Operator changed")
                _engagementState.onNext(State.Update(state, EngagementUpdateState.OperatorChanged(state.operator)))
            }

            currentOperator != state.operator -> {
                updateCurrentOperator()
                _engagementState.onNext(State.Update(state, EngagementUpdateState.Ongoing(state.operator)))
            }
        }
    }

    private fun handleEngagementEnd() {
        ensureNotScTransferredEngagement {
            currentEngagement = null
            Logger.i(TAG, "Engagement ended by Operator")
            unsubscribeFromEvents(this)
            resetState(state.actionOnEnd.isRetain)

            when {
                //We need just silently clear internal state when Call Visualizer engagement ends
                this.isCallVisualizer -> _engagementState.onNext(State.EngagementEnded(endAction = EndAction.ClearStateCallVisualizer))
                state.actionOnEnd.isRetain -> _engagementState.onNext(State.EngagementEnded(endAction = EndAction.Retain))
                state.actionOnEnd.isShowEndDialog -> _engagementState.onNext(State.EngagementEnded(endAction = EndAction.ShowEndDialog))
                state.actionOnEnd.isSurvey -> {
                    //Sending [ClearStateRegular] action to close all the activities and release resources until the survey is loaded
                    _engagementState.onNext(State.EngagementEnded(endAction = EndAction.ClearStateRegular))
                    getSurvey { survey, _ ->
                        if (survey != null) {
                            Logger.i(TAG, "Survey loaded")
                            _engagementState.onNext(State.EngagementEnded(endAction = EndAction.ShowSurvey(survey)))
                        } else {
                            _engagementState.onNext(State.EngagementEnded(endAction = EndAction.ShowEndDialog))
                        }
                    }
                }

                else -> _engagementState.onNext(State.EngagementEnded(endAction = EndAction.ShowEndDialog))

            }
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        currentEngagement?.onActivityResult(requestCode, resultCode, intent)
    }

    override fun updateIsSecureMessagingRequested(isSecureMessagingRequested: Boolean) {
        _isSecureMessagingRequested = isSecureMessagingRequested
    }
}
