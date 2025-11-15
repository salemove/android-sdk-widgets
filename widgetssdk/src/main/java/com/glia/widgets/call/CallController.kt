package com.glia.widgets.call

import android.text.format.DateUtils
import com.glia.androidsdk.Operator
import com.glia.androidsdk.comms.Audio
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.comms.Video
import com.glia.telemetry_lib.ButtonNames
import com.glia.telemetry_lib.GliaLogger
import com.glia.widgets.Constants
import com.glia.widgets.call.CallStatus.EngagementOngoingAudioCallStarted
import com.glia.widgets.call.CallStatus.EngagementOngoingVideoCallStarted
import com.glia.widgets.call.domain.HandleCallPermissionsUseCase
import com.glia.widgets.chat.domain.DecideOnQueueingUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.engagement.EngagementUpdateState
import com.glia.widgets.engagement.MediaType
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.domain.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.domain.EndEngagementUseCase
import com.glia.widgets.engagement.domain.EngagementStateUseCase
import com.glia.widgets.engagement.domain.EnqueueForEngagementUseCase
import com.glia.widgets.engagement.domain.FlipCameraButtonStateUseCase
import com.glia.widgets.engagement.domain.FlipVisitorCameraUseCase
import com.glia.widgets.engagement.domain.IsCurrentEngagementCallVisualizerUseCase
import com.glia.widgets.engagement.domain.IsQueueingOrLiveEngagementUseCase
import com.glia.widgets.engagement.domain.OperatorMediaUseCase
import com.glia.widgets.engagement.domain.ToggleVisitorAudioMediaStateUseCase
import com.glia.widgets.engagement.domain.ToggleVisitorVideoMediaStateUseCase
import com.glia.widgets.engagement.domain.VisitorMediaUseCase
import com.glia.widgets.helper.DeviceMonitor
import com.glia.widgets.helper.Logger
import com.glia.widgets.helper.NetworkState
import com.glia.widgets.helper.TAG
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.helper.TimeCounter.FormattedTimerStatusListener
import com.glia.widgets.helper.TimeCounter.RawTimerStatusListener
import com.glia.widgets.helper.formattedName
import com.glia.widgets.helper.imageUrl
import com.glia.widgets.helper.logCallScreenButtonClicked
import com.glia.widgets.internal.audio.domain.TurnSpeakerphoneUseCase
import com.glia.widgets.internal.dialog.DialogContract
import com.glia.widgets.internal.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.internal.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.internal.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.internal.dialog.model.Link
import com.glia.widgets.internal.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.internal.engagement.domain.ShouldShowMediaEngagementViewUseCase
import com.glia.widgets.internal.notification.domain.CallNotificationUseCase
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.MessagesNotSeenHandler.MessagesNotSeenHandlerListener
import com.glia.widgets.view.MinimizeHandler
import com.glia.widgets.view.floatingvisitorvideoview.FloatingVisitorVideoContract
import com.glia.widgets.webbrowser.domain.GetUrlFromLinkUseCase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.Optional
import java.util.concurrent.TimeUnit

private const val MAX_IDLE_TIME = 3200
private const val INACTIVITY_TIMER_TICKER_VALUE = 400
private const val INACTIVITY_TIMER_DELAY_VALUE = 0

internal class CallController(
    private val callTimer: TimeCounter,
    private val inactivityTimeCounter: TimeCounter,
    private val connectingTimerCounter: TimeCounter,
    private val minimizeHandler: MinimizeHandler,
    private val dialogController: DialogContract.Controller,
    private val messagesNotSeenHandler: MessagesNotSeenHandler,
    private val callNotificationUseCase: CallNotificationUseCase,
    private val endEngagementUseCase: EndEngagementUseCase,
    private val shouldShowMediaEngagementViewUseCase: ShouldShowMediaEngagementViewUseCase,
    private val isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    isCurrentEngagementCallVisualizerUseCase: IsCurrentEngagementCallVisualizerUseCase,
    private val turnSpeakerphoneUseCase: TurnSpeakerphoneUseCase,
    private val confirmationDialogUseCase: ConfirmationDialogUseCase,
    private val confirmationDialogLinksUseCase: ConfirmationDialogLinksUseCase,
    private val handleCallPermissionsUseCase: HandleCallPermissionsUseCase,
    private val engagementStateUseCase: EngagementStateUseCase,
    private val operatorMediaUseCase: OperatorMediaUseCase,
    private val acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase,
    private val visitorMediaUseCase: VisitorMediaUseCase,
    private val toggleVisitorAudioMediaStateUseCase: ToggleVisitorAudioMediaStateUseCase,
    private val toggleVisitorVideoMediaStateUseCase: ToggleVisitorVideoMediaStateUseCase,
    private val flipVisitorCameraUseCase: FlipVisitorCameraUseCase,
    private val flipCameraButtonStateUseCase: FlipCameraButtonStateUseCase,
    private val isQueueingOrLiveEngagementUseCase: IsQueueingOrLiveEngagementUseCase,
    private val enqueueForEngagementUseCase: EnqueueForEngagementUseCase,
    private val decideOnQueueingUseCase: DecideOnQueueingUseCase,
    private val getUrlFromLinkUseCase: GetUrlFromLinkUseCase,
    private val deviceMonitor: DeviceMonitor
) : CallContract.Controller {
    private val disposable = CompositeDisposable()
    private val mediaUpgradeDisposable = CompositeDisposable()
    private val connectionDisposable = CompositeDisposable()
    private var callTimerStatusListener: FormattedTimerStatusListener? = null
    private var inactivityTimerStatusListener: RawTimerStatusListener? = null
    private var connectingTimerStatusListener: RawTimerStatusListener? = null

    @Volatile
    private var callState: CallState = CallState.initial(isCurrentEngagementCallVisualizerUseCase())
    private var messagesNotSeenHandlerListener: MessagesNotSeenHandlerListener? = null

    private var view: CallContract.View? = null

    init {
        Logger.d(TAG, "constructor")

        if (isCurrentEngagementCallVisualizerUseCase()) {
            shouldShowMediaEngagementView(true)
        }

        subscribeToEngagement()
        disposable.add(decideOnQueueingUseCase().subscribe { enqueueForEngagement() })
    }

    private fun subscribeToEngagement() {
        engagementStateUseCase().subscribe(::onEngagementStateChanged).also(disposable::add)
        subscribeToMediaState()
        visitorMediaUseCase.onHoldState.subscribe(::onHoldChanged).also(disposable::add)
        flipCameraButtonStateUseCase().subscribe(::onNewFlipCameraButtonState).also(disposable::add)
    }

    // This method combines both visitor and operator media state updates so that they would be fired one after another.
    // This is done to prevent errors such as "does not support one-way audio calls" only because the
    // other participant media state is passed few milliseconds late.
    private fun subscribeToMediaState() {
        val initialState = object : MediaState {
            override fun getVideo(): Video? = null
            override fun getAudio(): Audio? = null
        }

        Flowable.combineLatest(
            visitorMediaUseCase().startWithItem(initialState).map { Optional.of(it) },
            operatorMediaUseCase().startWithItem(initialState).map { Optional.of(it) }
        ) { visitorState, operatorState ->
            Pair(visitorState, operatorState)
        }.debounce(200, TimeUnit.MILLISECONDS)
            .subscribe {
                val visitorMedia = it.first.orElse(null)
                val operatorMedia = it.second.orElse(null)
                onNewVisitorMediaState(visitorMedia)
                onNewOperatorMediaState(operatorMedia)
                callNotificationUseCase(visitorMedia, operatorMedia)
            }.also(disposable::add)
    }

    private fun onNewVisitorMediaState(visitorMediaState: MediaState?) {
        emitViewState(callState.visitorMediaStateChanged(visitorMediaState))
    }

    private fun onNewFlipCameraButtonState(flipButtonState: FloatingVisitorVideoContract.FlipButtonState) {
        emitViewState(callState.flipButtonStateChanged(flipButtonState))
    }

    private fun onHoldChanged(isOnHold: Boolean) {
        emitViewState(callState.setOnHold(isOnHold))
    }

    private fun newEngagementLoaded() {
        if (!connectingTimerCounter.isRunning) {
            connectingTimerCounter.startNew(Constants.CALL_TIMER_DELAY, Constants.CALL_TIMER_INTERVAL_VALUE)
        }
        emitViewState(callState.engagementStarted())
    }

    override fun startCall(mediaType: MediaType?, upgradeToCall: Boolean) {
        if (upgradeToCall || mediaType == null) {
            initCall(mediaType)
            return
        }
        handleCallPermissionsUseCase(mediaType) { isPermissionsGranted: Boolean ->
            if (isPermissionsGranted) {
                initCall(mediaType)
            } else {
                view?.showMissingPermissionsDialog()
            }
        }
    }

    private fun initCall(mediaType: MediaType?) {
        if (isShowOverlayPermissionRequestDialogUseCase()) {
            dialogController.showOverlayPermissionsDialog()
        } else {
            decideOnQueueingUseCase.markOverlayStepCompleted()
        }
        messagesNotSeenHandler.onNavigatedToCall()
        if (callState.integratorCallStarted || dialogController.isShowingUnexpectedErrorDialog) {
            return
        }
        emitViewState(callState.initCall(mediaType))
        createNewTimerStatusCallback()
        initMessagesNotSeenCallback()
        tryToQueueForEngagement()
        val newInactiveTimerListener = createInactivityTimerStatusListener()
        inactivityTimeCounter.addRawValueListener(newInactiveTimerListener)
        inactivityTimerStatusListener = newInactiveTimerListener
        val newConnectingTimerListener = createConnectingTimerStatusListener()
        connectingTimerCounter.addRawValueListener(newConnectingTimerListener)
        connectingTimerStatusListener = newConnectingTimerListener
        minimizeHandler.addListener { minimizeView() }
        messagesNotSeenHandler.addListener(messagesNotSeenHandlerListener)
    }

    private fun tryToQueueForEngagement() {
        if (!isQueueingOrLiveEngagementUseCase()) {
            confirmationDialogUseCase { shouldShow: Boolean ->
                if (shouldShow) {
                    dialogController.showEngagementConfirmationDialog()
                } else {
                    decideOnQueueingUseCase.onQueueingRequested()
                }
            }
        }
    }

    override fun onLiveObservationDialogRequested() {
        if (isQueueingOrLiveEngagementUseCase()) return
        view?.showEngagementConfirmationDialog()
    }

    override val confirmationDialogLinks: ConfirmationDialogLinks
        get() = confirmationDialogLinksUseCase.invoke()

    override fun onLinkClicked(link: Link) {
        Logger.d(TAG, "onLinkClicked")
        getUrlFromLinkUseCase(link)?.let {
            view?.navigateToWebBrowserActivity(link.title, it)
        } ?: run {
            Logger.e(TAG, "The URL is missing after the confirmation dialog link is clicked")
        }
    }

    override fun onLiveObservationDialogAllowed() {
        Logger.d(TAG, "onLiveObservationDialogAllowed")
        dialogController.dismissCurrentDialog()
        decideOnQueueingUseCase.onQueueingRequested()
    }

    override fun onLiveObservationDialogRejected() {
        Logger.d(TAG, "onLiveObservationDialogRejected")
        stop()
        dialogController.dismissDialogs()
    }

    private fun enqueueForEngagement() {
        enqueueForEngagementUseCase(callState.requestedMediaType ?: return)
    }

    override fun onDestroy(retained: Boolean) {
        Logger.d(TAG, "onDestroy, retain: $retained")
        view?.also {
            Logger.d(TAG, "destroyingView")
            it.destroyView()
            view = null
        }

        if (!retained) {
            disposable.clear()
            callTimerStatusListener?.also {
                callTimer.removeFormattedValueListener(it)
                callTimerStatusListener = null
            }
            callTimer.clear()
            inactivityTimeCounter.clear()
            connectingTimerCounter.clear()
            inactivityTimerStatusListener = null
            minimizeHandler.clear()
            messagesNotSeenHandler.removeListener(messagesNotSeenHandlerListener)
            messagesNotSeenHandlerListener = null
            mediaUpgradeDisposable.clear()
            callState = CallState.initial(callState.isCallVisualizer)
        }
    }

    override fun onDestroy() {
        throw RuntimeException("no op")
    }

    override fun onPause() {
        mediaUpgradeDisposable.clear()
        connectionDisposable.clear()
    }

    override fun endEngagementClicked() {
        GliaLogger.logCallScreenButtonClicked(ButtonNames.END_ENGAGEMENT)
        showEndEngagementDialog()
    }

    override fun setView(view: CallContract.View) {
        Logger.d(TAG, "setViewCallback")
        this.view = view
        view.emitState(callState)
    }

    override fun endEngagementDialogYesClicked() {
        Logger.d(TAG, "endEngagementDialogYesClicked")
        stop()
        dialogController.dismissDialogs()
    }

    override fun endEngagementDialogDismissed() {
        Logger.d(TAG, "endEngagementDialogDismissed")
        dialogController.dismissCurrentDialog()
    }

    override fun noMoreOperatorsAvailableDismissed() {
        Logger.d(TAG, "noMoreOperatorsAvailableDismissed")
        stop()
        dialogController.dismissDialogs()
    }

    override fun unexpectedErrorDialogDismissed() {
        Logger.d(TAG, "unexpectedErrorDialogDismissed")
        stop()
        dialogController.dismissDialogs()
    }

    override fun overlayPermissionsDialogDismissed() {
        Logger.d(TAG, "overlayPermissionsDialogDismissed")
        decideOnQueueingUseCase.onOverlayDialogShown()
        dialogController.dismissCurrentDialog()
    }

    override fun exitQueueingClicked() {
        GliaLogger.logCallScreenButtonClicked(ButtonNames.CLOSE)
        dialogController.showExitQueueDialog()
    }

    override fun onResume() {
        Logger.d(TAG, "onResume\n")
        onResumeSetup()
    }

    private fun subscribeToConnectionStatus() {
        connectionDisposable.clear()
        deviceMonitor.networkState
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleNetworkStateChange)
            .also(connectionDisposable::add)
    }

    private fun onResumeSetup() {
        showLandscapeControls()
        subscribeToMediaUpgradeEvents()
        subscribeToConnectionStatus()
    }

    private fun subscribeToMediaUpgradeEvents() {
        mediaUpgradeDisposable.addAll(acceptMediaUpgradeOfferUseCase.result.subscribe(::handleMediaUpgradeAcceptResult))
    }

    private fun handleMediaUpgradeAcceptResult(it: MediaUpgradeOffer) {
        Logger.d(TAG, "upgradeOfferChoiceSubmitSuccess")
        val mediaType: MediaType = if (it.video != null && it.video != MediaDirection.NONE) {
            MediaType.VIDEO
        } else {
            MediaType.AUDIO
        }
        emitViewState(callState.changeRequestedMediaType(mediaType))
    }

    override fun chatButtonClicked() {
        GliaLogger.logCallScreenButtonClicked(ButtonNames.CHAT)
        updateFromCallScreenUseCase(true)
        view?.navigateToChat()
        onDestroy(true)
        messagesNotSeenHandler.callChatButtonClicked()
    }

    override fun onUserInteraction() {
        if (view == null) {
            return
        }
        showLandscapeControls()
    }

    override fun minimizeButtonClicked() {
        GliaLogger.logCallScreenButtonClicked(ButtonNames.MINIMIZE)
        minimizeHandler.minimize()
    }

    override fun muteButtonClicked() {
        toggleVisitorAudioMediaStateUseCase()
    }

    override fun videoButtonClicked() {
        toggleVisitorVideoMediaStateUseCase()
    }

    override fun flipVideoButtonClicked() {
        flipVisitorCameraUseCase()
    }

    private fun onNewOperatorMediaState(operatorMediaState: MediaState) {
        if (!isQueueingOrLiveEngagementUseCase.hasOngoingLiveEngagement) return
        Logger.d(TAG, "newOperatorMediaState: $operatorMediaState, timer task running: ${callTimer.isRunning}")
        if (operatorMediaState.video != null) {
            onOperatorMediaStateVideo(operatorMediaState)
        } else if (operatorMediaState.audio != null) {
            onOperatorMediaStateAudio(operatorMediaState)
        } else {
            onOperatorMediaStateUnknown()
        }
        if (callState.isMediaEngagementStarted && !callTimer.isRunning && callTimerStatusListener != null) {
            callTimer.startNew(Constants.CALL_TIMER_DELAY, Constants.CALL_TIMER_INTERVAL_VALUE)
        }
    }

    override fun onSpeakerButtonPressed() {
        val shouldOnSpeaker = !callState.isSpeakerOn

        emitViewState(callState.speakerValueChanged(shouldOnSpeaker))
        turnSpeakerphoneUseCase(on = shouldOnSpeaker)

        GliaLogger.logCallScreenButtonClicked(if (shouldOnSpeaker) ButtonNames.SPEAKER_ON else ButtonNames.SPEAKER_OFF)
    }

    override fun shouldShowMediaEngagementView(upgradeToCall: Boolean): Boolean {
        return shouldShowMediaEngagementViewUseCase.execute(upgradeToCall)
    }

    override fun onBackClicked() {
        GliaLogger.logCallScreenButtonClicked(ButtonNames.NAVIGATION_BACK)
        updateFromCallScreenUseCase(false)
        onDestroy(true)
    }

    @Synchronized
    private fun emitViewState(state: CallState) {
        if (setState(state) && view != null) {
            Logger.d(TAG, "Emit state:\n$state")
            view?.emitState(callState)
        }
    }

    @Synchronized
    private fun setState(state: CallState): Boolean {
        if (callState == state) return false
        callState = state
        return true
    }

    private fun createInactivityTimerStatusListener(): RawTimerStatusListener {
        return object : RawTimerStatusListener {
            override fun onNewRawTimerValue(timerValue: Int) {
                if (callState.isVideoCall) {
                    Logger.d(TAG, "inactivityTimer onNewTimerValue: $timerValue")
                    emitViewState(callState.landscapeControlsVisibleChanged(timerValue < MAX_IDLE_TIME))
                }
                if (timerValue >= MAX_IDLE_TIME) {
                    inactivityTimeCounter.stop()
                }
            }

            override fun onRawTimerCancelled() {}
        }
    }

    private fun createConnectingTimerStatusListener(): RawTimerStatusListener {
        return object : RawTimerStatusListener {
            override fun onNewRawTimerValue(timerValue: Int) {
                if (callState.isCallOngoingAndOperatorIsConnecting) {
                    emitViewState(callState.connectingTimerValueChanged(TimeUnit.MILLISECONDS.toSeconds(timerValue.toLong()).toString()))
                }
            }

            override fun onRawTimerCancelled() {}
        }
    }

    private fun initMessagesNotSeenCallback() {
        messagesNotSeenHandlerListener = MessagesNotSeenHandlerListener {
            emitViewState(callState.changeNumberOfMessages(it))
        }
    }

    private fun showEndEngagementDialog() {
        if (callState.isMediaEngagementStarted) {
            dialogController.showEndEngagementDialog()
        }
    }

    private fun createNewTimerStatusCallback() {
        if (callTimerStatusListener == null) {
            val newListener = object : FormattedTimerStatusListener {
                override fun onNewFormattedTimerValue(formattedValue: String) {
                    if (callState.showCallTimerView()) {
                        emitViewState(callState.newStartedCallTimerValue(formattedValue))
                    }
                }

                override fun onFormattedTimerCancelled() {
                    // Should only happen if engagement ends.
                }
            }
            callTimer.addFormattedValueListener(newListener)
            callTimerStatusListener = newListener
        }
    }

    private fun restartInactivityTimeCounter() {
        inactivityTimeCounter.startNew(INACTIVITY_TIMER_DELAY_VALUE, INACTIVITY_TIMER_TICKER_VALUE)
    }

    private fun stop() {
        Logger.d(TAG, "Stop, engagement ended")
        endEngagementUseCase()
        mediaUpgradeDisposable.clear()
        connectionDisposable.clear()
        emitViewState(callState.stop())
    }

    private fun onOperatorMediaStateVideo(operatorMediaState: MediaState) {
        Logger.d(TAG, "newOperatorMediaState: video")
        var formattedTime = DateUtils.formatElapsedTime(0)
        if (callState.isCallOngoingAndOperatorConnected) {
            formattedTime = callState.callStatus.time
        }
        emitViewState(callState.videoCallOperatorVideoStarted(operatorMediaState, formattedTime))
        connectingTimerCounter.stop()
    }

    private fun onOperatorMediaStateAudio(operatorMediaState: MediaState) {
        Logger.d(TAG, "newOperatorMediaState: audio")
        var formattedTime = DateUtils.formatElapsedTime(0)
        if (callState.isCallOngoingAndOperatorConnected) formattedTime = callState.callStatus.time
        emitViewState(callState.audioCallStarted(operatorMediaState, formattedTime))
        connectingTimerCounter.stop()
    }

    private fun onOperatorMediaStateUnknown() {
        Logger.d(TAG, "newOperatorMediaState: null")
        if (callState.isMediaEngagementStarted) {
            emitViewState(callState.backToOngoing())
        }
        callNotificationUseCase.removeAllNotifications()
        if (!connectingTimerCounter.isRunning) {
            connectingTimerCounter.startNew(Constants.CALL_TIMER_DELAY, Constants.CALL_TIMER_INTERVAL_VALUE)
        }
    }

    private fun onEngagementStateChanged(state: State) {
        when (state) {
            is State.EngagementStarted -> newEngagementLoaded()
            is State.Update -> handleEngagementStateUpdate(state.updateState)
            is State.QueueUnstaffed, is State.UnexpectedErrorHappened -> {
                emitViewState(callState.changeVisibility(false))
            }

            else -> {
                //no op
            }
        }
    }

    private fun handleEngagementStateUpdate(state: EngagementUpdateState) {
        when (state) {
            is EngagementUpdateState.Ongoing -> onEngagementOngoing(state.operator)
            is EngagementUpdateState.OperatorChanged -> onOperatorChanged(state.operator)
            is EngagementUpdateState.OperatorConnected -> onOperatorConnected(state.operator)
            EngagementUpdateState.Transferring -> onTransferring()
        }
    }

    private fun onEngagementOngoing(operator: Operator) {
        if (callState.callStatus !is EngagementOngoingAudioCallStarted && callState.callStatus !is EngagementOngoingVideoCallStarted) {
            onOperatorConnected(operator)
        }
    }

    private fun onOperatorConnected(operator: Operator) {
        val name = operator.formattedName
        val imageUrl = operator.imageUrl
        operatorConnected(name, imageUrl)
    }

    private fun onOperatorChanged(operator: Operator) {
        val name = operator.formattedName
        val imageUrl = operator.imageUrl
        operatorChanged(name, imageUrl)
    }

    private fun operatorChanged(operatorName: String, profileImgUrl: String?) {
        emitViewState(callState.operatorConnecting(operatorName, profileImgUrl))
    }

    private fun operatorConnected(operatorName: String, profileImgUrl: String?) {
        if (callState.isCallOngoingAndOperatorIsConnecting) {
            emitViewState(callState.operatorConnecting(operatorName, profileImgUrl))
        } else {
            if (callState.isAudioCall) {
                onOperatorConnectedAndAudioCallOngoing(operatorName, profileImgUrl)
            } else {
                onOperatorConnectedAndVideoCallOngoing(operatorName, profileImgUrl)
            }
        }
    }

    private fun onOperatorConnectedAndVideoCallOngoing(
        operatorName: String,
        profileImgUrl: String?
    ) {
        emitViewState(
            callState
                .operatorConnecting(operatorName, profileImgUrl)
                .videoCallOperatorVideoStarted(callState.callStatus.operatorMediaState, callState.callStatus.time)
        )
    }

    private fun onOperatorConnectedAndAudioCallOngoing(operatorName: String, profileImgUrl: String?) {
        emitViewState(
            callState.operatorConnecting(operatorName, profileImgUrl)
                .audioCallStarted(callState.callStatus.operatorMediaState, callState.callStatus.time)
        )
    }

    private fun onTransferring() {
        emitViewState(callState.setTransferring())
    }

    private fun showLandscapeControls() {
        emitViewState(callState.landscapeControlsVisibleChanged(true))
        restartInactivityTimeCounter()
    }

    private fun minimizeView() {
        view?.minimizeView()
        onDestroy(true)
    }

    private fun handleNetworkStateChange(networkState: NetworkState) {
        when (networkState) {
            NetworkState.CONNECTED -> view?.dismissConnectionSnackBar()
            NetworkState.DISCONNECTED -> view?.showConnectionSnackBar()
        }
    }
}
