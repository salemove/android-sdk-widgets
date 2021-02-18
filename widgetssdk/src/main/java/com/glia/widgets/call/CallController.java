package com.glia.widgets.call;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.comms.MediaDirection;
import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.DialogsState;
import com.glia.widgets.model.GliaCallRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepositoryCallback;
import com.glia.widgets.model.MinimizeHandler;
import com.glia.widgets.view.DialogOfferType;

public class CallController {

    private CallViewCallback viewCallback;
    private CallGliaCallback gliaCallback;
    private MediaUpgradeOfferRepositoryCallback mediaUpgradeOfferRepositoryCallback;
    private TimeCounter.FormattedTimerStatusListener callTimerStatusListener;
    private TimeCounter.RawTimerStatusListener inactivityTimerStatusListener;
    private MinimizeHandler.OnMinimizeCalledListener minimizeCalledListener;
    private final GliaCallRepository repository;
    private final MediaUpgradeOfferRepository mediaUpgradeOfferRepository;
    private final TimeCounter callTimer;
    private final TimeCounter inactivityTimeCounter;
    private final MinimizeHandler minimizeHandler;
    private final static int MAX_IDLE_TIME = 3200;
    private final static int INACTIVITY_TIMER_TICKER_VALUE = 400;
    private final static int INACTIVITY_TIMER_DELAY_VALUE = 0;

    private final String TAG = "CallController";
    private volatile CallState callState;
    private volatile DialogsState dialogsState;

    public CallController(
            GliaCallRepository callRepository,
            MediaUpgradeOfferRepository mediaUpgradeOfferRepository,
            TimeCounter callTimer,
            CallViewCallback viewCallback,
            TimeCounter inactivityTimeCounter,
            MinimizeHandler minimizeHandler
    ) {
        Logger.d(TAG, "constructor");
        this.viewCallback = viewCallback;
        this.callState = new CallState.Builder()
                .setIntegratorCallStarted(false)
                .setVisible(false)
                .setHasOverlayPermissions(false)
                .setMessagesNotSeen(0)
                .setCallStatus(new CallStatus.NotOngoing())
                .setLandscapeLayoutControlsVisible(false)
                .createCallState();
        this.dialogsState = new DialogsState.NoDialog();
        this.repository = callRepository;
        this.callTimer = callTimer;
        this.mediaUpgradeOfferRepository = mediaUpgradeOfferRepository;
        this.inactivityTimeCounter = inactivityTimeCounter;
        this.minimizeHandler = minimizeHandler;
    }

    public void initCall() {
        Logger.d(TAG, "initCall");
        if (callState.integratorCallStarted || dialogsState.showingChatEnderDialog()) {
            return;
        }
        emitViewState(callState.initCall());
        initControllerCallbacks();
        initMinimizeCallback();
        repository.init(gliaCallback);
        mediaUpgradeOfferRepository.addCallback(mediaUpgradeOfferRepositoryCallback);
        inactivityTimeCounter.addRawValueListener(inactivityTimerStatusListener);
        minimizeHandler.addListener(minimizeCalledListener);
    }

    private void initMinimizeCallback() {
        minimizeCalledListener = () -> {
            handleFloatingChatheads(
                    callState.callStatus.getOperatorProfileImageUrl(),
                    GliaWidgets.CALL_ACTIVITY
            );
            onDestroy(true);
        };
    }

    public void onDestroy(boolean retain) {
        Logger.d(TAG, "onDestroy, retain: " + retain);
        viewCallback = null;
        if (!retain) {
            repository.onDestroy();
            gliaCallback = null;
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
        }
    }

    private void initControllerCallbacks() {
        gliaCallback = new CallGliaCallback() {
            @Override
            public void error(GliaException exception) {
                Logger.e(TAG, exception.toString());
                showUnexpectedErrorDialog();
                emitViewState(callState.stop());
            }

            @Override
            public void onMessage(ChatMessage message) {
                Logger.d(TAG, "onMessage: " + message.getContent());
                emitViewState(callState.changeNumberOfMessages(callState.messagesNotSeen + 1));
            }

            @Override
            public void engagementSuccess(OmnicoreEngagement engagement) {
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
                createNewTimerStatusCallback();
                callTimer.addFormattedValueListener(callTimerStatusListener);
            }

            @Override
            public void engagementEndedByOperator() {
                Logger.d(TAG, "engagementEndedByOperator");
                stop();
                if (!isDialogShowing()) {
                    showNoMoreOperatorsAvailableDialog();
                }
            }

            @Override
            public void newOperatorMediaState(OperatorMediaState operatorMediaState) {
                Logger.d(TAG, "newOperatorMediaState: " + operatorMediaState.toString());
                if (operatorMediaState.getVideo() != null) {
                    Logger.d(TAG, "newOperatorMediaState: video");
                    if (callState.isCallOngoing()) {
                        emitViewState(callState.videoCallOperatorVideoStarted(operatorMediaState));
                    }
                    startOperatorVideo(operatorMediaState);
                } else if (operatorMediaState.getAudio() != null) {
                    Logger.d(TAG, "newOperatorMediaState: audio");
                    if (callState.isCallOngoing()) {
                        emitViewState(callState.audioCallStarted(operatorMediaState));
                    }
                }
            }

            @Override
            public void newVisitorMediaState(VisitorMediaState visitorMediaState) {
                if (visitorMediaState.getVideo() != null) {
                    Logger.d(TAG, "newVisitorMediaState: video");
                    if (callState.isCallOngoing()) {
                        emitViewState(callState.videoCallVisitorVideoStarted(visitorMediaState));
                    }
                    startVisitorVideo(visitorMediaState);
                }
            }
        };
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
                if (dialogsState instanceof DialogsState.UpgradeDialog) {
                    dismissDialogs();
                }
            }

            @Override
            public void upgradeOfferChoiceDeclinedSuccess(
                    MediaUpgradeOfferRepository.Submitter submitter
            ) {
                Logger.d(TAG, "upgradeOfferChoiceDeclinedSuccess");
                if (dialogsState instanceof DialogsState.UpgradeDialog) {
                    dismissDialogs();
                }
            }
        };

        inactivityTimerStatusListener = new TimeCounter.RawTimerStatusListener() {
            @Override
            public void onNewTimerValue(int timerValue) {
                if (callState.isVideoCall()) {
                    Logger.d(TAG, "inactivityTimer onNewTimerValue: " + timerValue);
                    emitViewState(callState.landscapeControlsVisibleChanged(timerValue < MAX_IDLE_TIME));
                }
                if (timerValue >= MAX_IDLE_TIME) {
                    inactivityTimeCounter.stop();
                }
            }

            @Override
            public void onCancel() {

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

    private synchronized void emitDialogState(DialogsState state) {
        if (setDialogState(state) && viewCallback != null) {
            Logger.d(TAG, "Emit dialog state:\n" + dialogsState.toString());
            viewCallback.emitDialog(dialogsState);
        }
    }

    private synchronized boolean setDialogState(DialogsState dialogsState) {
        if (this.dialogsState.equals(dialogsState)) return false;
        this.dialogsState = dialogsState;
        return true;
    }

    private void stop() {
        Logger.d(TAG, "Stop, engagement ended");
        repository.stop();
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
        viewCallback.emitDialog(dialogsState);
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
        dismissDialogs();
    }

    public void endEngagementDialogDismissed() {
        Logger.d(TAG, "endEngagementDialogDismissed");
        dismissDialogs();
    }

    public void noMoreOperatorsAvailableDismissed() {
        Logger.d(TAG, "noMoreOperatorsAvailableDismissed");
        stop();
        dismissDialogs();
    }

    public void unexpectedErrorDialogDismissed() {
        Logger.d(TAG, "unexpectedErrorDialogDismissed");
        stop();
        dismissDialogs();
    }

    public void overlayPermissionsDialogDismissed() {
        Logger.d(TAG, "overlayPermissionsDialogDismissed");
        emitDialogState(new DialogsState.NoDialog());
    }

    private void dismissDialogs() {
        Logger.d(TAG, "Dismiss dialogs");
        emitDialogState(new DialogsState.NoDialog());
    }

    private void showExitChatDialog() {
        if (!isDialogShowing() && callState.isCallOngoing()) {
            emitDialogState(new DialogsState.EndEngagementDialog(
                    callState.callStatus.getFormattedOperatorName()));
        }
    }

    private boolean isDialogShowing() {
        return !(dialogsState instanceof DialogsState.NoDialog);
    }

    public void leaveChatQueueClicked() {
        Logger.d(TAG, "leaveChatQueueClicked");
        showExitQueueDialog();
    }

    public void onBackArrowClicked() {
        Logger.d(TAG, "onBackArrowClicked");
        if (!callState.hasOverlayPermissions) {
            emitViewState(callState.changeVisibility(false));
        } else if (callState.hasOverlayPermissions) {
            handleFloatingChatheads(
                    callState.callStatus.getOperatorProfileImageUrl(),
                    GliaWidgets.CALL_ACTIVITY
            );
        }
    }

    /**
     * Method for either showing or hiding the floating chat head.
     *
     * @param returnDestination 1 of either {@link com.glia.widgets.GliaWidgets#CALL_ACTIVITY}
     *                          or {@link com.glia.widgets.GliaWidgets#CHAT_ACTIVITY}
     *                          hides chat head if anything else.
     */
    private void handleFloatingChatheads(String operatorProfileImgUrl, String returnDestination) {
        if (viewCallback != null && callState.hasOverlayPermissions) {
            Logger.d(TAG, "handleFloatingChatHeads, returnDestination: " + returnDestination);
            viewCallback.handleFloatingChatHead(operatorProfileImgUrl, returnDestination);
        }
    }

    private void showExitQueueDialog() {
        if (!isDialogShowing()) {
            emitDialogState(new DialogsState.ExitQueueDialog());
        }
    }

    private void showUpgradeAudioDialog(MediaUpgradeOffer mediaUpgradeOffer) {
        if (!isDialogShowing() && callState.isCallOngoing()) {
            emitDialogState(new DialogsState.UpgradeDialog(
                    new DialogOfferType.AudioUpgradeOffer(
                            mediaUpgradeOffer,
                            callState.callStatus.getFormattedOperatorName()
                    )));
        }
    }

    private void showUpgradeVideoDialog2Way(MediaUpgradeOffer mediaUpgradeOffer) {
        if (!isDialogShowing() && callState.isCallOngoing()) {
            emitDialogState(new DialogsState.UpgradeDialog(
                    new DialogOfferType.VideoUpgradeOffer2Way(
                            mediaUpgradeOffer,
                            callState.callStatus.getFormattedOperatorName()
                    )));
        }
    }

    private void showUpgradeVideoDialog1Way(MediaUpgradeOffer mediaUpgradeOffer) {
        if (!isDialogShowing() && callState.isCallOngoing()) {
            emitDialogState(new DialogsState.UpgradeDialog(
                    new DialogOfferType.VideoUpgradeOffer1Way(
                            mediaUpgradeOffer,
                            callState.callStatus.getFormattedOperatorName()
                    )));
        }
    }

    private void showUnexpectedErrorDialog() {
        if (!isDialogShowing()) {
            emitDialogState(new DialogsState.UnexpectedErrorDialog());
        }
    }

    public void onResume(boolean canDrawOverlays) {
        Logger.d(TAG, "onResume, canDrawOverlays: " + canDrawOverlays);
        emitViewState(callState.drawOverlaysPermissionChanged(canDrawOverlays));
        if (callState.hasOverlayPermissions) {
            handleFloatingChatheads(
                    callState.callStatus.getOperatorProfileImageUrl(),
                    null
            );
        }
    }

    public void chatButtonClicked() {
        Logger.d(TAG, "chatButtonClicked");
        handleFloatingChatheads(
                callState.callStatus.getOperatorProfileImageUrl(),
                GliaWidgets.CALL_ACTIVITY
        );
        viewCallback.navigateToChat();
        emitViewState(callState.changeNumberOfMessages(0));
        onDestroy(true);
    }

    private void createNewTimerStatusCallback() {
        if (callTimerStatusListener != null) {
            callTimer.removeFormattedValueListener(callTimerStatusListener);
        }
        callTimerStatusListener = new TimeCounter.FormattedTimerStatusListener() {
            @Override
            public void onNewTimerValue(String formatedValue) {
                if (callState.isCallOngoing()) {
                    emitViewState(callState.newTimerValue(formatedValue));
                }
            }

            @Override
            public void onCancel() {
                // Should only happen if engagement ends.
            }
        };
    }

    public void acceptUpgradeOfferClicked(MediaUpgradeOffer mediaUpgradeOffer) {
        Logger.d(TAG, "upgradeToAudioClicked");
        mediaUpgradeOfferRepository.acceptOffer(
                mediaUpgradeOffer,
                MediaUpgradeOfferRepository.Submitter.CALL
        );
        emitDialogState(new DialogsState.NoDialog());
    }

    public void declineUpgradeOfferClicked(MediaUpgradeOffer mediaUpgradeOffer) {
        Logger.d(TAG, "closeUpgradeDialogClicked");
        mediaUpgradeOfferRepository.declineOffer(
                mediaUpgradeOffer,
                MediaUpgradeOfferRepository.Submitter.CALL
        );
        emitDialogState(new DialogsState.NoDialog());
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

    private void showNoMoreOperatorsAvailableDialog() {
        if (!isDialogShowing()) {
            emitDialogState(new DialogsState.NoMoreOperatorsDialog());
        }
    }
}
