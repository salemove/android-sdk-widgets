package com.glia.widgets.call;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.helper.CallTimer;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.DialogsState;
import com.glia.widgets.model.GliaCallRepository;

public class CallController {

    private CallViewCallback viewCallback;
    private CallGliaCallback gliaCallback;
    private final GliaCallRepository repository;
    private CallTimer.TimerStatusListener timerStatusListener;
    private final CallTimer callTimer;

    private final String TAG = "CallController";
    private volatile CallState callState;
    private volatile DialogsState dialogsState;

    public CallController(GliaCallRepository callRepository, CallTimer callTimer, CallViewCallback viewCallback) {
        Logger.d(TAG, "constructor");
        this.viewCallback = viewCallback;
        this.callState = new CallState.Builder()
                .setIntegratorCallStarted(false)
                .setVisible(false)
                .setHasOverlayPermissions(false)
                .setMessagesNotSeen(0)
                .setCallStatus(new CallStatus.NotOngoing())
                .createCallState();
        this.dialogsState = new DialogsState.NoDialog();
        this.repository = callRepository;
        this.callTimer = callTimer;
    }

    public void initCall() {
        Logger.d(TAG, "initCall");
        if (callState.hasOverlayPermissions) {
            handleFloatingChatheads(false);
        }
        emitViewState(callState.initCall());
        initControllerCallback();
        repository.init(gliaCallback);
    }

    public void onDestroy(boolean retain) {
        Logger.d(TAG, "onDestroy, retain: " + retain);
        viewCallback = null;
        if (!retain) {
            if (repository != null) {
                repository.onDestroy();
            }
            gliaCallback = null;
            if (timerStatusListener != null) {
                callTimer.removeListener(timerStatusListener);
                timerStatusListener = null;
            }
        }
    }

    private void initControllerCallback() {
        gliaCallback = new CallGliaCallback() {
            @Override
            public void error(GliaException exception) {
                Logger.e(TAG, exception.toString());
                showUnexpectedErrorDialog();
                emitViewState(callState.stop());
            }

            @Override
            public void engagementEnded() {
                Logger.d(TAG, "engagementEnded");
                emitViewState(callState.stop());
            }

            @Override
            public void onMessage(ChatMessage message) {
                Logger.d(TAG, "onMessage: " + message.getContent());
                emitViewState(callState.newMessage());
            }

            @Override
            public void engagementSuccess(OmnicoreEngagement engagement) {
                Logger.d(TAG, "engagementSuccess");
                emitViewState(callState.engagementStarted(
                        engagement.getOperator().getName(),
                        Utils.toMmSs(0))
                );
                createNewTimerStatusCallback();
                callTimer.addListener(timerStatusListener);
            }

            @Override
            public void engagementEndedByOperator() {
                Logger.d(TAG, "engagementEndedByOperator");
                stop();
            }

            @Override
            public void newOperatorMediaState(OperatorMediaState operatorMediaState) {
                Logger.d(TAG, "newOperatorMediaState: " + operatorMediaState.toString());
                if (operatorMediaState.getAudio() == null) {
                    Logger.d(TAG, "newOperatorMediaState: audio null");
                    if (callState.hasOverlayPermissions) {
                        handleFloatingChatheads(true);
                    }
                    viewCallback.navigateToChat();
                }
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
        repository.stop();
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
            CallStatus.StartedAudioCall startedAudioCall =
                    (CallStatus.StartedAudioCall) callState.callStatus;
            emitDialogState(new DialogsState.EndEngagementDialog(startedAudioCall.getFormattedOperatorName()));
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
            emitViewState(callState.hide());
        } else if (callState.hasOverlayPermissions) {
            handleFloatingChatheads(true);
        }
    }

    private void handleFloatingChatheads(boolean show) {
        if (viewCallback != null && callState.hasOverlayPermissions) {
            Logger.d(TAG, "handleFloatingChatHeads, show: " + show);
            viewCallback.handleFloatingChatHead(show);
        }
    }

    private void showExitQueueDialog() {
        if (!isDialogShowing()) {
            emitDialogState(new DialogsState.ExitQueueDialog());
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
    }

    public void chatButtonClicked() {
        Logger.d(TAG, "chatButtonClicked");
        handleFloatingChatheads(true);
        viewCallback.navigateToChat();
        onDestroy(false);
    }

    private void createNewTimerStatusCallback() {
        if (timerStatusListener != null) {
            callTimer.removeListener(timerStatusListener);
        }
        timerStatusListener = new CallTimer.TimerStatusListener() {
            @Override
            public void onNewTimerValue(String formatedValue) {
                if (callState.isCallOngoing()) {
                    emitViewState(callState.newTimerValue(formatedValue));
                }
            }

            @Override
            public void onCancel() {
            }
        };
    }
}
