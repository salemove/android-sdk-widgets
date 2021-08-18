package com.glia.widgets.call;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.comms.Media;
import com.glia.androidsdk.comms.MediaDirection;
import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.Constants;
import com.glia.widgets.core.engagement.domain.ShouldShowMediaEngagementViewUseCase;
import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase;
import com.glia.widgets.core.queue.QueueTicketsEventsListener;
import com.glia.widgets.core.queue.domain.GliaQueueForMediaEngagementUseCase;
import com.glia.widgets.dialog.DialogController;
import com.glia.widgets.core.queue.domain.GliaCancelQueueTicketUseCase;
import com.glia.widgets.core.engagement.domain.GliaEndEngagementUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.visitor.domain.GliaOnVisitorMediaStateUseCase;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.MediaUpgradeOfferRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepositoryCallback;
import com.glia.widgets.model.MessagesNotSeenHandler;
import com.glia.widgets.model.MinimizeHandler;
import com.glia.widgets.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.notification.domain.ShowAudioCallNotificationUseCase;
import com.glia.widgets.notification.domain.ShowVideoCallNotificationUseCase;
import com.glia.widgets.permissions.domain.HasCallNotificationChannelEnabledUseCase;
import com.glia.widgets.dialog.domain.IsShowEnableCallNotificationChannelDialogUseCase;
import com.glia.widgets.dialog.domain.IsShowOverlayPermissionRequestDialogUseCase;

import java.util.concurrent.TimeUnit;

public class CallController implements
        GliaOnEngagementUseCase.Listener,
        GliaOnVisitorMediaStateUseCase.Listener,
        GliaOnEngagementEndUseCase.Listener {

    private CallViewCallback viewCallback;
    private MediaUpgradeOfferRepositoryCallback mediaUpgradeOfferRepositoryCallback;
    private TimeCounter.FormattedTimerStatusListener callTimerStatusListener;
    private TimeCounter.RawTimerStatusListener inactivityTimerStatusListener;
    private TimeCounter.RawTimerStatusListener connectingTimerStatusListener;
    private MinimizeHandler.OnMinimizeCalledListener minimizeCalledListener;
    private MessagesNotSeenHandler.MessagesNotSeenHandlerListener messagesNotSeenHandlerListener;
    private final MediaUpgradeOfferRepository mediaUpgradeOfferRepository;
    private final TimeCounter callTimer;
    private final TimeCounter inactivityTimeCounter;
    private final TimeCounter connectingTimerCounter;
    private final MinimizeHandler minimizeHandler;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final static int MAX_IDLE_TIME = 3200;
    private final static int INACTIVITY_TIMER_TICKER_VALUE = 400;
    private final static int INACTIVITY_TIMER_DELAY_VALUE = 0;

    private final ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase;
    private final ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase;
    private final RemoveCallNotificationUseCase removeCallNotificationUseCase;
    private final GliaQueueForMediaEngagementUseCase gliaQueueForMediaEngagementUseCase;
    private final GliaCancelQueueTicketUseCase cancelQueueTicketUseCase;
    private final GliaOnEngagementUseCase onEngagementUseCase;
    private final AddOperatorMediaStateListenerUseCase addOperatorMediaStateListenerUseCase;
    private final GliaOnVisitorMediaStateUseCase onVisitorMediaStateUseCase;
    private final GliaOnEngagementEndUseCase onEngagementEndUseCase;
    private final GliaEndEngagementUseCase endEngagementUseCase;
    private final ShouldShowMediaEngagementViewUseCase shouldShowMediaEngagementViewUseCase;
    private final IsShowOverlayPermissionRequestDialogUseCase isShowOverlayPermissionRequestDialogUseCase;
    private final HasCallNotificationChannelEnabledUseCase hasCallNotificationChannelEnabledUseCase;
    private final IsShowEnableCallNotificationChannelDialogUseCase isShowEnableCallNotificationChannelDialogUseCase;
    private final DialogController dialogController;

    private final QueueTicketsEventsListener queueTicketsEventsListener = new QueueTicketsEventsListener() {
        @Override
        public void onTicketReceived(String ticketId) {
            onQueueTicketReceived(ticketId);
        }

        @Override
        public void started() {
            queueForEngagementStarted();
        }

        @Override
        public void ongoing() {
            queueForEngagementOngoing();
        }

        @Override
        public void stopped() {
            queueForEngagementStopped();
        }

        @Override
        public void error(GliaException exception) {
            queueForEngagementError(exception);
        }
    };

    private final GliaOperatorMediaRepository.OperatorMediaStateListener operatorMediaStateListener = this::onNewOperatorMediaState;

    private final String TAG = "CallController";
    private volatile CallState callState;

    public CallController(
            MediaUpgradeOfferRepository mediaUpgradeOfferRepository,
            TimeCounter callTimer,
            CallViewCallback viewCallback,
            TimeCounter inactivityTimeCounter,
            TimeCounter connectingTimerCounter,
            MinimizeHandler minimizeHandler,
            DialogController dialogController,
            MessagesNotSeenHandler messagesNotSeenHandler,
            ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase,
            ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase,
            RemoveCallNotificationUseCase removeCallNotificationUseCase,
            GliaQueueForMediaEngagementUseCase gliaQueueForMediaEngagementUseCase,
            GliaCancelQueueTicketUseCase cancelQueueTicketUseCase,
            GliaOnEngagementUseCase onEngagementUseCase,
            AddOperatorMediaStateListenerUseCase addOperatorMediaStateListenerUseCase,
            GliaOnVisitorMediaStateUseCase onVisitorMediaStateUseCase,
            GliaOnEngagementEndUseCase onEngagementEndUseCase,
            GliaEndEngagementUseCase endEngagementUseCase,
            ShouldShowMediaEngagementViewUseCase shouldShowMediaEngagementViewUseCase,
            IsShowOverlayPermissionRequestDialogUseCase isShowOverlayPermissionRequestDialogUseCase,
            HasCallNotificationChannelEnabledUseCase hasCallNotificationChannelEnabledUseCase,
            IsShowEnableCallNotificationChannelDialogUseCase isShowEnableCallNotificationChannelDialogUseCase
    ) {
        Logger.d(TAG, "constructor");
        this.viewCallback = viewCallback;
        this.callState = new CallState.Builder()
                .setIntegratorCallStarted(false)
                .setVisible(false)
                .setMessagesNotSeen(0)
                .setCallStatus(new CallStatus.NotOngoing(null))
                .setLandscapeLayoutControlsVisible(false)
                .setIsSpeakerOn(false)
                .setIsMuted(false)
                .setHasVideo(false)
                .createCallState();
        this.dialogController = dialogController;
        this.callTimer = callTimer;
        this.mediaUpgradeOfferRepository = mediaUpgradeOfferRepository;
        this.inactivityTimeCounter = inactivityTimeCounter;
        this.connectingTimerCounter = connectingTimerCounter;
        this.minimizeHandler = minimizeHandler;
        this.messagesNotSeenHandler = messagesNotSeenHandler;

        this.showAudioCallNotificationUseCase = showAudioCallNotificationUseCase;
        this.showVideoCallNotificationUseCase = showVideoCallNotificationUseCase;
        this.removeCallNotificationUseCase = removeCallNotificationUseCase;
        this.gliaQueueForMediaEngagementUseCase = gliaQueueForMediaEngagementUseCase;
        this.cancelQueueTicketUseCase = cancelQueueTicketUseCase;
        this.onEngagementUseCase = onEngagementUseCase;
        this.addOperatorMediaStateListenerUseCase = addOperatorMediaStateListenerUseCase;
        this.onVisitorMediaStateUseCase = onVisitorMediaStateUseCase;
        this.onEngagementEndUseCase = onEngagementEndUseCase;
        this.endEngagementUseCase = endEngagementUseCase;
        this.shouldShowMediaEngagementViewUseCase = shouldShowMediaEngagementViewUseCase;
        this.isShowOverlayPermissionRequestDialogUseCase = isShowOverlayPermissionRequestDialogUseCase;
        this.hasCallNotificationChannelEnabledUseCase = hasCallNotificationChannelEnabledUseCase;
        this.isShowEnableCallNotificationChannelDialogUseCase = isShowEnableCallNotificationChannelDialogUseCase;
    }

    public void initCall(String companyName,
                         String queueId,
                         String contextUrl,
                         Engagement.MediaType mediaType) {
        Logger.d(TAG, "initCall");
        messagesNotSeenHandler.onNavigatedToCall();
        if (callState.integratorCallStarted || dialogController.isShowingChatEnderDialog()) {
            return;
        }
        emitViewState(callState.initCall(companyName, mediaType));
        createNewTimerStatusCallback();
        initControllerCallbacks();
        initMinimizeCallback();
        initMessagesNotSeenCallback();
        onEngagementUseCase.execute(this);
        addOperatorMediaStateListenerUseCase.execute(operatorMediaStateListener);
        gliaQueueForMediaEngagementUseCase.execute(queueId, contextUrl, mediaType, queueTicketsEventsListener);
        onEngagementEndUseCase.execute(this);
        mediaUpgradeOfferRepository.addCallback(mediaUpgradeOfferRepositoryCallback);
        inactivityTimeCounter.addRawValueListener(inactivityTimerStatusListener);
        connectingTimerCounter.addRawValueListener(connectingTimerStatusListener);
        minimizeHandler.addListener(minimizeCalledListener);
        messagesNotSeenHandler.addListener(messagesNotSeenHandlerListener);
    }

    private void initMessagesNotSeenCallback() {
        messagesNotSeenHandlerListener = count ->
                emitViewState(callState.changeNumberOfMessages(count));
    }

    private void initMinimizeCallback() {
        minimizeCalledListener = () -> onDestroy(true);
    }

    public void onDestroy(boolean retain) {
        Logger.d(TAG, "onDestroy, retain: " + retain);
        viewCallback = null;
        if (!retain) {
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
            minimizeCalledListener = null;
            minimizeHandler.clear();
            messagesNotSeenHandler.removeListener(messagesNotSeenHandlerListener);
            messagesNotSeenHandlerListener = null;

            onEngagementUseCase.unregisterListener(this);
            onVisitorMediaStateUseCase.unregisterListener(this);
            onEngagementEndUseCase.unregisterListener(this);
        }
    }

    private void initControllerCallbacks() {
        mediaUpgradeOfferRepositoryCallback = new MediaUpgradeOfferRepositoryCallback() {
            @Override
            public void newOffer(MediaUpgradeOffer offer) {
                if (offer.video == MediaDirection.NONE && offer.audio == MediaDirection.TWO_WAY) {
                    // audio call
                    Logger.d(TAG, "audioUpgradeRequested");
                    showUpgradeAudioDialog(offer);
                } else if (offer.video == MediaDirection.TWO_WAY) {
                    // 2 way video call
                    Logger.d(TAG, "2 way videoUpgradeRequested");
                    showUpgradeVideoDialog2Way(offer);
                } else if (offer.video == MediaDirection.ONE_WAY) {
                    // 1 way video call
                    Logger.d(TAG, "1 way videoUpgradeRequested");
                    showUpgradeVideoDialog1Way(offer);
                }
            }

            @Override
            public void upgradeOfferChoiceSubmitSuccess(
                    MediaUpgradeOffer offer,
                    MediaUpgradeOfferRepository.Submitter submitter
            ) {
                Logger.d(TAG, "upgradeOfferChoiceSubmitSuccess");
                dialogController.dismissDialogs();
            }

            @Override
            public void upgradeOfferChoiceDeclinedSuccess(
                    MediaUpgradeOfferRepository.Submitter submitter
            ) {
                Logger.d(TAG, "upgradeOfferChoiceDeclinedSuccess");
                dialogController.dismissDialogs();
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

    private void stop() {
        Logger.d(TAG, "Stop, engagement ended");
        cancelQueueTicketUseCase.execute();
        endEngagementUseCase.execute();
        mediaUpgradeOfferRepository.stopAll();
        emitViewState(callState.stop());
    }

    public void leaveChatClicked() {
        Logger.d(TAG, "leaveChatClicked");
        showExitChatDialog();
    }

    public void setViewCallback(CallViewCallback callViewCallback) {
        Logger.d(TAG, "setViewCallback");
        this.viewCallback = callViewCallback;
        viewCallback.emitState(callState);
        emitViewState(callState.landscapeControlsVisibleChanged(!callState.isVideoCall()));
    }

    public void endEngagementDialogYesClicked() {
        Logger.d(TAG, "endEngagementDialogYesClicked");
        stop();
        dialogController.dismissDialogs();
    }

    public void endEngagementDialogDismissed() {
        Logger.d(TAG, "endEngagementDialogDismissed");
        dialogController.dismissDialogs();
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
        dialogController.dismissDialogs();
    }

    private void showExitChatDialog() {
        if (callState.isMediaEngagementStarted()) {
            dialogController.showExitChatDialog(callState.callStatus.getFormattedOperatorName());
        }
    }

    public void leaveChatQueueClicked() {
        Logger.d(TAG, "leaveChatQueueClicked");
        dialogController.showExitQueueDialog();
    }

    public void onBackArrowClicked(boolean isChatInBackstack) {
        Logger.d(TAG, "onBackArrowClicked");
        messagesNotSeenHandler.callOnBackClicked(isChatInBackstack);
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

    public void onResume() {
        Logger.d(TAG, "onResume\n");

        if (isShowOverlayPermissionRequestDialogUseCase.execute()) {
            dialogController.showOverlayPermissionsDialog();
        }

        if (hasCallNotificationChannelEnabledUseCase.execute()) {
            if (callState.isVideoCall() || callState.is2WayVideoCall()) {
                showVideoCallNotificationUseCase.execute();
            } else if (callState.isAudioCall()) {
                showAudioCallNotificationUseCase.execute();
            }
        }
    }

    public void chatButtonClicked() {
        Logger.d(TAG, "chatButtonClicked");
        if (viewCallback != null) {
            viewCallback.navigateToChat();
        }
        onDestroy(true);
        messagesNotSeenHandler.callChatButtonClicked();
    }

    private void createNewTimerStatusCallback() {
        if (callTimerStatusListener == null) {
            callTimerStatusListener = new TimeCounter.FormattedTimerStatusListener() {
                @Override
                public void onNewFormattedTimerValue(String formatedValue) {
                    if (callState.isMediaEngagementStarted()) {
                        emitViewState(callState.newStartedCallTimerValue(formatedValue));
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

    public void acceptUpgradeOfferClicked(MediaUpgradeOffer mediaUpgradeOffer) {
        Logger.d(TAG, "upgradeToAudioClicked");
        mediaUpgradeOfferRepository.acceptOffer(
                mediaUpgradeOffer,
                MediaUpgradeOfferRepository.Submitter.CALL
        );
        dialogController.dismissDialogs();
    }

    public void declineUpgradeOfferClicked(MediaUpgradeOffer mediaUpgradeOffer) {
        Logger.d(TAG, "closeUpgradeDialogClicked");
        mediaUpgradeOfferRepository.declineOffer(
                mediaUpgradeOffer,
                MediaUpgradeOfferRepository.Submitter.CALL
        );
        dialogController.dismissDialogs();
    }

    public void onUserInteraction() {
        emitViewState(callState.landscapeControlsVisibleChanged(true));
        Logger.d(TAG, "onUserInteraction, restartingInactivityTimer");
        restartInactivityTimeCounter();
    }

    public void minimizeButtonClicked() {
        Logger.d(TAG, "minimizeButtonClicked");
        minimizeHandler.minimize();
    }

    public void muteButtonClicked() {
        Logger.d(TAG, "muteButtonClicked");
        VisitorMediaState currentMediaState = callState.callStatus.getVisitorMediaState();
        if (currentMediaState != null && currentMediaState.getAudio() != null) {
            Logger.d(TAG, "muteButton status:" + currentMediaState.getAudio().getStatus().toString());
            if (currentMediaState.getAudio().getStatus() == Media.Status.PAUSED) {
                currentMediaState.getAudio().unmute();
                if (currentMediaState.getAudio().getStatus() == Media.Status.PLAYING) {
                    emitViewState(callState.muteStatusChanged(false));
                }
            } else if (currentMediaState.getAudio().getStatus() == Media.Status.PLAYING) {
                currentMediaState.getAudio().mute();
                if (currentMediaState.getAudio().getStatus() == Media.Status.PAUSED) {
                    emitViewState(callState.muteStatusChanged(true));
                }
            }
        }
    }

    public void videoButtonClicked() {
        Logger.d(TAG, "videoButtonClicked");
        if (callState.isCallOngoingAndOperatorConnected()) {
            VisitorMediaState currentMediaState = callState.callStatus.getVisitorMediaState();
            Logger.d(TAG, "videoButton status:" + currentMediaState.getAudio().getStatus().toString());
            if (currentMediaState.getVideo().getStatus() == Media.Status.PAUSED) {
                currentMediaState.getVideo().resume();
                if (currentMediaState.getVideo().getStatus() == Media.Status.PLAYING) {
                    emitViewState(callState.hasVideoChanged(true));
                }
            } else if (currentMediaState.getVideo().getStatus() == Media.Status.PLAYING) {
                currentMediaState.getVideo().pause();
                if (currentMediaState.getVideo().getStatus() == Media.Status.PAUSED) {
                    emitViewState(callState.hasVideoChanged(false));
                }
            }
        }
    }

    private void restartInactivityTimeCounter() {
        inactivityTimeCounter.startNew(INACTIVITY_TIMER_DELAY_VALUE, INACTIVITY_TIMER_TICKER_VALUE);
    }

    public void notificationsDialogDismissed() {
        dialogController.dismissDialogs();
    }

    public void onNewOperatorMediaState(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: " + operatorMediaState.toString() +
                ", timertaskrunning: " + callTimer.isRunning());
        if (operatorMediaState.getVideo() != null) {
            if (isShowEnableCallNotificationChannelDialogUseCase.execute()) {
                dialogController.showEnableCallNotificationChannelDialog();
            }

            onOperatorMediaStateVideo(operatorMediaState);
            onVisitorMediaStateUseCase.execute(this);
        } else if (operatorMediaState.getAudio() != null) {
            if (isShowEnableCallNotificationChannelDialogUseCase.execute()) {
                dialogController.showEnableCallNotificationChannelDialog();
            }

            onOperatorMediaStateAudio(operatorMediaState);
            onVisitorMediaStateUseCase.execute(this);
        } else {
            onOperatorMediaStateUnknown();
        }
        if (callState.isMediaEngagementStarted() &&
                !callTimer.isRunning() &&
                callTimerStatusListener != null
        ) {
            callTimer.startNew(Constants.CALL_TIMER_DELAY, Constants.CALL_TIMER_INTERVAL_VALUE);
        }
    }

    @Override
    public void onNewVisitorMediaState(VisitorMediaState visitorMediaState) {
        Logger.d(TAG, "newVisitorMediaState: " + visitorMediaState.toString());
        emitViewState(callState.visitorMediaStateChanged(visitorMediaState));
        Logger.d(TAG, "newVisitorMediaState- is2WayVideo:" + callState.is2WayVideoCall());
    }

    @Override
    public void engagementEnded() {
        Logger.d(TAG, "engagementEndedByOperator");
        stop();
        dialogController.showNoMoreOperatorsAvailableDialog();
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        Logger.d(TAG, "engagementSuccess");
        mediaUpgradeOfferRepository.startListening();
        String operatorProfileImgUrl = null;
        try {
            operatorProfileImgUrl = engagement.getOperator().getPicture().getURL().get();
        } catch (Exception e) {
        }
        emitViewState(
                callState.engagementStarted(
                        engagement.getOperator().getName(),
                        operatorProfileImgUrl
                )
        );
        if (!connectingTimerCounter.isRunning()) {
            connectingTimerCounter.startNew(
                    Constants.CALL_TIMER_DELAY,
                    Constants.CALL_TIMER_INTERVAL_VALUE
            );
        }
    }

    public void onSpeakerButtonPressed() {
        boolean newValue = !callState.isSpeakerOn;
        Logger.d(TAG, "onSpeakerButtonPressed, new value: " + newValue);
        emitViewState(callState.speakerValueChanged(newValue));
    }

    private void onOperatorMediaStateVideo(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: video");
        String formatedTime = Utils.toMmSs(0L);
        if (callState.isCallOngoingAndOperatorConnected()) {
            formatedTime = callState.callStatus.getTime();
        }
        emitViewState(callState.videoCallOperatorVideoStarted(
                operatorMediaState,
                formatedTime
        ));
        showVideoCallNotificationUseCase.execute();
        connectingTimerCounter.stop();
    }

    private void onOperatorMediaStateAudio(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: audio");
        String formatedTime = Utils.toMmSs(0L);
        if (callState.isCallOngoingAndOperatorConnected()) formatedTime = callState.callStatus.getTime();

        emitViewState(callState.audioCallStarted(
                operatorMediaState,
                formatedTime
        ));

        showAudioCallNotificationUseCase.execute();
        connectingTimerCounter.stop();
    }

    private void onOperatorMediaStateUnknown() {
        Logger.d(TAG, "newOperatorMediaState: null");
        if (callState.isMediaEngagementStarted()) {
            emitViewState(callState.backToOngoing());
        }
        removeCallNotificationUseCase.execute();
        if (!connectingTimerCounter.isRunning()) {
            connectingTimerCounter.startNew(
                    Constants.CALL_TIMER_DELAY,
                    Constants.CALL_TIMER_INTERVAL_VALUE
            );
        }
    }

    public void onQueueTicketReceived(String ticket) {
        Logger.d(TAG, "ticketLoaded");
        emitViewState(callState.ticketLoaded(ticket));
    }

    public void queueForEngagementStarted() {
        Logger.d(TAG, "queueForEngagementStarted");
    }

    public void queueForEngagementStopped() {
        Logger.d(TAG, "queueForEngagementStopped");
    }

    public void queueForEngagementError(GliaException exception) {
        if (exception != null) {
            Logger.e(TAG, exception.debugMessage);
            dialogController.showUnexpectedErrorDialog();
            emitViewState(callState.changeVisibility(false));
        }
    }

    public void queueForEngagementOngoing() {
        Logger.d(TAG, "queueForEngagementOngoing");
    }

    public boolean shouldShowMediaEngagementView() {
        return shouldShowMediaEngagementViewUseCase.execute();
    }
}
