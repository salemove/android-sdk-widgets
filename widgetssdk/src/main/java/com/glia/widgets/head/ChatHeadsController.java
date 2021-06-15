package com.glia.widgets.head;

import com.glia.androidsdk.Operator;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.Constants;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.glia.GliaOnEngagementEndUseCase;
import com.glia.widgets.glia.GliaOnEngagementUseCase;
import com.glia.widgets.glia.GliaOnOperatorMediaStateUseCase;
import com.glia.widgets.glia.GliaOnQueueTicketUseCase;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.model.ChatHeadInput;
import com.glia.widgets.model.MessagesNotSeenHandler;
import com.glia.widgets.model.PermissionType;
import com.glia.widgets.permissions.CheckIfHasPermissionsUseCase;

import java.util.ArrayList;
import java.util.List;

public class ChatHeadsController implements
        GliaOnEngagementUseCase.Listener,
        GliaOnQueueTicketUseCase.Listener,
        GliaOnEngagementEndUseCase.Listener,
        GliaOnOperatorMediaStateUseCase.Listener {

    private final static String TAG = "ChatHeadsController";

    private ChatHeadState chatHeadState;
    private ChatHeadInput lastInput = null;
    private final List<OnChatheadSettingsChangedListener> viewListeners = new ArrayList<>();
    private OnChatheadSettingsChangedListener overlayListener;
    private ChatHeadServiceListener chatHeadServiceListener;

    private final GliaOnEngagementUseCase gliaOnEngagementUseCase;
    private final GliaOnQueueTicketUseCase onQueueTicketUseCase;
    private final GliaOnEngagementEndUseCase gliaOnEngagementEndUseCase;
    private final GliaOnOperatorMediaStateUseCase gliaOnOperatorMediaStateUseCase;
    private final CheckIfHasPermissionsUseCase checkIfHasPermissionsUseCase;

    public ChatHeadsController(
            GliaOnEngagementUseCase gliaOnEngagementUseCase,
            GliaOnQueueTicketUseCase onQueueTicketUseCase,
            GliaOnEngagementEndUseCase gliaOnEngagementEndUseCase,
            GliaOnOperatorMediaStateUseCase gliaOnOperatorMediaStateUseCase,
            CheckIfHasPermissionsUseCase checkIfHasPermissionsUseCase,
            MessagesNotSeenHandler messagesNotSeenHandler
    ) {
        this.chatHeadState = new ChatHeadState.Builder()
                .setMessageCount(0)
                .createChatHeadState();
        messagesNotSeenHandler.addListener(count -> {
            Logger.d(TAG, "new message count received: " + count);
            emitViewState(chatHeadState.onNewMessage(count));
        });
        this.gliaOnEngagementUseCase = gliaOnEngagementUseCase;
        this.onQueueTicketUseCase = onQueueTicketUseCase;
        this.gliaOnEngagementEndUseCase = gliaOnEngagementEndUseCase;
        this.gliaOnOperatorMediaStateUseCase = gliaOnOperatorMediaStateUseCase;
        this.checkIfHasPermissionsUseCase = checkIfHasPermissionsUseCase;
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
        gliaOnEngagementUseCase.execute(this);
        onQueueTicketUseCase.execute(this);
    }

    public void init(boolean enableChatHeads, boolean useOverlays) {
        Logger.d(TAG, "init");
        emitViewState(chatHeadState.enableChatHeadsChanged(enableChatHeads));
        emitViewState(chatHeadState.setUseOverlays(useOverlays));
        handleService();
    }

    public void onChatBackButtonPressed() {
        Logger.d(TAG, "onChatBackButtonPressed");
        emitViewState(chatHeadState.onNewMessage(0));
        emitViewState(chatHeadState.changeVisibility(
                chatHeadState.engagementRequested,
                Constants.CHAT_ACTIVITY
        ));
    }

    public void onCallBackButtonPressed() {
        Logger.d(TAG, "onCallBackButtonPressed");
        emitViewState(chatHeadState.changeVisibility(
                chatHeadState.engagementRequested,
                Constants.CALL_ACTIVITY));
    }

    public void onMinimizeButtonClicked() {
        Logger.d(TAG, "onMinimizeButtonClicked");
        emitViewState(chatHeadState.changeVisibility(
                chatHeadState.engagementRequested,
                Constants.CALL_ACTIVITY
        ));
    }

    public void onChatButtonClicked() {
        Logger.d(TAG, "onChatButtonClicked");
        emitViewState(chatHeadState.changeVisibility(
                chatHeadState.engagementRequested,
                Constants.CALL_ACTIVITY
        ));
    }

    public void onNavigatedToChat(
            ChatHeadInput chatHeadInput,
            boolean enableChatHeads,
            boolean useOverlays,
            boolean hasOngoingMediaQueueing
    ) {
        Logger.d(TAG, "onNavigatedToChat, hasOngoingMediaQueueing: " + hasOngoingMediaQueueing);
        if (hasOngoingMediaQueueing) {
            emitViewState(chatHeadState.changeVisibility(
                    true,
                    Constants.CALL_ACTIVITY
            ));
        } else {
            changeVisibilityByMedia();
        }
        if (chatHeadInput != null && chatHeadInput.uiTheme != null) {
            emitViewState(chatHeadState.themeChanged(chatHeadInput.uiTheme));
        }
        if (chatHeadInput != null) {
            lastInput = chatHeadInput;
        }
        init(enableChatHeads, useOverlays);
    }

    private void changeVisibilityByMedia() {
        boolean hasOngoingMedia = chatHeadState.operatorMediaState != null &&
                (chatHeadState.operatorMediaState.getAudio() != null ||
                        chatHeadState.operatorMediaState.getVideo() != null);

        emitViewState(chatHeadState.changeVisibility(
                chatHeadState.engagementRequested && hasOngoingMedia,
                hasOngoingMedia ? Constants.CALL_ACTIVITY : null
        ));
    }

    public void onNavigatedToCall(
            ChatHeadInput chatHeadInput,
            boolean enableChatHeads,
            boolean useOverlays) {
        Logger.d(TAG, "onNavigatedToCall");
        emitViewState(chatHeadState.changeVisibility(
                false,
                null
        ));
        if (chatHeadInput != null) {
            lastInput = chatHeadInput;
        }
        init(true, useOverlays);
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
            if (checkIfHasPermissionsUseCase.execute(PermissionType.OVERLAY)) {
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

    @Override
    public void engagementEnded() {
        Logger.d(TAG, "engagementEnded");
        emitViewState(chatHeadState.setOperatorMediaState(null));
        emitViewState(chatHeadState.setOperatorProfileImgUrl(null));
        emitViewState(chatHeadState.changeEngagementRequested(false));
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        Logger.d(TAG, "newEngagementLoaded");
        operatorDataLoaded(engagement.getOperator());
        gliaOnEngagementEndUseCase.execute(this);
        gliaOnOperatorMediaStateUseCase.execute(this);
    }

    @Override
    public void onNewOperatorMediaState(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "new operatorMediaState: " + operatorMediaState);
        handleService();
        emitViewState(chatHeadState.setOperatorMediaState(operatorMediaState));

    }

    @Override
    public void ticketLoaded(String ticket) {
        Logger.d(TAG, "ticketLoaded: " + ticket);
        handleService();
        emitViewState(chatHeadState.changeEngagementRequested(true));
    }

    public interface OnChatheadSettingsChangedListener {
        void emitState(ChatHeadState chatHeadState);
    }

    public interface ChatHeadServiceListener {
        void startService();

        void stopService();
    }

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

    private void operatorDataLoaded(Operator operator) {
        Logger.d(TAG, "operatorDataLoaded: " + operator.toString());
        String operatorProfileImgUrl = null;
        try {
            operatorProfileImgUrl = operator.getPicture().getURL().get();
        } catch (Exception e) {
            Logger.d(TAG, "operatorDataLoaded, ex: " + e.toString());
        }
        emitViewState(chatHeadState.setOperatorProfileImgUrl(operatorProfileImgUrl));
        emitViewState(chatHeadState.changeEngagementRequested(true));
    }
}
