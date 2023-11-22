package com.glia.widgets.call

import android.text.format.DateUtils
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.Operator
import com.glia.androidsdk.comms.MediaDirection
import com.glia.androidsdk.comms.MediaState
import com.glia.androidsdk.comms.MediaUpgradeOffer
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.widgets.Constants
import com.glia.widgets.call.CallStatus.EngagementOngoingAudioCallStarted
import com.glia.widgets.call.CallStatus.EngagementOngoingVideoCallStarted
import com.glia.widgets.call.domain.HandleCallPermissionsUseCase
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase
import com.glia.widgets.core.audio.domain.TurnSpeakerphoneUseCase
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.core.dialog.DialogController
import com.glia.widgets.core.dialog.domain.ConfirmationDialogLinksUseCase
import com.glia.widgets.core.dialog.domain.IsShowEnableCallNotificationChannelDialogUseCase
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase
import com.glia.widgets.core.dialog.model.ConfirmationDialogLinks
import com.glia.widgets.core.dialog.model.Link
import com.glia.widgets.core.engagement.domain.ConfirmationDialogUseCase
import com.glia.widgets.core.engagement.domain.ShouldShowMediaEngagementViewUseCase
import com.glia.widgets.core.notification.domain.CallNotificationUseCase
import com.glia.widgets.core.permissions.domain.HasCallNotificationChannelEnabledUseCase
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase
import com.glia.widgets.core.queue.domain.GliaQueueForMediaEngagementUseCase
import com.glia.widgets.core.queue.domain.QueueTicketStateChangeToUnstaffedUseCase
import com.glia.widgets.core.queue.domain.exception.QueueingOngoingException
import com.glia.widgets.engagement.AcceptMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.DeclineMediaUpgradeOfferUseCase
import com.glia.widgets.engagement.EndEngagementUseCase
import com.glia.widgets.engagement.EngagementStateUseCase
import com.glia.widgets.engagement.EngagementUpdateState
import com.glia.widgets.engagement.HasOngoingEngagementUseCase
import com.glia.widgets.engagement.IsCurrentEngagementCallVisualizer
import com.glia.widgets.engagement.MediaUpgradeOfferUseCase
import com.glia.widgets.engagement.OperatorMediaUseCase
import com.glia.widgets.engagement.State
import com.glia.widgets.engagement.State.StartedCallVisualizer
import com.glia.widgets.engagement.State.StartedOmniCore
import com.glia.widgets.engagement.ToggleVisitorAudioMediaStateUseCase
import com.glia.widgets.engagement.ToggleVisitorVideoMediaStateUseCase
import com.glia.widgets.engagement.VisitorMediaUseCase
import com.glia.widgets.helper.Logger.d
import com.glia.widgets.helper.Logger.e
import com.glia.widgets.helper.Logger.i
import com.glia.widgets.helper.TimeCounter
import com.glia.widgets.helper.TimeCounter.FormattedTimerStatusListener
import com.glia.widgets.helper.TimeCounter.RawTimerStatusListener
import com.glia.widgets.helper.imageUrl
import com.glia.widgets.helper.isQueueUnavailable
import com.glia.widgets.helper.unSafeSubscribe
import com.glia.widgets.view.MessagesNotSeenHandler
import com.glia.widgets.view.MessagesNotSeenHandler.MessagesNotSeenHandlerListener
import com.glia.widgets.view.MinimizeHandler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

private const val MAX_IDLE_TIME = 3200
private const val INACTIVITY_TIMER_TICKER_VALUE = 400
private const val INACTIVITY_TIMER_DELAY_VALUE = 0
private const val TAG = "CallController"

internal class CallController(
    private val sdkConfigurationManager: GliaSdkConfigurationManager,
    private val callTimer: TimeCounter,
    private var viewCallback: CallViewCallback?,
    private val inactivityTimeCounter: TimeCounter,
    private val connectingTimerCounter: TimeCounter,
    private val minimizeHandler: MinimizeHandler,
    private val dialogController: DialogController,
    private val messagesNotSeenHandler: MessagesNotSeenHandler,
    private val callNotificationUseCase: CallNotificationUseCase,
    private val gliaQueueForMediaEngagementUseCase: GliaQueueForMediaEngagementUseCase,
    private val cancelQueueTicketUseCase: GliaCancelQueueTicketUseCase,
    private val endEngagementUseCase: EndEngagementUseCase,
    private val shouldShowMediaEngagementViewUseCase: ShouldShowMediaEngagementViewUseCase,
    private val isShowOverlayPermissionRequestDialogUseCase: IsShowOverlayPermissionRequestDialogUseCase,
    private val hasCallNotificationChannelEnabledUseCase: HasCallNotificationChannelEnabledUseCase,
    private val isShowEnableCallNotificationChannelDialogUseCase: IsShowEnableCallNotificationChannelDialogUseCase,
    private val updateFromCallScreenUseCase: UpdateFromCallScreenUseCase,
    private val ticketStateChangeToUnstaffedUseCase: QueueTicketStateChangeToUnstaffedUseCase,
    private val isCurrentEngagementCallVisualizer: IsCurrentEngagementCallVisualizer,
    private val hasOngoingEngagementUseCase: HasOngoingEngagementUseCase,
    private val turnSpeakerphoneUseCase: TurnSpeakerphoneUseCase,
    private val confirmationDialogUseCase: ConfirmationDialogUseCase,
    private val confirmationDialogLinksUseCase: ConfirmationDialogLinksUseCase,
    private val handleCallPermissionsUseCase: HandleCallPermissionsUseCase,
    private val engagementStateUseCase: EngagementStateUseCase,
    private val operatorMediaUseCase: OperatorMediaUseCase,
    private val mediaUpgradeOfferUseCase: MediaUpgradeOfferUseCase,
    private val acceptMediaUpgradeOfferUseCase: AcceptMediaUpgradeOfferUseCase,
    private val declineMediaUpgradeOfferUseCase: DeclineMediaUpgradeOfferUseCase,
    private val visitorMediaUseCase: VisitorMediaUseCase,
    private val toggleVisitorAudioMediaStateUseCase: ToggleVisitorAudioMediaStateUseCase,
    private val toggleVisitorVideoMediaStateUseCase: ToggleVisitorVideoMediaStateUseCase
) {
    private val disposable = CompositeDisposable()
    private val mediaUpgradeDisposable = CompositeDisposable()
    private var callTimerStatusListener: FormattedTimerStatusListener? = null
    private var inactivityTimerStatusListener: RawTimerStatusListener? = null
    private var connectingTimerStatusListener: RawTimerStatusListener? = null

    @Volatile
    private var callState: CallState = CallState.initial(isCurrentEngagementCallVisualizer())
    private var messagesNotSeenHandlerListener: MessagesNotSeenHandlerListener? = null

    init {
        d(TAG, "constructor")

        if (isCurrentEngagementCallVisualizer()) {
            shouldShowMediaEngagementView(true)
        }

        subscribeToEngagement()
    }

    private fun subscribeToEngagement() {
        engagementStateUseCase().unSafeSubscribe(::onEngagementStateChanged)
        operatorMediaUseCase().unSafeSubscribe(::onNewOperatorMediaState)
        visitorMediaUseCase().unSafeSubscribe(::onNewVisitorMediaState)
        visitorMediaUseCase.onHoldState.unSafeSubscribe(::onHoldChanged)
    }

    private fun onNewVisitorMediaState(visitorMediaState: MediaState?) {
        emitViewState(callState.visitorMediaStateChanged(visitorMediaState))
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

    fun startCall(
        companyName: String,
        queueId: String?,
        visitorContextAssetId: String?,
        mediaType: Engagement.MediaType,
        useOverlays: Boolean,
        screenSharingMode: ScreenSharing.Mode,
        isUpgradeToCall: Boolean
    ) {
        if (isUpgradeToCall) {
            initCall(companyName, queueId, visitorContextAssetId, mediaType, useOverlays, screenSharingMode)
            return
        }
        handleCallPermissionsUseCase.invoke(mediaType) { isPermissionsGranted: Boolean ->
            if (isPermissionsGranted) {
                initCall(companyName, queueId, visitorContextAssetId, mediaType, useOverlays, screenSharingMode)
            } else {
                if (viewCallback != null) {
                    viewCallback!!.showMissingPermissionsDialog()
                }
            }
        }
    }

    private fun initCall(
        companyName: String,
        queueId: String?,
        visitorContextAssetId: String?,
        mediaType: Engagement.MediaType,
        useOverlays: Boolean,
        screenSharingMode: ScreenSharing.Mode
    ) {
        sdkConfigurationManager.isUseOverlay = useOverlays
        sdkConfigurationManager.screenSharingMode = screenSharingMode
        if (isShowOverlayPermissionRequestDialogUseCase.execute()) {
            dialogController.showOverlayPermissionsDialog()
        }
        messagesNotSeenHandler.onNavigatedToCall()
        if (callState.integratorCallStarted || dialogController.isShowingChatEnderDialog) {
            return
        }
        emitViewState(callState.initCall(companyName, queueId, visitorContextAssetId, mediaType))
        createNewTimerStatusCallback()
        initControllerCallbacks()
        initMessagesNotSeenCallback()
        tryToQueueForEngagement(queueId, visitorContextAssetId, mediaType)
        inactivityTimeCounter.addRawValueListener(inactivityTimerStatusListener)
        connectingTimerCounter.addRawValueListener(connectingTimerStatusListener)
        minimizeHandler.addListener { minimizeView() }
        messagesNotSeenHandler.addListener(messagesNotSeenHandlerListener)
    }

    private fun tryToQueueForEngagement(queueId: String?, visitorContextAssetId: String?, mediaType: Engagement.MediaType) {
        if (!hasOngoingEngagementUseCase.invoke()) {
            confirmationDialogUseCase { shouldShow: Boolean ->
                if (shouldShow) {
                    dialogController.showEngagementConfirmationDialog()
                } else {
                    queueForEngagement(queueId, visitorContextAssetId, mediaType)
                }
            }
        }
    }

    fun onLiveObservationDialogRequested() {
        if (hasOngoingEngagementUseCase.invoke()) return
        viewCallback!!.showEngagementConfirmationDialog()
    }

    val confirmationDialogLinks: ConfirmationDialogLinks
        get() = confirmationDialogLinksUseCase.invoke()

    fun onLinkClicked(link: Link) {
        d(TAG, "onLinkClicked")
        viewCallback?.navigateToWebBrowserActivity(link.title, link.url)
    }

    fun onLiveObservationDialogAllowed() {
        d(TAG, "onLiveObservationDialogAllowed")
        dialogController.dismissCurrentDialog()
        queueForEngagement(callState.queueId, callState.visitorContextAssetId, callState.requestedMediaType)
    }

    fun onLiveObservationDialogRejected() {
        d(TAG, "onLiveObservationDialogRejected")
        stop()
        dialogController.dismissDialogs()
    }

    private fun queueForEngagement(queueId: String?, visitorContextAssetId: String?, mediaType: Engagement.MediaType) {
        disposable.add(
            gliaQueueForMediaEngagementUseCase
                .execute(queueId, visitorContextAssetId, mediaType)
                .subscribe({ queueForEngagementStarted() }) { queueForEngagementError(it) }
        )
    }

    fun onDestroy(retain: Boolean) {
        d(TAG, "onDestroy, retain: $retain")
        viewCallback?.also {
            d(TAG, "destroyingView")
            it.destroyView()
            viewCallback = null
        }

        if (!retain) {
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
        }
    }

    fun onPause() {
        mediaUpgradeDisposable.clear()
    }

    fun leaveChatClicked() {
        d(TAG, "leaveChatClicked")
        showExitChatDialog()
    }

    fun setViewCallback(callViewCallback: CallViewCallback?) {
        d(TAG, "setViewCallback")
        viewCallback = callViewCallback
        viewCallback?.emitState(callState)
    }

    fun endEngagementDialogYesClicked() {
        d(TAG, "endEngagementDialogYesClicked")
        stop()
        dialogController.dismissDialogs()
    }

    fun endEngagementDialogDismissed() {
        d(TAG, "endEngagementDialogDismissed")
        dialogController.dismissCurrentDialog()
    }

    fun noMoreOperatorsAvailableDismissed() {
        d(TAG, "noMoreOperatorsAvailableDismissed")
        stop()
        dialogController.dismissDialogs()
    }

    fun unexpectedErrorDialogDismissed() {
        d(TAG, "unexpectedErrorDialogDismissed")
        stop()
        dialogController.dismissDialogs()
    }

    fun overlayPermissionsDialogDismissed() {
        d(TAG, "overlayPermissionsDialogDismissed")
        dialogController.dismissCurrentDialog()
    }

    fun leaveChatQueueClicked() {
        d(TAG, "leaveChatQueueClicked")
        dialogController.showExitQueueDialog()
    }

    fun onResume() {
        d(TAG, "onResume\n")
        onResumeSetup()
    }

    private fun onResumeSetup() {
        showCallNotification()
        showLandscapeControls()
        subscribeToMediaUpgradeEvents()
    }

    private fun subscribeToMediaUpgradeEvents() {
        mediaUpgradeDisposable.addAll(
            mediaUpgradeOfferUseCase().subscribe(::handleMediaUpgradeRequest),
            acceptMediaUpgradeOfferUseCase.result.subscribe(::handleMediaUpgradeAcceptResult)
        )
    }

    private fun handleMediaUpgradeAcceptResult(it: MediaUpgradeOffer) {
        d(TAG, "upgradeOfferChoiceSubmitSuccess")
        val mediaType: Engagement.MediaType = if (it.video != null && it.video != MediaDirection.NONE) {
            Engagement.MediaType.VIDEO
        } else {
            Engagement.MediaType.AUDIO
        }
        emitViewState(callState.changeRequestedMediaType(mediaType))
    }

    private fun handleMediaUpgradeRequest(it: MediaUpgradeOffer) {
        when {
            // audio call
            it.video == MediaDirection.NONE && it.audio == MediaDirection.TWO_WAY -> {
                d(TAG, "Audio upgrade requested")
                showUpgradeAudioDialog(it)
            }
            // 2 way video call
            it.video == MediaDirection.TWO_WAY -> {
                d(TAG, "2 way video upgrade requested")
                showUpgradeVideoDialog2Way(it)
            }
            // 1 way video call
            it.video == MediaDirection.ONE_WAY -> {
                d(TAG, "1 way video upgrade requested")
                showUpgradeVideoDialog1Way(it)
            }
        }
    }

    fun chatButtonClicked() {
        d(TAG, "chatButtonClicked")
        updateFromCallScreenUseCase.updateFromCallScreen(true)
        viewCallback?.navigateToChat()
        onDestroy(true)
        messagesNotSeenHandler.callChatButtonClicked()
    }

    fun acceptUpgradeOfferClicked(mediaUpgradeOffer: MediaUpgradeOffer) {
        i(TAG, "Upgrade offer accepted by visitor")
        acceptMediaUpgradeOfferUseCase(mediaUpgradeOffer)
        dialogController.dismissCurrentDialog()
    }

    fun declineUpgradeOfferClicked(mediaUpgradeOffer: MediaUpgradeOffer) {
        i(TAG, "Upgrade offer declined by visitor")
        declineMediaUpgradeOfferUseCase(mediaUpgradeOffer)
        dialogController.dismissCurrentDialog()
    }

    fun onUserInteraction() {
        if (viewCallback == null) {
            return
        }
        showLandscapeControls()
    }

    fun minimizeButtonClicked() {
        d(TAG, "minimizeButtonClicked")
        minimizeHandler.minimize()
    }

    fun muteButtonClicked() {
        toggleVisitorAudioMediaStateUseCase()
    }

    fun videoButtonClicked() {
        toggleVisitorVideoMediaStateUseCase()
    }

    fun notificationsDialogDismissed() {
        dialogController.dismissCurrentDialog()
    }

    private fun onNewOperatorMediaState(operatorMediaState: MediaState) {
        d(TAG, "newOperatorMediaState: $operatorMediaState, timertaskrunning: ${callTimer.isRunning}")
        if (operatorMediaState.video != null) {
            if (isShowEnableCallNotificationChannelDialogUseCase.execute()) {
                dialogController.showEnableCallNotificationChannelDialog()
            }
            onOperatorMediaStateVideo(operatorMediaState)
        } else if (operatorMediaState.audio != null) {
            if (isShowEnableCallNotificationChannelDialogUseCase.execute()) {
                dialogController.showEnableCallNotificationChannelDialog()
            }
            onOperatorMediaStateAudio(operatorMediaState)
        } else {
            onOperatorMediaStateUnknown()
        }
        if (callState.isMediaEngagementStarted && !callTimer.isRunning && callTimerStatusListener != null) {
            callTimer.startNew(Constants.CALL_TIMER_DELAY, Constants.CALL_TIMER_INTERVAL_VALUE)
        }
    }

    fun onSpeakerButtonPressed() {
        val newValue = !callState.isSpeakerOn
        d(TAG, "onSpeakerButtonPressed, new value: $newValue")
        emitViewState(callState.speakerValueChanged(newValue))
        turnSpeakerphoneUseCase.invoke(newValue)
    }

    private fun queueForEngagementStarted() {
        observeQueueTicketState()
    }

    private fun queueForEngagementStopped() {
        i(TAG, "Queue for engagement stopped due to error or empty queue")
    }

    private fun queueForEngagementError(exception: Throwable?) {
        if (exception == null) return

        e(TAG, exception.toString())

        when (exception) {
            is GliaException -> {
                if (exception.isQueueUnavailable) {
                    dialogController.showNoMoreOperatorsAvailableDialog()
                } else {
                    dialogController.showUnexpectedErrorDialog()
                }
                emitViewState(callState.changeVisibility(false))
            }

            is QueueingOngoingException -> queueForEngagementStarted()
        }

    }

    fun shouldShowMediaEngagementView(isUpgradeToCall: Boolean): Boolean {
        return shouldShowMediaEngagementViewUseCase.execute(isUpgradeToCall)
    }

    fun onBackClicked() {
        updateFromCallScreenUseCase.updateFromCallScreen(false)
    }

    @Synchronized
    private fun emitViewState(state: CallState) {
        if (setState(state) && viewCallback != null) {
            d(TAG, "Emit state:\n$state")
            viewCallback!!.emitState(callState)
        }
    }

    @Synchronized
    private fun setState(state: CallState): Boolean {
        if (callState == state) return false
        callState = state
        return true
    }

    private fun initControllerCallbacks() {
        inactivityTimerStatusListener = object : RawTimerStatusListener {
            override fun onNewRawTimerValue(timerValue: Int) {
                if (callState.isVideoCall) {
                    d(TAG, "inactivityTimer onNewTimerValue: $timerValue")
                    emitViewState(callState.landscapeControlsVisibleChanged(timerValue < MAX_IDLE_TIME))
                }
                if (timerValue >= MAX_IDLE_TIME) {
                    inactivityTimeCounter.stop()
                }
            }

            override fun onRawTimerCancelled() {}
        }
        connectingTimerStatusListener = object : RawTimerStatusListener {
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

    private fun showUpgradeAudioDialog(mediaUpgradeOffer: MediaUpgradeOffer) {
        if (callState.isMediaEngagementStarted) {
            dialogController.showUpgradeAudioDialog(mediaUpgradeOffer, callState.callStatus.formattedOperatorName)
        }
    }

    private fun showUpgradeVideoDialog2Way(mediaUpgradeOffer: MediaUpgradeOffer) {
        if (callState.isMediaEngagementStarted) dialogController.showUpgradeVideoDialog2Way(
            mediaUpgradeOffer,
            callState.callStatus.formattedOperatorName
        )
    }

    private fun showUpgradeVideoDialog1Way(mediaUpgradeOffer: MediaUpgradeOffer) {
        if (callState.isMediaEngagementStarted) dialogController.showUpgradeVideoDialog1Way(
            mediaUpgradeOffer,
            callState.callStatus.formattedOperatorName
        )
    }

    private fun showExitChatDialog() {
        if (callState.isMediaEngagementStarted) {
            dialogController.showExitChatDialog(callState.callStatus.formattedOperatorName)
        }
    }

    private fun createNewTimerStatusCallback() {
        if (callTimerStatusListener == null) {
            callTimerStatusListener = object : FormattedTimerStatusListener {
                override fun onNewFormattedTimerValue(formattedValue: String) {
                    if (callState.showCallTimerView()) {
                        emitViewState(callState.newStartedCallTimerValue(formattedValue))
                    }
                }

                override fun onFormattedTimerCancelled() {
                    // Should only happen if engagement ends.
                }
            }
            callTimer.addFormattedValueListener(callTimerStatusListener)
        }
    }

    private fun restartInactivityTimeCounter() {
        inactivityTimeCounter.startNew(INACTIVITY_TIMER_DELAY_VALUE, INACTIVITY_TIMER_TICKER_VALUE)
    }

    private fun stop() {
        d(TAG, "Stop, engagement ended")
        disposable.add(
            cancelQueueTicketUseCase.execute()
                .subscribe({ queueForEngagementStopped() }) { it?.apply { e(TAG, "cancelQueueTicketUseCase error: $message") } }
        )
        endEngagementUseCase.invoke(false)
        mediaUpgradeDisposable.clear()
        emitViewState(callState.stop())
    }

    private fun onOperatorMediaStateVideo(operatorMediaState: MediaState) {
        d(TAG, "newOperatorMediaState: video")
        var formattedTime = DateUtils.formatElapsedTime(0)
        if (callState.isCallOngoingAndOperatorConnected) {
            formattedTime = callState.callStatus.time
        }
        emitViewState(callState.videoCallOperatorVideoStarted(operatorMediaState, formattedTime))
        callNotificationUseCase.invoke(callState.callStatus.visitorMediaState, operatorMediaState)
        connectingTimerCounter.stop()
    }

    private fun onOperatorMediaStateAudio(operatorMediaState: MediaState) {
        d(TAG, "newOperatorMediaState: audio")
        var formattedTime = DateUtils.formatElapsedTime(0)
        if (callState.isCallOngoingAndOperatorConnected) formattedTime = callState.callStatus.time
        emitViewState(callState.audioCallStarted(operatorMediaState, formattedTime))
        callNotificationUseCase.invoke(callState.callStatus.visitorMediaState, operatorMediaState)
        connectingTimerCounter.stop()
    }

    private fun onOperatorMediaStateUnknown() {
        d(TAG, "newOperatorMediaState: null")
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
            StartedCallVisualizer, StartedOmniCore -> newEngagementLoaded()
            is State.Update -> handleEngagementStateUpdate(state.updateState)
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
        val name = operator.name
        val imageUrl = operator.imageUrl
        operatorConnected(name, imageUrl)
    }

    private fun onOperatorChanged(operator: Operator) {
        val name = operator.name
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

    private fun showCallNotification() {
        if (hasCallNotificationChannelEnabledUseCase()) {
            callNotificationUseCase(callState.callStatus.visitorMediaState, callState.callStatus.operatorMediaState)
        }
    }

    private fun showLandscapeControls() {
        emitViewState(callState.landscapeControlsVisibleChanged(true))
        restartInactivityTimeCounter()
    }

    private fun minimizeView() {
        viewCallback?.minimizeView()
    }

    private fun observeQueueTicketState() {
        d(TAG, "observeQueueTicketState")
        disposable.add(
            ticketStateChangeToUnstaffedUseCase.execute().subscribe({ dialogController.showNoMoreOperatorsAvailableDialog() }) {
                e(TAG, "Error happened while observing queue state : $it")
            }
        )
    }
}
