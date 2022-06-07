package com.glia.widgets.view.head.controller;

import com.glia.androidsdk.Operator;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.core.chathead.domain.IsDisplayApplicationChatHeadUseCase;
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase;
import com.glia.widgets.core.engagement.domain.GetOperatorFlowableUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.visitor.VisitorMediaUpdatesListener;
import com.glia.widgets.core.visitor.domain.AddVisitorMediaStateListenerUseCase;
import com.glia.widgets.core.visitor.domain.RemoveVisitorMediaStateListenerUseCase;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.head.ChatHeadLayoutContract;

import io.reactivex.disposables.CompositeDisposable;

public class ApplicationChatHeadLayoutController
        implements ChatHeadLayoutContract.Controller, VisitorMediaUpdatesListener {
    private static final String TAG = ApplicationChatHeadLayoutController.class.getSimpleName();

    private final IsDisplayApplicationChatHeadUseCase isDisplayApplicationChatHeadUseCase;
    private final ResolveChatHeadNavigationUseCase navigationDestinationUseCase;
    private final GliaOnEngagementUseCase gliaOnEngagementUseCase;
    private final GliaOnEngagementEndUseCase onEngagementEndUseCase;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase;
    private final RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase;
    private final GetOperatorFlowableUseCase getOperatorFlowableUseCase;

    private final CompositeDisposable engagementDisposables = new CompositeDisposable();

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
            AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase,
            RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase,
            GetOperatorFlowableUseCase getOperatorFlowableUseCase
    ) {
        this.isDisplayApplicationChatHeadUseCase = isDisplayApplicationChatHeadUseCase;
        this.navigationDestinationUseCase = navigationDestinationUseCase;
        this.gliaOnEngagementUseCase = gliaOnEngagementUseCase;
        this.onEngagementEndUseCase = onEngagementEndUseCase;
        this.messagesNotSeenHandler = messagesNotSeenHandler;
        this.addVisitorMediaStateListenerUseCase = addVisitorMediaStateListenerUseCase;
        this.removeVisitorMediaStateListenerUseCase = removeVisitorMediaStateListenerUseCase;
        this.getOperatorFlowableUseCase = getOperatorFlowableUseCase;
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
        this.addVisitorMediaStateListenerUseCase.execute(this);
    }

    private void newEngagementLoaded(OmnicoreEngagement engagement) {
        state = State.ENGAGEMENT;
        engagementDisposables.add(
                getOperatorFlowableUseCase.execute()
                        .subscribe(
                                this::operatorDataLoaded,
                                throwable -> Logger.e(TAG, "getOperatorFlowableUseCase error: " + throwable.getMessage())
                        )
        );
        updateChatHeadView();
    }

    private void engagementEnded() {
        state = State.ENDED;
        operatorProfileImgUrl = null;
        unreadMessagesCount = 0;
        engagementDisposables.dispose();
        updateChatHeadView();
    }

    private void onUnreadMessageCountChange(int count) {
        unreadMessagesCount = count;
        updateChatHeadView();
    }

    private void operatorDataLoaded(Operator operator) {
        operatorProfileImgUrl = Utils.getOperatorImageUrl(operator);
        updateChatHeadView();
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
                showOperatorImageOrPlaceholder(view);
                break;
            case QUEUEING:
                view.showQueueing();
                break;
            case ENDED:
            default:
                view.showPlaceholder();
        }
    }

    private void showOperatorImageOrPlaceholder(ChatHeadLayoutContract.View view) {
        if (operatorProfileImgUrl != null) {
            view.showOperatorImage(operatorProfileImgUrl);
        } else {
            view.showPlaceholder();
        }
    }

    private void updateOnHold() {
        if (isOnHold && state == State.ENGAGEMENT) {
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
