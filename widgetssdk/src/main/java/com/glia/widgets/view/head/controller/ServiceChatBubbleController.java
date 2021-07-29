package com.glia.widgets.view.head.controller;

import android.view.View;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.Operator;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.UiTheme;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.queue.QueueTicketsEventsListener;
import com.glia.widgets.core.queue.domain.SubscribeToQueueingStateChangeUseCase;
import com.glia.widgets.core.queue.domain.UnsubscribeFromQueueingStateChangeUseCase;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.core.chathead.domain.ResolveChatBubbleNavigationUseCase;
import com.glia.widgets.core.chathead.domain.ToggleChatBubbleServiceUseCase;
import com.glia.widgets.view.head.ChatHeadContract;

import java.util.Optional;

public class ServiceChatBubbleController implements ChatHeadContract.Controller {
    private final ToggleChatBubbleServiceUseCase toggleChatBubbleServiceUseCase;
    private final ResolveChatBubbleNavigationUseCase resolveChatBubbleNavigationUseCase;

    private final GliaOnEngagementUseCase gliaOnEngagementUseCase;
    private final GliaOnEngagementEndUseCase gliaOnEngagementEndUseCase;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final SubscribeToQueueingStateChangeUseCase subscribeToQueueingStateChangeUseCase;
    private final UnsubscribeFromQueueingStateChangeUseCase unsubscribeFromQueueingStateChangeUseCase;

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

    private ChatHeadContract.View chatHeadView;
    private State state = State.ENDED;
    private String operatorProfileImgUrl = null;
    private int unreadMessagesCount = 0;

    private GliaSdkConfiguration sdkConfiguration;
    private UiTheme buildTimeTheme;

    public ServiceChatBubbleController(
            ToggleChatBubbleServiceUseCase toggleChatBubbleServiceUseCase,
            ResolveChatBubbleNavigationUseCase resolveChatBubbleNavigationUseCase,
            GliaOnEngagementUseCase gliaOnEngagementUseCase,
            GliaOnEngagementEndUseCase onEngagementEndUseCase,
            MessagesNotSeenHandler messagesNotSeenHandler,
            SubscribeToQueueingStateChangeUseCase subscribeToQueueingStateChangeUseCase,
            UnsubscribeFromQueueingStateChangeUseCase unsubscribeFromQueueingStateChangeUseCase
    ) {
        this.toggleChatBubbleServiceUseCase = toggleChatBubbleServiceUseCase;
        this.resolveChatBubbleNavigationUseCase = resolveChatBubbleNavigationUseCase;
        this.gliaOnEngagementUseCase = gliaOnEngagementUseCase;
        this.gliaOnEngagementEndUseCase = onEngagementEndUseCase;
        this.messagesNotSeenHandler = messagesNotSeenHandler;
        this.subscribeToQueueingStateChangeUseCase = subscribeToQueueingStateChangeUseCase;
        this.unsubscribeFromQueueingStateChangeUseCase = unsubscribeFromQueueingStateChangeUseCase;
    }

    @Override
    public void onDestroy() {
        toggleChatBubbleServiceUseCase.execute(null);
    }

    @Override
    public void onResume(View view) {
        toggleChatBubbleServiceUseCase.execute(view);
    }

    @Override
    public void onSetChatHeadView(ChatHeadContract.View view) {
        this.chatHeadView = view;
    }

    @Override
    public void onChatHeadClicked() {
        switch (resolveChatBubbleNavigationUseCase.execute()) {
            case CALL_VIEW:
                chatHeadView.navigateToCall();
                break;
            case CHAT_VIEW:
            default:
                chatHeadView.navigateToChat();
        }
    }

    public void setSdkConfiguration(GliaSdkConfiguration configuration) {
        this.sdkConfiguration = configuration;
    }

    private void engagementEnded() {
        state = State.ENDED;
        operatorProfileImgUrl = null;
        unreadMessagesCount = 0;
        updateChatHeadView();
    }

    private void newEngagementLoaded(OmnicoreEngagement engagement) {
        state = State.ENGAGEMENT;
        operatorDataLoaded(engagement.getOperator());
        gliaOnEngagementEndUseCase.execute(this::engagementEnded);
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

    public void init() {
        gliaOnEngagementUseCase.execute(this::newEngagementLoaded);
        messagesNotSeenHandler.addListener(this::onUnreadMessageCountChange);
        subscribeToQueueingStateChangeUseCase.execute(queueTicketsEventsListener);
    }

    public void updateChatHeadView() {
        if (chatHeadView != null) {
            updateChatHeadViewState();
            chatHeadView.showUnreadMessageCount(unreadMessagesCount);
            chatHeadView.updateConfiguration(buildTimeTheme, sdkConfiguration);
        }
    }

    private void updateChatHeadViewState() {
        switch (state) {
            case ENGAGEMENT:
                chatHeadView.showOperatorImage(operatorProfileImgUrl);
                break;
            case QUEUEING:
                chatHeadView.showQueueing();
                break;
            case ENDED:
            default:
                chatHeadView.showPlaceholder();
        }
    }

    public void setBuildTimeTheme(UiTheme theme) {
        this.buildTimeTheme = theme;
    }

    private enum State {
        ENDED,
        QUEUEING,
        ENGAGEMENT
    }
}

