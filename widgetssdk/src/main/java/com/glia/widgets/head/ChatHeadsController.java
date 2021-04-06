package com.glia.widgets.head;

import com.glia.androidsdk.Operator;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.widgets.Constants;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.model.ChatHeadInput;
import com.glia.widgets.model.GliaChatHeadControllerRepository;
import com.glia.widgets.model.GliaChatHeadControllerRepositoryCallback;
import com.glia.widgets.model.MessagesNotSeenHandler;

import java.util.ArrayList;
import java.util.List;

public class ChatHeadsController {

    private final static String TAG = "ChatHeadsController";

    private ChatHeadState chatHeadState;
    private final GliaChatHeadControllerRepository repository;
    private ChatHeadInput lastInput = null;
    private final List<OnChatheadSettingsChangedListener> viewListeners = new ArrayList<>();
    private OnChatheadSettingsChangedListener overlayListener;
    private ChatHeadServiceListener chatHeadServiceListener;

    public ChatHeadsController(
            GliaChatHeadControllerRepository gliaChatHeadControllerRepository,
            MessagesNotSeenHandler messagesNotSeenHandler
    ) {
        this.repository = gliaChatHeadControllerRepository;
        this.chatHeadState = new ChatHeadState.Builder()
                .setMessageCount(0)
                .createChatHeadState();
        messagesNotSeenHandler.addListener(count -> {
            Logger.d(TAG, "new message count received: " + count);
            emitViewState(chatHeadState.onNewMessage(count));
        });
    }

    public void addListener(OnChatheadSettingsChangedListener listener) {
        Logger.d(TAG, "addListener");
        listener.emitState(chatHeadState);
        viewListeners.add(listener);
    }

    public void addOverlayListener(OnChatheadSettingsChangedListener listener) {
        Logger.d(TAG, "addOverlayListener");
        overlayListener = listener;
        overlayListener.emitState(chatHeadState);
    }

    public void removeListener(OnChatheadSettingsChangedListener listener) {
        Logger.d(TAG, "removeListener");
        viewListeners.remove(listener);
    }

    public void clearOverlayListener() {
        Logger.d(TAG, "clearOverlayListener");
        overlayListener = null;
    }

    public void initChatObserving() {
        Logger.d(TAG, "initChatObserving");
        repository.init(repositoryCallback);
    }

    public void setUseOverlays(boolean useOverlays) {
        Logger.d(TAG, "setUseOverlays: " + useOverlays);
        emitViewState(chatHeadState.setUseOverlays(useOverlays));
    }

    public void setHasOverlayPermissions(boolean hasOverlayPermissions) {
        Logger.d(TAG, "setHasOverlayPermissions: " + hasOverlayPermissions);
        emitViewState(chatHeadState.setHasOverlayPermissions(hasOverlayPermissions));
        handleService();
    }

    public void onBackButtonPressed(
            String callingActivity,
            boolean isChatInBackstack
    ) {
        Logger.d(TAG, "onBackButtonPressed, callingActivity: " + callingActivity +
                ", isChatInBackstack: " + isChatInBackstack);
        if (callingActivity.equals(Constants.CALL_ACTIVITY)) {
            emitViewState(chatHeadState.changeVisibility(
                    chatHeadState.operatorMediaState != null,
                    callingActivity));
        } else if (callingActivity.equals(Constants.CHAT_ACTIVITY)) {
            emitViewState(chatHeadState.onNewMessage(0));
            emitViewState(chatHeadState.changeVisibility(true, callingActivity));
        }
    }

    public void onMinimizeButtonClicked() {
        Logger.d(TAG, "onMinimizeButtonClicked");
        emitViewState(chatHeadState.changeVisibility(true, Constants.CALL_ACTIVITY));
    }

    public void onChatButtonClicked() {
        Logger.d(TAG, "onChatButtonClicked");
        emitViewState(chatHeadState.changeVisibility(true, Constants.CALL_ACTIVITY));
    }

    public void onNavigatedToChat(ChatHeadInput chatHeadInput) {
        Logger.d(TAG, "onNavigatedToChat");
        boolean hasOngoingMedia = chatHeadState.operatorMediaState != null &&
                (chatHeadState.operatorMediaState.getAudio() != null ||
                        chatHeadState.operatorMediaState.getVideo() != null);
        emitViewState(chatHeadState.changeVisibility(
                hasOngoingMedia,
                hasOngoingMedia ? Constants.CALL_ACTIVITY : null
        ));
        if (chatHeadInput != null && chatHeadInput.uiTheme != null) {
            emitViewState(chatHeadState.themeChanged(chatHeadInput.uiTheme));
        }
        if (chatHeadInput != null) {
            lastInput = chatHeadInput;
        }
    }

    public void onNavigatedToCall() {
        Logger.d(TAG, "onNavigatedToCall");
        emitViewState(chatHeadState.changeVisibility(
                false,
                null
        ));
    }

    public void chatEndedByUser() {
        Logger.d(TAG, "chatEndedByUser");
        emitViewState(chatHeadState.onNewMessage(0));
        emitViewState(chatHeadState.changeVisibility(false, null));
        emitViewState(chatHeadState.setOperatorMediaState(null));
    }

    public void addChatHeadServiceListener(ChatHeadServiceListener chatHeadServiceListener) {
        Logger.d(TAG, "addChatHeadServiceListener");
        this.chatHeadServiceListener = chatHeadServiceListener;
    }

    private void handleService() {
        if (chatHeadState.useOverlays) {
            if (chatHeadState.hasOverlayPermissions) {
                if (!isOverlayServiceStarted()) {
                    chatHeadServiceListener.startService();
                }
            } else {
                chatHeadServiceListener.stopService();
            }
        } else {
            chatHeadServiceListener.stopService();
        }
    }

    private boolean isOverlayServiceStarted() {
        return overlayListener != null;
    }

    public interface OnChatheadSettingsChangedListener {
        void emitState(ChatHeadState chatHeadState);
    }

    public interface ChatHeadServiceListener {
        void startService();

        void stopService();
    }

    private final GliaChatHeadControllerRepositoryCallback repositoryCallback =
            new GliaChatHeadControllerRepositoryCallback() {

                @Override
                public void operatorDataLoaded(Operator operator) {
                    Logger.d(TAG, "operatorDataLoaded: " + operator.toString());
                    String operatorProfileImgUrl = null;
                    try {
                        operatorProfileImgUrl = operator.getPicture().getURL().get();
                    } catch (Exception e) {
                        Logger.d(TAG, "operatorDataLoaded, ex: " + e.toString());
                    }
                    emitViewState(chatHeadState.setOperatorProfileImgUrl(operatorProfileImgUrl));
                }

                @Override
                public void newOperatorMediaState(OperatorMediaState operatorMediaState) {
                    Logger.d(TAG, "new operatorMediaState: " + operatorMediaState);
                    emitViewState(chatHeadState.setOperatorMediaState(operatorMediaState));
                }
            };

    private synchronized void emitViewState(ChatHeadState state) {
        if (setState(state)) {
            Logger.d(TAG, "Emit state:\n" + state.toString());
            for (OnChatheadSettingsChangedListener listener : viewListeners) {
                listener.emitState(chatHeadState);
            }
            if (overlayListener != null) {
                overlayListener.emitState(chatHeadState);
            }
        }
    }

    private synchronized boolean setState(ChatHeadState state) {
        if (this.chatHeadState.equals(state)) return false;
        this.chatHeadState = state;
        return true;
    }

    public ChatHeadInput chatHeadClicked() {
        return lastInput;
    }
}
