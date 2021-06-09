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
import com.glia.widgets.dialog.DialogController;
import com.glia.widgets.glia.GliaCancelQueueTicketUseCase;
import com.glia.widgets.glia.GliaEndEngagementUseCase;
import com.glia.widgets.glia.GliaOnEngagementEndUseCase;
import com.glia.widgets.glia.GliaOnEngagementUseCase;
import com.glia.widgets.glia.GliaOnOperatorMediaStateUseCase;
import com.glia.widgets.glia.GliaOnQueueTicketUseCase;
import com.glia.widgets.glia.GliaOnVisitorMediaStateUseCase;
import com.glia.widgets.glia.GliaQueueForMediaEngagementUseCase;
import com.glia.widgets.head.ChatHeadsController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.MediaUpgradeOfferRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepositoryCallback;
import com.glia.widgets.model.MessagesNotSeenHandler;
import com.glia.widgets.model.MinimizeHandler;
import com.glia.widgets.model.PermissionType;
import com.glia.widgets.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.notification.domain.ShowAudioCallNotificationUseCase;
import com.glia.widgets.notification.domain.ShowVideoCallNotificationUseCase;
import com.glia.widgets.permissions.CheckIfShowPermissionsDialogUseCase;
import com.glia.widgets.permissions.ResetPermissionsUseCase;
import com.glia.widgets.permissions.UpdateDialogShownUseCase;
import com.glia.widgets.permissions.UpdatePermissionsUseCase;

public class CallController implements
        GliaQueueForMediaEngagementUseCase.Listener,
        GliaOnQueueTicketUseCase.Listener,
        GliaOnEngagementUseCase.Listener,
        GliaOnOperatorMediaStateUseCase.Listener,
        GliaOnVisitorMediaStateUseCase.Listener,
        GliaOnEngagementEndUseCase.Listener {

    private CallViewCallback viewCallback;
    private MediaUpgradeOfferRepositoryCallback mediaUpgradeOfferRepositoryCallback;
    private TimeCounter.FormattedTimerStatusListener callTimerStatusListener;
    private TimeCounter.RawTimerStatusListener inactivityTimerStatusListener;
    private MinimizeHandler.OnMinimizeCalledListener minimizeCalledListener;
    private MessagesNotSeenHandler.MessagesNotSeenHandlerListener messagesNotSeenHandlerListener;
    private final MediaUpgradeOfferRepository mediaUpgradeOfferRepository;
    private final TimeCounter callTimer;
    private final TimeCounter inactivityTimeCounter;
    private final MinimizeHandler minimizeHandler;
    private final ChatHeadsController chatHeadsController;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final static int MAX_IDLE_TIME = 3200;
    private final static int INACTIVITY_TIMER_TICKER_VALUE = 400;
    private final static int INACTIVITY_TIMER_DELAY_VALUE = 0;

    private final ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase;
    private final ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase;
    private final RemoveCallNotificationUseCase removeCallNotificationUseCase;
    private final CheckIfShowPermissionsDialogUseCase checkIfShowPermissionsDialogUseCase;
    private final UpdateDialogShownUseCase updateDialogShownUseCase;
    private final UpdatePermissionsUseCase updatePermissionsUseCase;
    private final ResetPermissionsUseCase resetPermissionsUseCase;
    private final GliaQueueForMediaEngagementUseCase queueForMediaTicketUseCase;
    private final GliaCancelQueueTicketUseCase cancelQueueTicketUseCase;
    private final GliaOnQueueTicketUseCase onQueueTicketUseCase;
    private final GliaOnEngagementUseCase onEngagementUseCase;
    private final GliaOnOperatorMediaStateUseCase onOperatorMediaStateUseCase;
    private final GliaOnVisitorMediaStateUseCase onVisitorMediaStateUseCase;
    private final GliaOnEngagementEndUseCase onEngagementEndUseCase;
    private final GliaEndEngagementUseCase endEngagementUseCase;
    private final DialogController dialogController;

    private final String TAG = "CallController";
    private volatile CallState callState;

    public CallController(
            MediaUpgradeOfferRepository mediaUpgradeOfferRepository,
            TimeCounter callTimer,
            CallViewCallback viewCallback,
            TimeCounter inactivityTimeCounter,
            MinimizeHandler minimizeHandler,
            ChatHeadsController chatHeadsController,
            DialogController dialogController,
            MessagesNotSeenHandler messagesNotSeenHandler,
            ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase,
            ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase,
            RemoveCallNotificationUseCase removeCallNotificationUseCase,
            CheckIfShowPermissionsDialogUseCase checkIfShowPermissionsDialogUseCase,
            UpdateDialogShownUseCase updateDialogShownUseCase,
            UpdatePermissionsUseCase updatePermissionsUseCase,
            ResetPermissionsUseCase resetPermissionsUseCase,
            GliaQueueForMediaEngagementUseCase queueForMediaTicketUseCase,
            GliaCancelQueueTicketUseCase cancelQueueTicketUseCase,
            GliaOnQueueTicketUseCase onQueueTicketUseCase,
            GliaOnEngagementUseCase onEngagementUseCase,
            GliaOnOperatorMediaStateUseCase onOperatorMediaStateUseCase,
            GliaOnVisitorMediaStateUseCase onVisitorMediaStateUseCase,
            GliaOnEngagementEndUseCase onEngagementEndUseCase,
            GliaEndEngagementUseCase endEngagementUseCase) {
        Logger.d(TAG, "constructor");
        this.viewCallback = viewCallback;
        this.callState = new CallState.Builder()
                .setIntegratorCallStarted(false)
                .setVisible(false)
                .setMessagesNotSeen(0)
                .setCallStatus(new CallStatus.NotOngoing())
                .setLandscapeLayoutControlsVisible(false)
                .setIsMuted(false)
                .setHasVideo(false)
                .createCallState();
        this.dialogController = dialogController;
        this.callTimer = callTimer;
        this.mediaUpgradeOfferRepository = mediaUpgradeOfferRepository;
        this.inactivityTimeCounter = inactivityTimeCounter;
        this.minimizeHandler = minimizeHandler;
        this.chatHeadsController = chatHeadsController;
        this.messagesNotSeenHandler = messagesNotSeenHandler;

        this.showAudioCallNotificationUseCase = showAudioCallNotificationUseCase;
        this.showVideoCallNotificationUseCase = showVideoCallNotificationUseCase;
        this.removeCallNotificationUseCase = removeCallNotificationUseCase;
        this.checkIfShowPermissionsDialogUseCase = checkIfShowPermissionsDialogUseCase;
        this.updateDialogShownUseCase = updateDialogShownUseCase;
        this.updatePermissionsUseCase = updatePermissionsUseCase;
        this.resetPermissionsUseCase = resetPermissionsUseCase;
        this.queueForMediaTicketUseCase = queueForMediaTicketUseCase;
        this.cancelQueueTicketUseCase = cancelQueueTicketUseCase;
        this.onQueueTicketUseCase = onQueueTicketUseCase;
        this.onEngagementUseCase = onEngagementUseCase;
        this.onOperatorMediaStateUseCase = onOperatorMediaStateUseCase;
        this.onVisitorMediaStateUseCase = onVisitorMediaStateUseCase;
        this.onEngagementEndUseCase = onEngagementEndUseCase;
        this.endEngagementUseCase = endEngagementUseCase;
    }

    public void initCall(String companyName,
                         String queueId,
                         String contextUrl,
                         boolean enableChatHeads,
                         boolean useOverlays,
                         Engagement.MediaType mediaType) {
        Logger.d(TAG, "initCall");
        messagesNotSeenHandler.onNavigatedToCall();
        chatHeadsController.init(enableChatHeads, useOverlays);
        chatHeadsController.onNavigatedToCall();
        if (callState.integratorCallStarted || dialogController.isShowingChatEnderDialog()) {
            return;
        }
        emitViewState(callState.initCall(companyName, mediaType));
        createNewTimerStatusCallback();
        initControllerCallbacks();
        initMinimizeCallback();
        initMessagesNotSeenCallback();
        onQueueTicketUseCase.execute(this);
        onEngagementUseCase.execute(this);
        onOperatorMediaStateUseCase.execute(this);
        onVisitorMediaStateUseCase.execute(this);
        queueForMediaTicketUseCase.execute(queueId, contextUrl, mediaType, this);
        onEngagementEndUseCase.execute(this);
        mediaUpgradeOfferRepository.addCallback(mediaUpgradeOfferRepositoryCallback);
        inactivityTimeCounter.addRawValueListener(inactivityTimerStatusListener);
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
            inactivityTimeCounter.clear();
            inactivityTimerStatusListener = null;
            minimizeCalledListener = null;
            minimizeHandler.clear();
            messagesNotSeenHandler.removeListener(messagesNotSeenHandlerListener);
            messagesNotSeenHandlerListener = null;
            resetPermissionsUseCase.execute();

            onQueueTicketUseCase.unregisterListener(this);
            onEngagementUseCase.unregisterListener(this);
            onOperatorMediaStateUseCase.unregisterListener(this);
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
                    // video call
                    Logger.d(TAG, "2 way videoUpgradeRequested");
                    showUpgradeVideoDialog2Way(offer);
                } else if (offer.video == MediaDirection.ONE_WAY) {
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
    }

    private void onOperatorMediaStateVideo(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: video");
        if (callState.isMediaEngagementStarted()) {
            emitViewState(callState.videoCallOperatorVideoStarted(operatorMediaState));
        }
        startOperatorVideo(operatorMediaState);
        showVideoCallNotificationUseCase.execute();
    }

    private void onOperatorMediaStateAudio(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: audio");
        if (callState.isMediaEngagementStarted()) {
            emitViewState(callState.audioCallStarted(operatorMediaState));
        }
        showAudioCallNotificationUseCase.execute();
    }

    private void onOperatorMediaStateUnknown() {
        Logger.d(TAG, "newOperatorMediaState: null");
        if (callState.isMediaEngagementStarted()) {
            emitViewState(callState.backToOngoing());
        }
        removeCallNotificationUseCase.execute();
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
        if (callState.queueTicketId != null) {
            cancelQueueTicketUseCase.execute(callState.queueTicketId);
        }
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

        if (callState.isVideoCall()) {
            startOperatorVideo(callState.callStatus.getOperatorMediaState());
            if (callState.is2WayVideoCall()) {
                if (viewCallback != null) {
                    startVisitorVideo(
                            ((CallStatus.StartedVideoCall) callState.callStatus).getVisitorMediaState());
                }
            }
        }
        emitViewState(callState.landscapeControlsVisibleChanged(!callState.isVideoCall()));
    }

    public void endEngagementDialogYesClicked() {
        Logger.d(TAG, "endEngagementDialogYesClicked");
        stop();
        chatHeadsController.chatEndedByUser();
        dialogController.dismissDialogs();
        removeCallNotificationUseCase.execute();
    }

    public void endEngagementDialogDismissed() {
        Logger.d(TAG, "endEngagementDialogDismissed");
        dialogController.dismissDialogs();
    }

    public void noMoreOperatorsAvailableDismissed() {
        Logger.d(TAG, "noMoreOperatorsAvailableDismissed");
        stop();
        dialogController.dismissDialogs();
        chatHeadsController.chatEndedByUser();
    }

    public void unexpectedErrorDialogDismissed() {
        Logger.d(TAG, "unexpectedErrorDialogDismissed");
        stop();
        dialogController.dismissDialogs();
        chatHeadsController.chatEndedByUser();
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
        chatHeadsController.onBackButtonPressed(
                Constants.CALL_ACTIVITY,
                isChatInBackstack
        );
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

    public void onResume(boolean hasOverlaysPermission,
                         boolean isCallChannelEnabled,
                         boolean isScreenSharingChannelEnabled) {
        Logger.d(TAG, "onResume\n" +
                "hasOverlayPermissions: " + hasOverlaysPermission +
                ", isCallChannelEnabled:" + isCallChannelEnabled +
                ", isScreenSharingChannelEnabled: " + isScreenSharingChannelEnabled);
        updatePermissionsUseCase.execute(
                hasOverlaysPermission,
                isCallChannelEnabled,
                isScreenSharingChannelEnabled
        );
        if (isCallChannelEnabled) {
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
        chatHeadsController.onChatButtonClicked();
    }

    private void createNewTimerStatusCallback() {
        if (callTimerStatusListener == null) {
            callTimerStatusListener = new TimeCounter.FormattedTimerStatusListener() {
                @Override
                public void onNewFormattedTimerValue(String formatedValue) {
                    if (callState.isMediaEngagementStarted()) {
                        emitViewState(callState.newTimerValue(formatedValue));
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
        chatHeadsController.onMinimizeButtonClicked();
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
        if (callState.hasMedia()) {
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

    private void startOperatorVideo(OperatorMediaState operatorMediaState) {
        if (viewCallback != null) {
            Logger.d(TAG, "startOperatorVideo");
            viewCallback.startOperatorVideoView(operatorMediaState);
        }
    }

    private void startVisitorVideo(VisitorMediaState visitorMediaState) {
        if (viewCallback != null) {
            Logger.d(TAG, "startVisitorVideo");
            viewCallback.startVisitorVideoView(visitorMediaState);
        }
    }

    public void notificationsDialogDismissed() {
        dialogController.dismissDialogs();
    }

    @Override
    public void onNewOperatorMediaState(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: " + operatorMediaState.toString() +
                ", timertaskrunning: " + callTimer.isRunning());
        if (operatorMediaState.getVideo() != null) {
            if (checkIfShowPermissionsDialogUseCase
                    .execute(PermissionType.CALL_CHANNEL, true) &&
                    dialogController.isNoDialogShown()) {
                dialogController.showEnableNotificationChannelDialog();
                updateDialogShownUseCase.execute(PermissionType.CALL_CHANNEL);
            }
            onOperatorMediaStateVideo(operatorMediaState);
        } else if (operatorMediaState.getAudio() != null) {
            if (checkIfShowPermissionsDialogUseCase
                    .execute(PermissionType.CALL_CHANNEL, true) &&
                    dialogController.isNoDialogShown()) {
                dialogController.showEnableNotificationChannelDialog();
                updateDialogShownUseCase.execute(PermissionType.CALL_CHANNEL);
            }
            onOperatorMediaStateAudio(operatorMediaState);
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
        if (visitorMediaState.getVideo() != null) {
            Logger.d(TAG, "newVisitorMediaState: video");
            startVisitorVideo(visitorMediaState);
        }
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
        String operatorProfileImgUrl = null;
        try {
            operatorProfileImgUrl = engagement.getOperator().getPicture().getURL().get();
        } catch (Exception e) {
        }
        emitViewState(callState.engagementStarted(
                engagement.getOperator().getName(),
                operatorProfileImgUrl,
                Utils.toMmSs(0))
        );
    }

    @Override
    public void ticketLoaded(String ticket) {
        Logger.d(TAG, "ticketLoaded");
        emitViewState(callState.ticketLoaded(ticket));
    }

    @Override
    public void queueForEngagementSuccess() {
        Logger.d(TAG, "queueForEngagementSuccess");
    }

    @Override
    public void error(GliaException exception) {
        if (exception != null) {
            Logger.e(TAG, exception.debugMessage);
            dialogController.showUnexpectedErrorDialog();
            emitViewState(callState.changeVisibility(false));
        }
    }
}
