package com.glia.widgets.view.head.controller;

import android.view.View;

import androidx.core.util.Pair;

import com.glia.androidsdk.Operator;
import com.glia.androidsdk.comms.VisitorMediaState;
import com.glia.androidsdk.omnibrowse.OmnibrowseEngagement;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.UiTheme;
import com.glia.widgets.core.callvisualizer.domain.GliaOnCallVisualizerEndUseCase;
import com.glia.widgets.core.callvisualizer.domain.GliaOnCallVisualizerUseCase;
import com.glia.widgets.core.callvisualizer.domain.IsCallVisualizerScreenSharingUseCase;
import com.glia.widgets.core.chathead.domain.ResolveChatHeadNavigationUseCase;
import com.glia.widgets.core.chathead.domain.ToggleChatHeadServiceUseCase;
import com.glia.widgets.core.configuration.GliaSdkConfiguration;
import com.glia.widgets.core.engagement.domain.GetOperatorFlowableUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.core.engagement.domain.GliaOnEngagementUseCase;
import com.glia.widgets.core.visitor.VisitorMediaUpdatesListener;
import com.glia.widgets.core.visitor.domain.AddVisitorMediaStateListenerUseCase;
import com.glia.widgets.core.visitor.domain.RemoveVisitorMediaStateListenerUseCase;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.CommonExtensionsKt;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.view.MessagesNotSeenHandler;
import com.glia.widgets.view.head.ChatHeadContract;
import com.glia.widgets.view.head.ChatHeadPosition;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class ServiceChatHeadController
    implements ChatHeadContract.Controller, VisitorMediaUpdatesListener,
    GliaOnEngagementEndUseCase.Listener, GliaOnCallVisualizerEndUseCase.Listener {
    private static final String TAG = ServiceChatHeadController.class.getSimpleName();

    private final ToggleChatHeadServiceUseCase toggleChatHeadServiceUseCase;
    private final ResolveChatHeadNavigationUseCase resolveChatHeadNavigationUseCase;

    private final GliaOnEngagementUseCase gliaOnEngagementUseCase;
    private final GliaOnCallVisualizerUseCase gliaOnCallVisualizerUseCase;
    private final GliaOnEngagementEndUseCase gliaOnEngagementEndUseCase;
    private final GliaOnCallVisualizerEndUseCase gliaOnCallVisualizerEndUseCase;
    private final MessagesNotSeenHandler messagesNotSeenHandler;
    private final AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase;
    private final RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase;
    private final GetOperatorFlowableUseCase getOperatorFlowableUseCase;
    private final IsCallVisualizerScreenSharingUseCase isCallVisualizerScreenSharingUseCase;
    private final ChatHeadPosition chatHeadPosition;


    private final CompositeDisposable engagementDisposables = new CompositeDisposable();

    private ChatHeadContract.View chatHeadView;
    private State state = State.ENDED;
    private String operatorProfileImgUrl = null;
    private int unreadMessagesCount = 0;
    private boolean isOnHold = false;

    private GliaSdkConfiguration sdkConfiguration;
    private UiTheme buildTimeTheme;

    /*
     * We need to keep track of the currently active (topmost) view. This can be either ChatView
     * or CallView. CallView has translucent theme and this changes the usual lifecycle of an activity.
     * When the translucent CallActivity is on top of the ChatActivity and config change happens (e.g screen rotation)
     * then ChatActivity onResume and onPause are called right after CallActivity's onResume. Current
     * solution ignores such onResume calls because another activity is already in resumed state.
     */
    private String resumedViewName = null;
    private Disposable operatorDisposable = null;

    public ServiceChatHeadController(
        ToggleChatHeadServiceUseCase toggleChatHeadServiceUseCase,
        ResolveChatHeadNavigationUseCase resolveChatHeadNavigationUseCase,
        GliaOnEngagementUseCase gliaOnEngagementUseCase,
        GliaOnCallVisualizerUseCase gliaOnCallVisualizerUseCase,
        GliaOnEngagementEndUseCase onEngagementEndUseCase,
        GliaOnCallVisualizerEndUseCase onCallVisualizerEndUseCase,
        MessagesNotSeenHandler messagesNotSeenHandler,
        AddVisitorMediaStateListenerUseCase addVisitorMediaStateListenerUseCase,
        RemoveVisitorMediaStateListenerUseCase removeVisitorMediaStateListenerUseCase,
        ChatHeadPosition chatHeadPosition,
        GetOperatorFlowableUseCase getOperatorFlowableUseCase,
        IsCallVisualizerScreenSharingUseCase isCallVisualizerScreenSharingUseCase
    ) {
        this.toggleChatHeadServiceUseCase = toggleChatHeadServiceUseCase;
        this.resolveChatHeadNavigationUseCase = resolveChatHeadNavigationUseCase;
        this.gliaOnEngagementUseCase = gliaOnEngagementUseCase;
        this.gliaOnCallVisualizerUseCase = gliaOnCallVisualizerUseCase;
        this.gliaOnEngagementEndUseCase = onEngagementEndUseCase;
        this.gliaOnCallVisualizerEndUseCase = onCallVisualizerEndUseCase;
        this.messagesNotSeenHandler = messagesNotSeenHandler;
        this.chatHeadPosition = chatHeadPosition;
        this.addVisitorMediaStateListenerUseCase = addVisitorMediaStateListenerUseCase;
        this.removeVisitorMediaStateListenerUseCase = removeVisitorMediaStateListenerUseCase;
        this.getOperatorFlowableUseCase = getOperatorFlowableUseCase;
        this.isCallVisualizerScreenSharingUseCase = isCallVisualizerScreenSharingUseCase;
    }

    @Override
    public void onDestroy() {
        toggleChatHeadServiceUseCase.onDestroy();
        removeVisitorMediaStateListenerUseCase.execute(this);
        gliaOnEngagementEndUseCase.unregisterListener(this);
        gliaOnCallVisualizerEndUseCase.unregisterListener(this);
    }

    @Override
    public void onResume(View view) {
        setResumedViewName(view);

        // see the comment on resumedViewName field declaration above
        if (!isResumedView(view)) return;

        toggleChatHeadServiceUseCase.invoke(view.getClass().getSimpleName());
    }

    public void onPause(View view) {
        clearResumedViewName(view);
    }

    @Override
    public void onSetChatHeadView(ChatHeadContract.View view) {
        this.chatHeadView = view;
    }

    @Override
    public void onApplicationStop() {
        Logger.d(TAG, "onApplicationStop()");
        toggleChatHeadServiceUseCase.invoke(null);
    }

    @Override
    public void onChatHeadPositionChanged(int x, int y) {
        chatHeadPosition.set(x, y);
    }

    @Override
    public Pair<Integer, Integer> getChatHeadPosition() {
        return chatHeadPosition.get();
    }

    @Override
    public void onChatHeadClicked() {
        switch (resolveChatHeadNavigationUseCase.execute()) {
            case CALL_VIEW:
                chatHeadView.navigateToCall();
                break;
            case SCREEN_SHARING:
                chatHeadView.navigateToEndScreenSharing();
                break;
            case CHAT_VIEW:
            default:
                chatHeadView.navigateToChat();
        }
    }

    @Override
    public void onNewVisitorMediaState(VisitorMediaState visitorMediaState) {
    } // no-op

    @Override
    public void onHoldChanged(boolean isOnHold) {
        this.isOnHold = isOnHold;
        updateChatHeadView();
    }

    public void setSdkConfiguration(GliaSdkConfiguration configuration) {
        this.sdkConfiguration = configuration;
    }

    public void init() {
        gliaOnEngagementUseCase.execute(this::newEngagementLoaded);
        gliaOnCallVisualizerUseCase.invoke(this::newCallVisualizerEngagementLoaded);
        messagesNotSeenHandler.addListener(this::onUnreadMessageCountChange);
        addVisitorMediaStateListenerUseCase.execute(this);
    }

    public void updateChatHeadView() {
        if (chatHeadView != null && buildTimeTheme != null) {
            updateChatHeadViewState();
            updateOnHold();
            chatHeadView.showUnreadMessageCount(unreadMessagesCount);
            chatHeadView.updateConfiguration(buildTimeTheme, sdkConfiguration);
        }
    }

    public void setBuildTimeTheme(UiTheme theme) {
        this.buildTimeTheme = theme;
    }

    @Override
    public void engagementEnded() {
        callVisualizerEngagementEnded();
    }

    @Override
    public void callVisualizerEngagementEnded() {
        state = State.ENDED;
        operatorProfileImgUrl = null;
        unreadMessagesCount = 0;
        engagementDisposables.clear();
        updateChatHeadView();
    }

    private void newEngagementLoaded(OmnicoreEngagement engagement) {
        state = State.ENGAGEMENT;
        if (operatorDisposable != null) operatorDisposable.dispose();
        operatorDisposable = getOperatorFlowableUseCase.execute()
            .subscribe(
                this::operatorDataLoaded,
                throwable -> Logger.e(TAG, "getOperatorFlowableUseCase error: " + throwable.getMessage())
            );
        engagementDisposables.add(operatorDisposable);
        gliaOnEngagementEndUseCase.execute(this);
        toggleChatHeadServiceUseCase.invoke(resumedViewName);
        if (sdkConfiguration == null) setSdkConfiguration(Dependencies.getSdkConfigurationManager().createWidgetsConfiguration());
        updateChatHeadView();
    }

    private void newCallVisualizerEngagementLoaded(OmnibrowseEngagement engagement) {
        state = State.ENGAGEMENT;
        if (operatorDisposable != null) operatorDisposable.dispose();
        operatorDisposable = getOperatorFlowableUseCase.execute()
            .subscribe(
                this::operatorDataLoaded,
                throwable -> Logger.e(TAG, "getOperatorFlowableUseCase error: " + throwable.getMessage())
            );
        engagementDisposables.add(operatorDisposable);
        // To recieve callback to engagementEnded() after Call Visualizer engagement ends
        gliaOnCallVisualizerEndUseCase.execute(this);
        if (sdkConfiguration == null) setSdkConfiguration(Dependencies.getSdkConfigurationManager().createWidgetsConfiguration());
        updateChatHeadView();
    }

    private void onUnreadMessageCountChange(int count) {
        unreadMessagesCount = count;
        updateChatHeadView();
    }

    private void operatorDataLoaded(Operator operator) {
        operatorProfileImgUrl = CommonExtensionsKt.getImageUrl(operator);
        updateChatHeadView();
    }

    private void updateChatHeadViewState() {
        switch (state) {
            case ENGAGEMENT:
                decideOnBubbleDesign();
                break;
            case QUEUEING:
                chatHeadView.showQueueing();
                break;
            case ENDED:
            default:
                chatHeadView.showPlaceholder();
        }
    }

    private void decideOnBubbleDesign() {
        if (isCallVisualizerScreenSharingUseCase.invoke()) {
            chatHeadView.showScreenSharing();
        } else if (operatorProfileImgUrl != null) {
            chatHeadView.showOperatorImage(operatorProfileImgUrl);
        } else {
            chatHeadView.showPlaceholder();
        }
    }

    private void setResumedViewName(View view) {
        if (resumedViewName == null) {
            resumedViewName = view.getClass().getSimpleName();
        }
    }

    private void clearResumedViewName(View view) {
        // On quick configuration changes view can be null
        if (view != null && isResumedView(view)) {
            resumedViewName = null;
        }
    }

    private boolean isResumedView(View view) {
        return resumedViewName != null &&
            resumedViewName.equals(view.getClass().getSimpleName());
    }

    private void updateOnHold() {
        if (isOnHold && state == State.ENGAGEMENT) {
            chatHeadView.showOnHold();
        } else {
            chatHeadView.hideOnHold();
        }
    }

    private enum State {
        ENDED,
        QUEUEING,
        ENGAGEMENT
    }
}
