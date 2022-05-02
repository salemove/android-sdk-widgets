package com.glia.widgets.view.head.controller;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.Operator;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.core.chathead.domain.IsDisplayApplicationChatHeadUseCase;
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.queue.QueueTicketsEventsListener;
import com.glia.widgets.core.queue.domain.SubscribeToQueueingStateChangeUseCase;
import com.glia.widgets.core.queue.domain.UnsubscribeFromQueueingStateChangeUseCase;
import com.glia.widgets.core.visitor.VisitorMediaUpdatesListener;
import com.glia.widgets.core.visitor.domain.AddVisitorMediaStateListenerUseCase;
import com.glia.widgets.core.visitor.domain.RemoveVisitorMediaStateListenerUseCase;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.head.ChatHeadLayoutContract;

import java.util.Optional;

public class ApplicationChatHeadLayoutController
        implements ChatHeadLayoutContract.Controller, VisitorMediaUpdatesListener {
    private final IsDisplayApplicationChatHeadUseCase isDisplayApplicationChatHeadUseCase;
    private final ResolveChatHeadNavigationUseCase navigationDestinationUseCase;
    private final GliaOnEngagementUseCase gliaOnEngagementUseCase;
    private final GliaOnEngagementEndUseCase onEngagementEndUseCase;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final SubscribeToQueueingStateChangeUseCase subscribeToQueueingStateChangeUseCase;
    private final UnsubscribeFromQueueingStateChangeUseCase unsubscribeFromQueueingStateChangeUseCase;
    private final AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase;
    private final RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase;

    private final QueueTicketsEventsListener queueTicketsEventsListener = new QueueTicketsEventsListener() {
        @Override
        public void onTicketReceived(String ticketId) {
            // no-op
        }

        @Override
        public void started() {
            state = State.QUEUEING;
            updateChatHeadView();
        }

        @Override
        public void ongoing() {
            state = State.QUEUEING;
            updateChatHeadView();
        }

        @Override
        public void stopped() {
            state = State.ENDED;
            updateChatHeadView();
        }

        @Override
        public void error(GliaException exception) {
            state = State.ENDED;
            updateChatHeadView();
        }
    };

    private ChatHeadLayoutContract.View chatHeadLayout;

    private State state = State.ENDED;
    private String operatorProfileImgUrl = null;
    private int unreadMessagesCount = 0;
    private boolean isOnHold = false;

    public ApplicationChatHeadLayoutController(
            IsDisplayApplicationChatHeadUseCase isDisplayApplicationChatHeadUseCase,
            ResolveChatHeadNavigationUseCase navigationDestinationUseCase,
            GliaOnEngagementUseCase gliaOnEngagementUseCase,
            GliaOnEngagementEndUseCase onEngagementEndUseCase,
            MessagesNotSeenHandler messagesNotSeenHandler,
            SubscribeToQueueingStateChangeUseCase subscribeToQueueingStateChangeUseCase,
            UnsubscribeFromQueueingStateChangeUseCase unsubscribeFromQueueingStateChangeUseCase,
            AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase,
            RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase
    ) {
        this.isDisplayApplicationChatHeadUseCase = isDisplayApplicationChatHeadUseCase;
        this.navigationDestinationUseCase = navigationDestinationUseCase;
        this.gliaOnEngagementUseCase = gliaOnEngagementUseCase;
        this.onEngagementEndUseCase = onEngagementEndUseCase;
        this.messagesNotSeenHandler = messagesNotSeenHandler;
        this.subscribeToQueueingStateChangeUseCase = subscribeToQueueingStateChangeUseCase;
        this.unsubscribeFromQueueingStateChangeUseCase = unsubscribeFromQueueingStateChangeUseCase;
        this.addVisitorMediaStateListenerUseCase = addVisitorMediaStateListenerUseCase;
        this.removeVisitorMediaStateListenerUseCase = removeVisitorMediaStateListenerUseCase;
    }

    @Override
    public void onChatHeadClicked() {
        switch (navigationDestinationUseCase.execute()) {
            case CALL_VIEW:
                chatHeadLayout.navigateToCall();
                break;
            case CHAT_VIEW:
                chatHeadLayout.navigateToChat();
                break;
        }
    }

    @Override
    public void setView(ChatHeadLayoutContract.View view) {
        chatHeadLayout = view;
        init();
    }

    @Override
    public void onDestroy() {
        gliaOnEngagementUseCase.unregisterListener(this::newEngagementLoaded);
        messagesNotSeenHandler.removeListener(this::onUnreadMessageCountChange);
        onEngagementEndUseCase.unregisterListener(this::engagementEnded);
        unsubscribeFromQueueingStateChangeUseCase.execute(queueTicketsEventsListener);
        removeVisitorMediaStateListenerUseCase.execute(this);
    }

    @Override
    public void onNewVisitorMediaState(VisitorMediaState visitorMediaState) {
    } // no-op

    @Override
    public void onHoldChanged(boolean isOnHold) {
        this.isOnHold = isOnHold;
    }

    public void updateChatHeadView() {
        updateChatHeadViewState(chatHeadLayout);
        updateOnHold();
        chatHeadLayout.showUnreadMessageCount(unreadMessagesCount);
        updateChatHeadLayoutVisibility(chatHeadLayout);
    }

    private void init() {
        this.gliaOnEngagementUseCase.execute(this::newEngagementLoaded);
        this.messagesNotSeenHandler.addListener(this::onUnreadMessageCountChange);
        this.onEngagementEndUseCase.execute(this::engagementEnded);
        this.subscribeToQueueingStateChangeUseCase.execute(queueTicketsEventsListener);
        this.addVisitorMediaStateListenerUseCase.execute(this);
    }

    private void newEngagementLoaded(OmnicoreEngagement engagement) {
        state = State.ENGAGEMENT;
        operatorDataLoaded(engagement.getOperator());
        updateChatHeadView();
    }

    private void engagementEnded() {
        state = State.ENDED;
        operatorProfileImgUrl = null;
        unreadMessagesCount = 0;
        updateChatHeadView();
    }

    private void onUnreadMessageCountChange(int count) {
        unreadMessagesCount = count;
        updateChatHeadView();
    }

    private void operatorDataLoaded(Operator operator) {
        try {
            Optional<String> operatorImageUrl = operator.getPicture().getURL();
            operatorImageUrl.ifPresent(s -> operatorProfileImgUrl = s);
        } catch (Exception ignored) {
        }
    }

    private void updateChatHeadLayoutVisibility(ChatHeadLayoutContract.View view) {
        if (isDisplayApplicationChatHeadUseCase.execute(view.isInChatView())) {
            view.show();
        } else {
            view.hide();
        }
    }

    private void updateChatHeadViewState(ChatHeadLayoutContract.View view) {
        switch (state) {
            case ENGAGEMENT:
                view.showOperatorImage(operatorProfileImgUrl);
                break;
            case QUEUEING:
                view.showQueueing();
                break;
            case ENDED:
            default:
                view.showPlaceholder();
        }
    }

    private void updateOnHold() {
        if (isOnHold) {
            chatHeadLayout.showOnHold();
        } else {
            chatHeadLayout.hideOnHold();
        }
    }

    private enum State {
        ENDED,
        QUEUEING,
        ENGAGEMENT
    }
}
