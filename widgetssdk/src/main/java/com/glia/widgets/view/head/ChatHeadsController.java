package com.glia.widgets.view.head;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.Operator;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.Constants;
import com.glia.widgets.UiTheme;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.operator.GliaOperatorMediaRepository;
import com.glia.widgets.core.operator.domain.AddOperatorMediaStateListenerUseCase;
import com.glia.widgets.core.queue.QueueTicketsEventsListener;
import com.glia.widgets.core.queue.domain.AddQueueTicketsEventsListenerUseCase;
import com.glia.widgets.core.queue.domain.GetIsMediaQueueingOngoingUseCase;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.head.model.ChatHeadInput;
import com.glia.widgets.core.permissions.domain.HasOverlayEnabledUseCase;

import java.util.ArrayList;
import java.util.List;

public class ChatHeadsController implements
        GliaOnEngagementUseCase.Listener,
        GliaOnEngagementEndUseCase.Listener {

    private final static String TAG = "ChatHeadsController";

    private ChatHeadState chatHeadState;
    private ChatHeadInput lastInput = null;
    private final List<OnChatheadSettingsChangedListener> viewListeners = new ArrayList<>();
    private OnChatheadSettingsChangedListener overlayListener;
    private ChatHeadServiceListener chatHeadServiceListener;

    private final GliaOnEngagementUseCase gliaOnEngagementUseCase;
    private final GliaOnEngagementEndUseCase gliaOnEngagementEndUseCase;
    private final AddOperatorMediaStateListenerUseCase addOperatorMediaStateListenerUseCase;
    private final AddQueueTicketsEventsListenerUseCase addQueueTicketsEventsListenerUseCase;
    private final GetIsMediaQueueingOngoingUseCase getIsMediaQueueingOngoingUseCase;
    private final HasOverlayEnabledUseCase hasOverlayEnabledUseCase;

    private final GliaOperatorMediaRepository.OperatorMediaStateListener operatorMediaStateListener = this::onNewOperatorMediaState;

    private final QueueTicketsEventsListener queueTicketsEventsListener = new QueueTicketsEventsListener() {
        @Override
        public void onTicketReceived(String ticketId) {
            onQueueTicketReceived(ticketId);
        }

        @Override
        public void started() {
            // no-op
        }

        @Override
        public void ongoing() {
            // no-op
        }

        @Override
        public void stopped() {
            // no-op
        }

        @Override
        public void error(GliaException exception) {
            // no-op
        }
    };


    public ChatHeadsController(
            GliaOnEngagementUseCase gliaOnEngagementUseCase,
            GliaOnEngagementEndUseCase gliaOnEngagementEndUseCase,
            AddOperatorMediaStateListenerUseCase addOperatorMediaStateListenerUseCase,
            AddQueueTicketsEventsListenerUseCase addQueueTicketsEventsListenerUseCase,
            MessagesNotSeenHandler messagesNotSeenHandler,
            GetIsMediaQueueingOngoingUseCase isMediaQueueingOngoingUseCase,
            HasOverlayEnabledUseCase hasOverlayEnabledUseCase
    ) {
        this.chatHeadState = new ChatHeadState.Builder()
                .setMessageCount(0)
                .createChatHeadState();
        messagesNotSeenHandler.addListener(count -> {
            Logger.d(TAG, "new message count received: " + count);
            emitViewState(chatHeadState.onNewMessage(count));
        });
        this.gliaOnEngagementUseCase = gliaOnEngagementUseCase;
        this.gliaOnEngagementEndUseCase = gliaOnEngagementEndUseCase;
        this.addOperatorMediaStateListenerUseCase = addOperatorMediaStateListenerUseCase;
        this.addQueueTicketsEventsListenerUseCase = addQueueTicketsEventsListenerUseCase;
        this.getIsMediaQueueingOngoingUseCase = isMediaQueueingOngoingUseCase;
        this.hasOverlayEnabledUseCase = hasOverlayEnabledUseCase;
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
    }

    public void init(boolean enableChatHeads) {
        Logger.d(TAG, "init");
        emitViewState(chatHeadState
                .enableChatHeadsChanged(enableChatHeads)
                .setUseOverlays(hasOverlayEnabledUseCase.execute())
        );
        addOperatorMediaStateListenerUseCase.execute(operatorMediaStateListener);
        addQueueTicketsEventsListenerUseCase.execute(queueTicketsEventsListener);
        handleService();
    }

    public void onChatBackButtonPressed() {
        Logger.d(TAG, "onChatBackButtonPressed");
        emitViewState(chatHeadState.onNewMessage(0).changeVisibility(
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
            boolean useOverlays
    ) {
        emitViewState(chatHeadState.setUseOverlays(useOverlays));
        boolean hasOngoingMediaQueueing = getIsMediaQueueingOngoingUseCase.execute();
        Logger.d(TAG, "onNavigatedToChat, hasOngoingMediaQueueing: " + hasOngoingMediaQueueing);

        if (hasOngoingMediaQueueing) {
            emitViewState(chatHeadState.changeVisibility(true, Constants.CALL_ACTIVITY));
        } else {
            changeVisibilityByMedia();
        }
        if (chatHeadInput != null) {
            lastInput = chatHeadInput;
        }
        init(enableChatHeads);
    }

    public void onSetupViewAppearance(UiTheme theme) {
        emitViewState(chatHeadState.themeChanged(theme));
    }

    private void changeVisibilityByMedia() {
        boolean hasOngoingMedia = chatHeadState.operatorMediaState != null && (chatHeadState.operatorMediaState.getAudio() != null || chatHeadState.operatorMediaState.getVideo() != null);
        emitViewState(chatHeadState.changeVisibility(chatHeadState.engagementRequested && hasOngoingMedia, hasOngoingMedia ? Constants.CALL_ACTIVITY : null));
    }

    public void onNavigatedToCall(
            ChatHeadInput chatHeadInput,
            boolean enableChatHeads,
            boolean useOverlays) {
        Logger.d(TAG, "onNavigatedToCall");
        emitViewState(
                chatHeadState.changeVisibility(
                        false,
                        null)
                        .setUseOverlays(useOverlays)
        );
        if (chatHeadInput != null) {
            lastInput = chatHeadInput;
        }
        init(enableChatHeads);
    }

    public void chatEndedByUser() {
        Logger.d(TAG, "chatEndedByUser");
        emitViewState(
                chatHeadState.onNewMessage(0)
                        .changeVisibility(false, null).
                        setOperatorMediaState(null)
        );
    }

    public void addChatHeadServiceListener(ChatHeadServiceListener chatHeadServiceListener) {
        Logger.d(TAG, "addChatHeadServiceListener");
        this.chatHeadServiceListener = chatHeadServiceListener;
    }

    private void handleService() {
        if (chatHeadState.useOverlays) {
            if (hasOverlayEnabledUseCase.execute()) {
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
        emitViewState(
                chatHeadState
                        .setOperatorMediaState(null)
                        .setOperatorProfileImgUrl(null)
                        .changeEngagementRequested(false)
        );
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        Logger.d(TAG, "newEngagementLoaded");
        operatorDataLoaded(engagement.getOperator());
        gliaOnEngagementEndUseCase.execute(this);
    }

    public void onNewOperatorMediaState(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "new operatorMediaState: " + operatorMediaState);
        handleService();
        emitViewState(chatHeadState.setOperatorMediaState(operatorMediaState));

    }

    public void onQueueTicketReceived(String ticket) {
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
        emitViewState(chatHeadState
                .setOperatorProfileImgUrl(operatorProfileImgUrl)
                .changeEngagementRequested(true)
        );
    }
}
