package com.glia.widgets.call;

import android.text.format.DateUtils;

import androidx.annotation.Nullable;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.Operator;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.MediaDirection;
import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.androidsdk.engagement.Survey;
import com.glia.androidsdk.omnibrowse.Omnibrowse;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.androidsdk.screensharing.ScreenSharing;
import com.glia.widgets.Constants;
import com.glia.widgets.call.domain.HandleCallPermissionsUseCase;
import com.glia.widgets.call.domain.ToggleVisitorAudioMediaMuteUseCase;
import com.glia.widgets.call.domain.ToggleVisitorVideoUseCase;
import com.glia.widgets.chat.domain.UpdateFromCallScreenUseCase;
import com.glia.widgets.core.audio.domain.TurnSpeakerphoneUseCase;
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerUseCase;
import com.glia.widgets.core.chathead.domain.SetPendingSurveyUsedUseCase;
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager;
import com.glia.widgets.core.dialog.DialogController;
import com.glia.widgets.core.dialog.domain.IsShowEnableCallNotificationChannelDialogUseCase;
import com.glia.widgets.core.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase;
import com.glia.widgets.core.engagement.domain.AcknowledgmentDialogUseCase;
import com.glia.widgets.core.engagement.domain.GetEngagementStateFlowableUseCase;
import com.glia.widgets.core.engagement.domain.GliaEndEngagementUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.engagement.domain.IsOngoingEngagementUseCase;
import com.glia.widgets.core.engagement.domain.ShouldShowMediaEngagementViewUseCase;
import com.glia.widgets.core.engagement.domain.model.EngagementStateEvent;
import com.glia.widgets.core.engagement.domain.model.EngagementStateEventVisitor;
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepository;
import com.glia.widgets.core.mediaupgradeoffer.MediaUpgradeOfferRepositoryCallback;
import com.glia.widgets.core.mediaupgradeoffer.domain.AddMediaUpgradeOfferCallbackUseCase;
import com.glia.widgets.core.mediaupgradeoffer.domain.RemoveMediaUpgradeOfferCallbackUseCase;
import com.glia.widgets.core.notification.domain.CallNotificationUseCase;
import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase;
import com.glia.widgets.core.operator.domain.RemoveOperatorMediaStateListenerUseCase;
import com.glia.widgets.core.permissions.domain.HasCallNotificationChannelEnabledUseCase;
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase;
import com.glia.widgets.core.queue.domain.GliaQueueForMediaEngagementUseCase;
import com.glia.widgets.core.queue.domain.QueueTicketStateChangeToUnstaffedUseCase;
import com.glia.widgets.core.queue.domain.exception.QueueingOngoingException;
import com.glia.widgets.core.survey.OnSurveyListener;
import com.glia.widgets.core.survey.domain.GliaSurveyUseCase;
import com.glia.widgets.core.visitor.VisitorMediaUpdatesListener;
import com.glia.widgets.core.visitor.domain.AddVisitorMediaStateListenerUseCase;
import com.glia.widgets.core.visitor.domain.RemoveVisitorMediaStateListenerUseCase;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.CommonExtensionsKt;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.MinimizeHandler;
import com.glia.widgets.view.head.controller.ServiceChatHeadController;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import kotlin.Unit;

public class CallController implements
        GliaOnEngagementUseCase.Listener,
        GliaOnEngagementEndUseCase.Listener,
        OnSurveyListener,
        VisitorMediaUpdatesListener {

    private static final int MAX_IDLE_TIME = 3200;
    private static final int INACTIVITY_TIMER_TICKER_VALUE = 400;
    private static final int INACTIVITY_TIMER_DELAY_VALUE = 0;
    private final GliaSdkConfigurationManager sdkConfigurationManager;
    private final MediaUpgradeOfferRepository mediaUpgradeOfferRepository;
    private final TimeCounter callTimer;
    private final TimeCounter inactivityTimeCounter;
    private final TimeCounter connectingTimerCounter;
    private final MinimizeHandler minimizeHandler;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final CallNotificationUseCase callNotificationUseCase;
    private final GliaQueueForMediaEngagementUseCase gliaQueueForMediaEngagementUseCase;
    private final GliaCancelQueueTicketUseCase cancelQueueTicketUseCase;
    private final GliaOnEngagementUseCase onEngagementUseCase;
    private final AddOperatorMediaStateListenerUseCase addOperatorMediaStateListenerUseCase;
    private final RemoveOperatorMediaStateListenerUseCase removeOperatorMediaStateListenerUseCase;
    private final GliaOnEngagementEndUseCase onEngagementEndUseCase;
    private final GliaEndEngagementUseCase endEngagementUseCase;
    private final ShouldShowMediaEngagementViewUseCase shouldShowMediaEngagementViewUseCase;
    private final IsShowOverlayPermissionRequestDialogUseCase isShowOverlayPermissionRequestDialogUseCase;
    private final HasCallNotificationChannelEnabledUseCase hasCallNotificationChannelEnabledUseCase;
    private final IsShowEnableCallNotificationChannelDialogUseCase isShowEnableCallNotificationChannelDialogUseCase;
    private final AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase;
    private final RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase;
    private final AddMediaUpgradeOfferCallbackUseCase addMediaUpgradeCallbackUseCase;
    private final RemoveMediaUpgradeOfferCallbackUseCase removeMediaUpgradeCallbackUseCase;
    private final SetPendingSurveyUsedUseCase setPendingSurveyUsedUseCase;
    private final DialogController dialogController;
    private final GliaSurveyUseCase surveyUseCase;
    private final ToggleVisitorAudioMediaMuteUseCase toggleVisitorAudioMediaMuteUseCase;
    private final ToggleVisitorVideoUseCase toggleVisitorVideoUseCase;
    private final GetEngagementStateFlowableUseCase getGliaEngagementStateFlowableUseCase;
    private final UpdateFromCallScreenUseCase updateFromCallScreenUseCase;
    private final QueueTicketStateChangeToUnstaffedUseCase ticketStateChangeToUnstaffedUseCase;
    private final IsCallVisualizerUseCase isCallVisualizerUseCase;
    private final IsOngoingEngagementUseCase isOngoingEngagementUseCase;
    private final TurnSpeakerphoneUseCase turnSpeakerphoneUseCase;
    private final HandleCallPermissionsUseCase handleCallPermissionsUseCase;
    private final AcknowledgmentDialogUseCase acknowledgmentDialogUseCase;
    private final String TAG = "CallController";
    private final CompositeDisposable disposable = new CompositeDisposable();
    private CallViewCallback viewCallback;
    private MediaUpgradeOfferRepositoryCallback mediaUpgradeOfferRepositoryCallback;
    private TimeCounter.FormattedTimerStatusListener callTimerStatusListener;
    private TimeCounter.RawTimerStatusListener inactivityTimerStatusListener;
    private TimeCounter.RawTimerStatusListener connectingTimerStatusListener;
    private volatile CallState callState;
    private final GliaOperatorMediaRepository.OperatorMediaStateListener operatorMediaStateListener = this::onNewOperatorMediaState;
    private MessagesNotSeenHandler.MessagesNotSeenHandlerListener messagesNotSeenHandlerListener;
    private boolean isVisitorEndEngagement = false;
    private boolean shouldHandleEndedEngagement = false;

    public CallController(
            GliaSdkConfigurationManager sdkConfigurationManager,
            MediaUpgradeOfferRepository mediaUpgradeOfferRepository,
            TimeCounter sharedTimer,
            CallViewCallback callViewCallback,
            TimeCounter inactivityTimeCounter,
            TimeCounter connectingTimerCounter,
            MinimizeHandler minimizeHandler,
            DialogController dialogController,
            MessagesNotSeenHandler messagesNotSeenHandler,
            CallNotificationUseCase callNotificationUseCase,
            GliaQueueForMediaEngagementUseCase queueForMediaEngagementUseCase,
            GliaCancelQueueTicketUseCase cancelQueueTicketUseCase,
            GliaOnEngagementUseCase onEngagementUseCase,
            AddOperatorMediaStateListenerUseCase addOperatorMediaStateListenerUseCase,
            RemoveOperatorMediaStateListenerUseCase removeOperatorMediaStateListenerUseCase,
            GliaOnEngagementEndUseCase onEngagementEndUseCase,
            GliaEndEngagementUseCase endEngagementUseCase,
            ShouldShowMediaEngagementViewUseCase shouldShowMediaEngagementViewUseCase,
            IsShowOverlayPermissionRequestDialogUseCase isShowOverlayPermissionRequestDialogUseCase,
            HasCallNotificationChannelEnabledUseCase hasCallNotificationChannelEnabledUseCase,
            IsShowEnableCallNotificationChannelDialogUseCase isShowEnableCallNotificationChannelDialogUseCase,
            GliaSurveyUseCase surveyUseCase,
            AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase,
            RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase,
            AddMediaUpgradeOfferCallbackUseCase addMediaUpgradeCallbackUseCase,
            RemoveMediaUpgradeOfferCallbackUseCase removeMediaUpgradeCallbackUseCase,
            ToggleVisitorAudioMediaMuteUseCase toggleVisitorAudioMediaMuteUseCase,
            ToggleVisitorVideoUseCase toggleVisitorVideoUseCase,
            GetEngagementStateFlowableUseCase getGliaEngagementStateFlowableUseCase,
            UpdateFromCallScreenUseCase updateFromCallScreenUseCase,
            QueueTicketStateChangeToUnstaffedUseCase ticketStateChangeToUnstaffedUseCase,
            IsCallVisualizerUseCase isCallVisualizerUseCase,
            IsOngoingEngagementUseCase isOngoingEngagementUseCase,
            SetPendingSurveyUsedUseCase setPendingSurveyUsedUseCase,
            TurnSpeakerphoneUseCase turnSpeakerphoneUseCase,
            AcknowledgmentDialogUseCase acknowledgmentDialogUseCase,
            HandleCallPermissionsUseCase handleCallPermissionsUseCase) {
        Logger.d(TAG, "constructor");
        this.sdkConfigurationManager = sdkConfigurationManager;
        this.viewCallback = callViewCallback;
        this.callState = new CallState.Builder()
                .setIntegratorCallStarted(false)
                .setVisible(false)
                .setMessagesNotSeen(0)
                .setCallStatus(new CallStatus.EngagementNotOngoing(null))
                .setLandscapeLayoutControlsVisible(false)
                .setIsSpeakerOn(false)
                .setIsMuted(false)
                .setHasVideo(false)
                .setIsCallVisualizer(isCallVisualizerUseCase.invoke())
                .createCallState();
        this.dialogController = dialogController;
        this.callTimer = sharedTimer;
        this.mediaUpgradeOfferRepository = mediaUpgradeOfferRepository;
        this.inactivityTimeCounter = inactivityTimeCounter;
        this.connectingTimerCounter = connectingTimerCounter;
        this.minimizeHandler = minimizeHandler;
        this.messagesNotSeenHandler = messagesNotSeenHandler;

        this.callNotificationUseCase = callNotificationUseCase;
        this.gliaQueueForMediaEngagementUseCase = queueForMediaEngagementUseCase;
        this.cancelQueueTicketUseCase = cancelQueueTicketUseCase;
        this.onEngagementUseCase = onEngagementUseCase;
        this.addOperatorMediaStateListenerUseCase = addOperatorMediaStateListenerUseCase;
        this.removeOperatorMediaStateListenerUseCase = removeOperatorMediaStateListenerUseCase;
        this.onEngagementEndUseCase = onEngagementEndUseCase;
        this.endEngagementUseCase = endEngagementUseCase;
        this.shouldShowMediaEngagementViewUseCase = shouldShowMediaEngagementViewUseCase;
        this.isShowOverlayPermissionRequestDialogUseCase = isShowOverlayPermissionRequestDialogUseCase;
        this.hasCallNotificationChannelEnabledUseCase = hasCallNotificationChannelEnabledUseCase;
        this.isShowEnableCallNotificationChannelDialogUseCase = isShowEnableCallNotificationChannelDialogUseCase;
        this.surveyUseCase = surveyUseCase;
        this.addVisitorMediaStateListenerUseCase = addVisitorMediaStateListenerUseCase;
        this.removeVisitorMediaStateListenerUseCase = removeVisitorMediaStateListenerUseCase;
        this.addMediaUpgradeCallbackUseCase = addMediaUpgradeCallbackUseCase;
        this.removeMediaUpgradeCallbackUseCase = removeMediaUpgradeCallbackUseCase;
        this.toggleVisitorAudioMediaMuteUseCase = toggleVisitorAudioMediaMuteUseCase;
        this.toggleVisitorVideoUseCase = toggleVisitorVideoUseCase;
        this.getGliaEngagementStateFlowableUseCase = getGliaEngagementStateFlowableUseCase;
        this.updateFromCallScreenUseCase = updateFromCallScreenUseCase;
        this.ticketStateChangeToUnstaffedUseCase = ticketStateChangeToUnstaffedUseCase;
        this.isCallVisualizerUseCase = isCallVisualizerUseCase;
        this.isOngoingEngagementUseCase = isOngoingEngagementUseCase;
        this.setPendingSurveyUsedUseCase = setPendingSurveyUsedUseCase;
        this.turnSpeakerphoneUseCase = turnSpeakerphoneUseCase;
        this.handleCallPermissionsUseCase = handleCallPermissionsUseCase;
        this.acknowledgmentDialogUseCase = acknowledgmentDialogUseCase;

        if (isCallVisualizerUseCase.invoke()) {
            shouldShowMediaEngagementView(true);
        }
    }

    @Override
    public void onNewVisitorMediaState(VisitorMediaState visitorMediaState) {
        emitViewState(callState.visitorMediaStateChanged(visitorMediaState));
    }

    @Override
    public void onHoldChanged(boolean isOnHold) {
        emitViewState(callState.setOnHold(isOnHold));
    }

    @Override
    public void engagementEnded() {
        Logger.i(TAG, "Engagement ended");
        stop();
        if (!isOngoingEngagementUseCase.invoke()) {
            dialogController.dismissDialogs();
        }
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        Logger.d(TAG, "engagementSuccess");
        mediaUpgradeOfferRepository.startListening();
        if (!connectingTimerCounter.isRunning()) {
            connectingTimerCounter.startNew(
                    Constants.CALL_TIMER_DELAY,
                    Constants.CALL_TIMER_INTERVAL_VALUE
            );
        }
        emitViewState(callState.engagementStarted());
    }

    public void startCall(String companyName,
                          String queueId,
                          String visitorContextAssetId,
                          Engagement.MediaType mediaType,
                          boolean useOverlays,
                          ScreenSharing.Mode screenSharingMode,
                          boolean isUpgradeToCall,
                          ServiceChatHeadController serviceChatHeadController) {
        if (isUpgradeToCall) {
            initCall(companyName, queueId, visitorContextAssetId, mediaType, useOverlays, screenSharingMode);
            if (serviceChatHeadController != null) {
              serviceChatHeadController.init();
            }
            return;
        }
        handleCallPermissionsUseCase.invoke(mediaType, isPermissionsGranted -> {
            if (isPermissionsGranted) {
                initCall(companyName, queueId, visitorContextAssetId, mediaType, useOverlays, screenSharingMode);
                if (serviceChatHeadController != null) {
                  serviceChatHeadController.init();
                }
            } else {
                if (viewCallback != null) {
                    viewCallback.showMissingPermissionsDialog();
                }
            }
            return null;
        });
    }

    private void initCall(String companyName,
                          String queueId,
                          String visitorContextAssetId,
                          Engagement.MediaType mediaType,
                          boolean useOverlays,
                          ScreenSharing.Mode screenSharingMode) {
        sdkConfigurationManager.setUseOverlay(useOverlays);
        sdkConfigurationManager.setScreenSharingMode(screenSharingMode);

        if (surveyUseCase.hasResult()) {
            return;
        }
        if (isShowOverlayPermissionRequestDialogUseCase.execute()) {
            dialogController.showOverlayPermissionsDialog();
        }
        messagesNotSeenHandler.onNavigatedToCall();
        if (callState.integratorCallStarted || dialogController.isShowingChatEnderDialog()) {
            return;
        }
        emitViewState(callState.initCall(companyName, queueId, visitorContextAssetId, mediaType));
        createNewTimerStatusCallback();
        initControllerCallbacks();
        initMessagesNotSeenCallback();
        onEngagementUseCase.execute(this);
        addOperatorMediaStateListenerUseCase.execute(operatorMediaStateListener);
        acknowledgmentDialogUseCase.invoke(shouldShow -> {
            if (shouldShow) {
                dialogController.showLiveObservationOptInDialog();
            } else {
                queueForEngagement(queueId, visitorContextAssetId, mediaType);
            }
            return Unit.INSTANCE;
        });
        onEngagementEndUseCase.execute(this);
        mediaUpgradeOfferRepository.addCallback(mediaUpgradeOfferRepositoryCallback);
        inactivityTimeCounter.addRawValueListener(inactivityTimerStatusListener);
        connectingTimerCounter.addRawValueListener(connectingTimerStatusListener);
        minimizeHandler.addListener(this::minimizeView);
        messagesNotSeenHandler.addListener(messagesNotSeenHandlerListener);
    }

    public void onLiveObservationDialogRequested() {
        if (isOngoingEngagementUseCase.invoke()) return;
        viewCallback.showLiveObservationOptInDialog(callState.companyName);
    }

    private void queueForEngagement(String queueId, String visitorContextAssetId, Engagement.MediaType mediaType) {
        disposable.add(
            gliaQueueForMediaEngagementUseCase
                .execute(queueId, visitorContextAssetId, mediaType)
                .subscribe(
                    this::queueForEngagementStarted,
                    this::queueForEngagementError
                )
        );
    }

    public void onLiveObservationDialogAllowed() {
        Logger.d(TAG, "onLiveObservationDialogAllowed");
        dialogController.dismissCurrentDialog();
        queueForEngagement(callState.queueId, callState.visitorContextAssetId, callState.requestedMediaType);
    }

    public void onLiveObservationDialogRejected() {
        Logger.d(TAG, "onLiveObservationDialogRejected");
        isVisitorEndEngagement = true;
        stop();
        dialogController.dismissDialogs();
    }

    public void onDestroy(boolean retain) {
        Logger.d(TAG, "onDestroy, retain: " + retain);
        if (viewCallback != null) {
            Logger.d(TAG, "destroyingView");
            viewCallback.destroyView();
        }
        viewCallback = null;

        if (!retain) {
            disposable.clear();
            mediaUpgradeOfferRepository.stopAll();
            mediaUpgradeOfferRepositoryCallback = null;
            if (callTimerStatusListener != null) {
                callTimer.removeFormattedValueListener(callTimerStatusListener);
                callTimerStatusListener = null;
            }
            callTimer.clear();
            inactivityTimeCounter.clear();
            connectingTimerCounter.clear();
            inactivityTimerStatusListener = null;
            minimizeHandler.clear();
            messagesNotSeenHandler.removeListener(messagesNotSeenHandlerListener);
            messagesNotSeenHandlerListener = null;

            onEngagementUseCase.unregisterListener(this);
            onEngagementEndUseCase.unregisterListener(this);
            shouldHandleEndedEngagement = false;
            removeOperatorMediaStateListenerUseCase.invoke(operatorMediaStateListener);
        }
    }

    public void onPause() {
        surveyUseCase.unregisterListener(this);
        removeVisitorMediaStateListenerUseCase.execute(this);
        if (mediaUpgradeOfferRepositoryCallback != null) {
            removeMediaUpgradeCallbackUseCase.invoke(mediaUpgradeOfferRepositoryCallback);
        }
    }

    public void leaveChatClicked() {
        Logger.d(TAG, "leaveChatClicked");
        showExitChatDialog();
    }

    public void setViewCallback(CallViewCallback callViewCallback) {
        Logger.d(TAG, "setViewCallback");
        this.viewCallback = callViewCallback;
        if (viewCallback != null) {
            viewCallback.emitState(callState);
        }
    }

    public void endEngagementDialogYesClicked() {
        Logger.d(TAG, "endEngagementDialogYesClicked");
        isVisitorEndEngagement = true;
        stop();
        dialogController.dismissDialogs();
    }

    public void endEngagementDialogDismissed() {
        Logger.d(TAG, "endEngagementDialogDismissed");
        dialogController.dismissCurrentDialog();
    }

    public void noMoreOperatorsAvailableDismissed() {
        Logger.d(TAG, "noMoreOperatorsAvailableDismissed");
        stop();
        dialogController.dismissDialogs();
    }

    public void unexpectedErrorDialogDismissed() {
        Logger.d(TAG, "unexpectedErrorDialogDismissed");
        stop();
        dialogController.dismissDialogs();
    }

    public void overlayPermissionsDialogDismissed() {
        Logger.d(TAG, "overlayPermissionsDialogDismissed");
        dialogController.dismissCurrentDialog();
    }

    public void leaveChatQueueClicked() {
        Logger.d(TAG, "leaveChatQueueClicked");
        dialogController.showExitQueueDialog();
    }

    public void onResume() {
        Logger.d(TAG, "onResume\n");
        if (shouldHandleEndedEngagement) {
            //Engagement has been started
            if (!isOngoingEngagementUseCase.invoke()) {
                // Engagement has ended
                surveyUseCase.registerListener(this);
            } else {
                // Engagement is ongoing
                onResumeSetup();
            }
        } else {
            // New session
            onResumeSetup();
        }
    }

    private void onResumeSetup() {
        addVisitorMediaStateListenerUseCase.execute(this);
        showCallNotification();
        showLandscapeControls();
        surveyUseCase.registerListener(this);
        if (isCallVisualizerUseCase.invoke()) {
            subscribeToOmnibrowseEvents();
        } else {
            subscribeToEngagementStateChange();
        }
        if (mediaUpgradeOfferRepositoryCallback != null) {
            addMediaUpgradeCallbackUseCase.invoke(mediaUpgradeOfferRepositoryCallback);
        }
    }

    public void chatButtonClicked() {
        Logger.d(TAG, "chatButtonClicked");
        updateFromCallScreenUseCase.updateFromCallScreen(true);
        if (viewCallback != null) {
            viewCallback.navigateToChat();
        }
        onDestroy(true);
        messagesNotSeenHandler.callChatButtonClicked();
    }

    public void acceptUpgradeOfferClicked(MediaUpgradeOffer mediaUpgradeOffer) {
        Logger.i(TAG, "Upgrade offer accepted by visitor");
        mediaUpgradeOfferRepository.acceptOffer(
                mediaUpgradeOffer,
                MediaUpgradeOfferRepository.Submitter.CALL
        );
        dialogController.dismissCurrentDialog();
    }

    public void declineUpgradeOfferClicked(MediaUpgradeOffer mediaUpgradeOffer) {
        Logger.i(TAG, "Upgrade offer declined by visitor");
        mediaUpgradeOfferRepository.declineOffer(
                mediaUpgradeOffer,
                MediaUpgradeOfferRepository.Submitter.CALL
        );
        dialogController.dismissCurrentDialog();
    }

    public void onUserInteraction() {
        if (viewCallback == null) {
            return;
        }
        showLandscapeControls();
    }

    public void minimizeButtonClicked() {
        Logger.d(TAG, "minimizeButtonClicked");
        minimizeHandler.minimize();
    }

    public void muteButtonClicked() {
        disposable.add(
                toggleVisitorAudioMediaMuteUseCase
                        .execute()
                        .subscribe(
                                () -> { // no-op
                                },
                                error -> Logger.e(TAG, "Muting failed with error: " + error.toString())
                        )
        );
    }

    public void videoButtonClicked() {
        disposable.add(
                toggleVisitorVideoUseCase
                        .execute()
                        .subscribe(
                                () -> { // no-op
                                },
                                error -> Logger.e(TAG, "Toggling visitor video error: " + error.toString())
                        )
        );
    }

    public void notificationsDialogDismissed() {
        dialogController.dismissCurrentDialog();
    }

    public void onNewOperatorMediaState(@Nullable OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: " + operatorMediaState +
                ", timertaskrunning: " + callTimer.isRunning());
        if (operatorMediaState == null) {
            return;
        }
        if (operatorMediaState.getVideo() != null) {
            if (isShowEnableCallNotificationChannelDialogUseCase.execute()) {
                dialogController.showEnableCallNotificationChannelDialog();
            }

            onOperatorMediaStateVideo(operatorMediaState);
        } else if (operatorMediaState.getAudio() != null) {
            if (isShowEnableCallNotificationChannelDialogUseCase.execute()) {
                dialogController.showEnableCallNotificationChannelDialog();
            }

            onOperatorMediaStateAudio(operatorMediaState);
        } else {
            onOperatorMediaStateUnknown();
        }
        if (callState.isMediaEngagementStarted() && !callTimer.isRunning() && callTimerStatusListener != null) {
            callTimer.startNew(Constants.CALL_TIMER_DELAY, Constants.CALL_TIMER_INTERVAL_VALUE);
        }
    }

    @Override
    public void onSurveyLoaded(@Nullable Survey survey) {
        Logger.i(TAG, "Survey loaded");
        setPendingSurveyUsedUseCase.invoke();
        if (viewCallback != null && survey != null) {
            viewCallback.navigateToSurvey(survey);
            Dependencies.getControllerFactory().destroyControllers();
        } else if (shouldHandleEndedEngagement && !isVisitorEndEngagement) {
            shouldHandleEndedEngagement = false;
            dialogController.showEngagementEndedDialog();
        } else {
            Dependencies.getControllerFactory().destroyControllers();
        }
    }

    public void onSpeakerButtonPressed() {
        boolean newValue = !callState.isSpeakerOn;
        Logger.d(TAG, "onSpeakerButtonPressed, new value: " + newValue);
        emitViewState(callState.speakerValueChanged(newValue));
        turnSpeakerphoneUseCase.invoke(newValue);
    }

    public void queueForEngagementStarted() {
        observeQueueTicketState();
    }

    public void queueForEngagementStopped() {
        Logger.i(TAG, "Queue for engagement stopped due to error or empty queue");
    }

    public void queueForEngagementError(Throwable exception) {
        if (exception != null) {
            Logger.e(TAG, exception.toString());
            if (exception instanceof GliaException) {
                switch (((GliaException) exception).cause) {
                    case QUEUE_CLOSED:
                    case QUEUE_FULL:
                        dialogController.showNoMoreOperatorsAvailableDialog();
                        break;
                    default:
                        dialogController.showUnexpectedErrorDialog();
                }
                emitViewState(callState.changeVisibility(false));
            } else if (exception instanceof QueueingOngoingException) {
                queueForEngagementStarted();
            }
        }
    }

    public boolean shouldShowMediaEngagementView(boolean isUpgradeToCall) {
        return shouldShowMediaEngagementViewUseCase.execute(isUpgradeToCall);
    }

    public void onBackClicked() {
        updateFromCallScreenUseCase.updateFromCallScreen(false);
    }

    private synchronized void emitViewState(CallState state) {
        if (setState(state) && viewCallback != null) {
            Logger.d(TAG, "Emit state:\n" + state.toString());
            viewCallback.emitState(callState);
        }

    }

    private synchronized boolean setState(CallState state) {
        if (this.callState.equals(state)) return false;
        this.callState = state;
        return true;
    }

    private void initControllerCallbacks() {
        mediaUpgradeOfferRepositoryCallback = new MediaUpgradeOfferRepositoryCallback() {
            @Override
            public void newOffer(MediaUpgradeOffer offer) {
                if (offer.video == MediaDirection.NONE && offer.audio == MediaDirection.TWO_WAY) {
                    // audio call
                    Logger.d(TAG, "Audio upgrade requested");
                    showUpgradeAudioDialog(offer);
                } else if (offer.video == MediaDirection.TWO_WAY) {
                    // 2 way video call
                    Logger.d(TAG, "2 way video upgrade requested");
                    showUpgradeVideoDialog2Way(offer);
                } else if (offer.video == MediaDirection.ONE_WAY) {
                    // 1 way video call
                    Logger.d(TAG, "1 way video upgrade requested");
                    showUpgradeVideoDialog1Way(offer);
                }
            }

            @Override
            public void upgradeOfferChoiceSubmitSuccess(
                    MediaUpgradeOffer offer,
                    MediaUpgradeOfferRepository.Submitter submitter
            ) {
                Logger.d(TAG, "upgradeOfferChoiceSubmitSuccess");
                Engagement.MediaType mediaType;
                if (offer.video != null && offer.video != MediaDirection.NONE) {
                    mediaType = Engagement.MediaType.VIDEO;
                } else {
                    mediaType = Engagement.MediaType.AUDIO;
                }
                emitViewState(callState.changeRequestedMediaType(mediaType));
            }

            @Override
            public void upgradeOfferChoiceDeclinedSuccess(
                    MediaUpgradeOfferRepository.Submitter submitter
            ) {
                Logger.d(TAG, "upgradeOfferChoiceDeclinedSuccess");
            }
        };

        inactivityTimerStatusListener = new TimeCounter.RawTimerStatusListener() {
            @Override
            public void onNewRawTimerValue(int timerValue) {
                if (callState.isVideoCall()) {
                    Logger.d(TAG, "inactivityTimer onNewTimerValue: " + timerValue);
                    emitViewState(callState.landscapeControlsVisibleChanged(timerValue < MAX_IDLE_TIME));
                }
                if (timerValue >= MAX_IDLE_TIME) {
                    inactivityTimeCounter.stop();
                }
            }

            @Override
            public void onRawTimerCancelled() {

            }
        };

        connectingTimerStatusListener = new TimeCounter.RawTimerStatusListener() {

            @Override
            public void onNewRawTimerValue(int timerValue) {
                if (callState.isCallOngoingAndOperatorIsConnecting()) {
                    emitViewState(
                            callState
                                    .connectingTimerValueChanged(
                                            String.valueOf(TimeUnit.MILLISECONDS.toSeconds(timerValue))
                                    )
                    );
                }
            }

            @Override
            public void onRawTimerCancelled() {

            }
        };
    }

    private void initMessagesNotSeenCallback() {
        messagesNotSeenHandlerListener = count -> emitViewState(callState.changeNumberOfMessages(count));
    }

    private void showUpgradeAudioDialog(MediaUpgradeOffer mediaUpgradeOffer) {
        if (callState.isMediaEngagementStarted()) {
            dialogController.showUpgradeAudioDialog(mediaUpgradeOffer, callState.callStatus.getFormattedOperatorName());
        }
    }

    private void showUpgradeVideoDialog2Way(MediaUpgradeOffer mediaUpgradeOffer) {
        if (callState.isMediaEngagementStarted())
            dialogController.showUpgradeVideoDialog2Way(
                    mediaUpgradeOffer,
                    callState.callStatus.getFormattedOperatorName()
            );
    }

    private void showUpgradeVideoDialog1Way(MediaUpgradeOffer mediaUpgradeOffer) {
        if (callState.isMediaEngagementStarted())
            dialogController.showUpgradeVideoDialog1Way(
                    mediaUpgradeOffer,
                    callState.callStatus.getFormattedOperatorName()
            );
    }

    private void showExitChatDialog() {
        if (callState.isMediaEngagementStarted()) {
            dialogController.showExitChatDialog(callState.callStatus.getFormattedOperatorName());
        }
    }

    private void createNewTimerStatusCallback() {
        if (callTimerStatusListener == null) {
            callTimerStatusListener = new TimeCounter.FormattedTimerStatusListener() {
                @Override
                public void onNewFormattedTimerValue(String formattedValue) {
                    if (callState.showCallTimerView()) {
                        emitViewState(callState.newStartedCallTimerValue(formattedValue));
                    }
                }

                @Override
                public void onFormattedTimerCancelled() {
                    // Should only happen if engagement ends.
                }
            };
            callTimer.addFormattedValueListener(callTimerStatusListener);
        }
    }

    private void restartInactivityTimeCounter() {
        inactivityTimeCounter.startNew(INACTIVITY_TIMER_DELAY_VALUE, INACTIVITY_TIMER_TICKER_VALUE);
    }

    private void stop() {
        Logger.d(TAG, "Stop, engagement ended");
        disposable.add(
                cancelQueueTicketUseCase.execute()
                        .subscribe(
                                this::queueForEngagementStopped,
                                throwable -> {
                                    if (throwable.getMessage() != null) Logger.e(TAG, "cancelQueueTicketUseCase error: " + throwable.getMessage());
                                }
                        )
        );
        endEngagementUseCase.invoke();
        mediaUpgradeOfferRepository.stopAll();
        emitViewState(callState.stop());
    }

    private void onOperatorMediaStateVideo(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: video");
        String formattedTime = DateUtils.formatElapsedTime(0);
        if (callState.isCallOngoingAndOperatorConnected()) {
            formattedTime = callState.callStatus.getTime();
        }
        emitViewState(callState.videoCallOperatorVideoStarted(
                operatorMediaState,
                formattedTime
        ));
        callNotificationUseCase.invoke(callState.callStatus.getVisitorMediaState(), operatorMediaState);
        connectingTimerCounter.stop();
    }

    private void onOperatorMediaStateAudio(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: audio");
        String formattedTime = DateUtils.formatElapsedTime(0);
        if (callState.isCallOngoingAndOperatorConnected())
            formattedTime = callState.callStatus.getTime();

        emitViewState(callState.audioCallStarted(
                operatorMediaState,
                formattedTime
        ));

        callNotificationUseCase.invoke(callState.callStatus.getVisitorMediaState(), operatorMediaState);
        connectingTimerCounter.stop();
    }

    private void onOperatorMediaStateUnknown() {
        Logger.d(TAG, "newOperatorMediaState: null");
        if (callState.isMediaEngagementStarted()) {
            emitViewState(callState.backToOngoing());
        }
        callNotificationUseCase.removeAllNotifications();
        if (!connectingTimerCounter.isRunning()) {
            connectingTimerCounter.startNew(
                    Constants.CALL_TIMER_DELAY,
                    Constants.CALL_TIMER_INTERVAL_VALUE
            );
        }
    }

    private void subscribeToEngagementStateChange() {
        disposable.add(
                getGliaEngagementStateFlowableUseCase.execute()
                        .subscribe(
                                this::onEngagementStateChanged,
                                throwable -> Logger.e(TAG, "subscribeToEngagementStateChange error: " + throwable.getMessage())
                        )
        );
    }

    private void subscribeToOmnibrowseEvents() {
        Glia.omnibrowse.on(Omnibrowse.Events.ENGAGEMENT, omnibrowseEngagement -> {
            omnibrowseEngagement.getMedia().on(Media.Events.OPERATOR_STATE_UPDATE, this::onNewOperatorMediaState);
            omnibrowseEngagement.on(Engagement.Events.STATE_UPDATE, engagementState -> {
                switch (engagementState.getVisitorStatus()) {
                    case ENGAGED:
                        onEngagementOngoing(engagementState.getOperator());
                        break;
                    case TRANSFERRING:
                        Logger.e(TAG, "CallVisualizer state TRANSFERRING", new Exception("CallVisualizer flow does not support engagement transfers"));
                        break;
                    case UNKNOWN:
                        Logger.d(TAG, "CallVisualizer state UNKNOWN");
                        break;
                }
            });
            omnibrowseEngagement.on(Engagement.Events.END, engagementState -> {
                if (viewCallback != null) viewCallback.destroyView();
            });
        });
    }

    private void onEngagementStateChanged(EngagementStateEvent engagementState) {
        EngagementStateEventVisitor<Operator> visitor = new EngagementStateEventVisitor.OperatorVisitor();
        switch (engagementState.getType()) {
            case ENGAGEMENT_OPERATOR_CHANGED:
                onOperatorChanged(visitor.visit(engagementState));
                break;
            case ENGAGEMENT_OPERATOR_CONNECTED:
                shouldHandleEndedEngagement = true;
                onOperatorConnected(visitor.visit(engagementState));
                break;
            case ENGAGEMENT_TRANSFERRING:
                onTransferring();
                break;
            case ENGAGEMENT_ONGOING:
                onEngagementOngoing(visitor.visit(engagementState));
            case ENGAGEMENT_ENDED:
                break;
        }
    }

    private void onEngagementOngoing(Operator operator) {
        if (!(callState.callStatus instanceof CallStatus.EngagementOngoingAudioCallStarted) &&
                !(callState.callStatus instanceof CallStatus.EngagementOngoingVideoCallStarted)) {
            onOperatorConnected(operator);
        }
    }

    private void onOperatorConnected(Operator operator) {
        String name = operator.getName();
        String imageUrl = CommonExtensionsKt.getImageUrl(operator);
        operatorConnected(name, imageUrl);
    }

    private void onOperatorChanged(Operator operator) {
        String name = operator.getName();
        String imageUrl = CommonExtensionsKt.getImageUrl(operator);
        operatorChanged(name, imageUrl);
    }

    private void operatorChanged(String operatorName, String profileImgUrl) {
        emitViewState(callState.operatorConnecting(operatorName, profileImgUrl));
    }

    private void operatorConnected(String operatorName, String profileImgUrl) {
        if (callState.isCallOngoingAndOperatorIsConnecting()) {
            emitViewState(callState.operatorConnecting(operatorName, profileImgUrl));
        } else {
            if (callState.isAudioCall()) {
                onOperatorConnectedAndAudioCallOngoing(operatorName, profileImgUrl);
            } else {
                onOperatorConnectedAndVideoCallOngoing(operatorName, profileImgUrl);
            }
        }
    }

    private void onOperatorConnectedAndVideoCallOngoing(String operatorName, String profileImgUrl) {
        emitViewState(
                callState
                        .operatorConnecting(operatorName, profileImgUrl)
                        .videoCallOperatorVideoStarted(
                                callState.callStatus.getOperatorMediaState(),
                                callState.callStatus.getTime()
                        )
        );
    }

    private void onOperatorConnectedAndAudioCallOngoing(String operatorName, String profileImgUrl) {
        emitViewState(
                callState
                        .operatorConnecting(operatorName, profileImgUrl)
                        .audioCallStarted(
                                callState.callStatus.getOperatorMediaState(),
                                callState.callStatus.getTime()
                        )
        );
    }

    private void onTransferring() {
        emitViewState(callState.setTransferring());
    }

    private void showCallNotification() {
        if (hasCallNotificationChannelEnabledUseCase.invoke()) {
            callNotificationUseCase.invoke(
                    callState.callStatus.getVisitorMediaState(),
                    callState.callStatus.getOperatorMediaState()
            );
        }
    }

    private void showLandscapeControls() {
        emitViewState(callState.landscapeControlsVisibleChanged(true));
        restartInactivityTimeCounter();
    }

    private void minimizeView() {
        if (viewCallback != null) viewCallback.minimizeView();
    }

    private void observeQueueTicketState() {
        Logger.d(TAG, "observeQueueTicketState");
        disposable.add(
                ticketStateChangeToUnstaffedUseCase
                        .execute()
                        .subscribe(dialogController::showNoMoreOperatorsAvailableDialog,
                                error -> Logger.e(TAG, "Error happened while observing queue state : " + error.toString())
                        )
        );
    }
}
