package com.glia.widgets.chat;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.androidsdk.comms.MediaDirection;
import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.GliaWidgets;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatItem;
import com.glia.widgets.chat.adapter.MediaUpgradeStartedTimerItem;
import com.glia.widgets.chat.adapter.OperatorStatusItem;
import com.glia.widgets.chat.adapter.ReceiveMessageItem;
import com.glia.widgets.chat.adapter.SendMessageItem;
import com.glia.widgets.head.ChatHeadsController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.ChatHeadInput;
import com.glia.widgets.model.DialogsState;
import com.glia.widgets.model.GliaChatRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepositoryCallback;
import com.glia.widgets.model.MinimizeHandler;
import com.glia.widgets.view.DialogOfferType;

import java.util.ArrayList;
import java.util.List;

public class ChatController {

    private static final int CALL_TIMER_DELAY = 1000;
    private static final int CALL_TIMER_TICKER_VALUE = 1000;

    private ChatViewCallback viewCallback;
    private ChatGliaCallback gliaCallback;
    private MediaUpgradeOfferRepositoryCallback mediaUpgradeOfferRepositoryCallback;
    private TimeCounter.FormattedTimerStatusListener timerStatusListener;
    private MinimizeHandler.OnMinimizeCalledListener minimizeCalledListener;
    private final GliaChatRepository repository;
    private final MediaUpgradeOfferRepository mediaUpgradeOfferRepository;
    private final TimeCounter callTimer;
    private final MinimizeHandler minimizeHandler;
    private final ChatHeadsController chatHeadsController;

    private final String TAG = "ChatController";
    private volatile ChatState chatState;
    private volatile DialogsState dialogsState;
    private boolean isNavigationPending;

    public ChatController(GliaChatRepository gliaChatRepository,
                          MediaUpgradeOfferRepository mediaUpgradeOfferRepository,
                          TimeCounter callTimer,
                          ChatViewCallback viewCallback,
                          MinimizeHandler minimizeHandler,
                          ChatHeadsController chatHeadsController) {
        Logger.d(TAG, "constructor");
        this.viewCallback = viewCallback;
        this.chatState = new ChatState.Builder()
                .setQueueTicketId(null)
                .setHistoryLoaded(false)
                .setOperatorName(null)
                .setCompanyName(null)
                .setQueueId(null)
                .setContextUrl(null)
                .setIsVisible(false)
                .setIntegratorChatStarted(false)
                .setOverlaysPermissionDialogShown(false)
                .setChatItems(new ArrayList<>())
                .setLastTypedText("")
                .createChatState();
        this.dialogsState = new DialogsState.NoDialog();
        this.repository = gliaChatRepository;
        this.mediaUpgradeOfferRepository = mediaUpgradeOfferRepository;
        this.callTimer = callTimer;
        this.minimizeHandler = minimizeHandler;
        this.chatHeadsController = chatHeadsController;
        isNavigationPending = false;
    }

    public void initChat(String companyName,
                         String queueId,
                         String contextUrl,
                         boolean useOverlays,
                         boolean isConfigurationChange,
                         UiTheme uiTheme,
                         boolean hasOverlayPermissions
    ) {
        chatHeadsController.setHasOverlayPermissions(hasOverlayPermissions);
        if (!isConfigurationChange) {
            chatHeadsController.onNavigatedToChat(
                    new ChatHeadInput(
                            chatState.companyName,
                            chatState.queueId,
                            chatState.contextUrl,
                            uiTheme
                    ));
        }
        if (chatState.integratorChatStarted || dialogsState.showingChatEnderDialog()) {
            return;
        }
        emitViewState(chatState.queueingStarted(companyName, queueId, contextUrl));
        chatHeadsController.setUseOverlays(useOverlays);
        initControllerCallback();
        initMediaUpgradeCallback();
        initMinimizeCallback();
        repository.init(gliaCallback, queueId, contextUrl);
        mediaUpgradeOfferRepository.addCallback(mediaUpgradeOfferRepositoryCallback);
        minimizeHandler.addListener(minimizeCalledListener);
    }

    private void initMinimizeCallback() {
        this.minimizeCalledListener = () -> onDestroy(true);
    }

    private synchronized void emitViewState(ChatState state) {
        if (setState(state) && viewCallback != null) {
            Logger.d(TAG, "Emit state:\n" + state.toString());
            viewCallback.emitState(chatState);
        }
    }

    private synchronized void emitChatItems(ChatState state) {
        if (setState(state) && viewCallback != null) {
            Logger.d(TAG, "Emit chat items:\n" + state.chatItems.toString() +
                    "\n(State): " + state.toString());
            viewCallback.emitItems(state.chatItems);
        }
    }

    private synchronized void emitDialogState(DialogsState state) {
        if (setDialogState(state) && viewCallback != null) {
            Logger.d(TAG, "Emit dialog state:\n" + dialogsState.toString());
            viewCallback.emitDialog(dialogsState);
        }
    }

    public void onDestroy(boolean retain) {
        Logger.d(TAG, "onDestroy, retain:" + retain);
        destroyView();
        viewCallback = null;
        if (!retain) {
            repository.onDestroy();
            gliaCallback = null;
            mediaUpgradeOfferRepository.stopAll();
            mediaUpgradeOfferRepositoryCallback = null;
            timerStatusListener = null;
            callTimer.clear();
            minimizeCalledListener = null;
            minimizeHandler.clear();
        }
    }

    public void sendMessagePreview(String message) {
        Logger.d(TAG, "Send preview: " + message);
        emitViewState(chatState.chatInputChanged(message));
        repository.sendMessagePreview(message);
    }

    public boolean sendMessage(String message) {
        Logger.d(TAG, "Send MESSAGE: " + message);
        boolean valid = isMessageValid(message);
        if (valid) {
            Logger.d(TAG, "Send MESSAGE valid! : " + message);
            repository.sendMessage(message);
        }
        return valid;
    }

    public void show() {
        Logger.d(TAG, "show");
        if (!chatState.isVisible) {
            emitViewState(chatState.changeVisibility(true));
        }
    }

    public void onBackArrowClicked() {
        Logger.d(TAG, "onBackArrowClicked");
        emitViewState(chatState.changeVisibility(false));
        chatHeadsController.onBackButtonPressed(GliaWidgets.CHAT_ACTIVITY, false, null);
    }

    public void noMoreOperatorsAvailableDismissed() {
        Logger.d(TAG, "noMoreOperatorsAvailableDismissed");
        stop();
        dismissDialogs();
        chatHeadsController.chatEndedByUser();
    }

    public void unexpectedErrorDialogDismissed() {
        Logger.d(TAG, "unexpectedErrorDialogDismissed");
        stop();
        dismissDialogs();
        chatHeadsController.chatEndedByUser();
    }

    public void endEngagementDialogYesClicked() {
        Logger.d(TAG, "endEngagementDialogYesClicked");
        stop();
        dismissDialogs();
        chatHeadsController.chatEndedByUser();
    }

    public void endEngagementDialogDismissed() {
        Logger.d(TAG, "endEngagementDialogDismissed");
        dismissDialogs();
    }

    public void leaveChatClicked() {
        Logger.d(TAG, "leaveChatClicked");
        showExitChatDialog();
    }

    public void leaveChatQueueClicked() {
        Logger.d(TAG, "leaveChatQueueClicked");
        showExitQueueDialog();
    }

    public boolean isChatVisible() {
        return chatState.isVisible;
    }

    public void setViewCallback(ChatViewCallback chatViewCallback) {
        Logger.d(TAG, "setViewCallback");
        this.viewCallback = chatViewCallback;
        viewCallback.emitState(chatState);
        viewCallback.emitItems(chatState.chatItems);
        viewCallback.emitDialog(dialogsState);
        viewCallback.setLastTypedText(chatState.lastTypedText);
        if (isNavigationPending) {
            viewCallback.navigateToCall();
        }
    }

    public void onResume(boolean hasOverlaysPermission) {
        Logger.d(TAG, "onResume: " + hasOverlaysPermission);
        if (!hasOverlaysPermission && !chatState.overlaysPermissionDialogShown) {
            showOverlayPermissionsDialog();
        }
        chatHeadsController.setHasOverlayPermissions(hasOverlaysPermission);
        emitViewState(chatState.changeVisibility(chatState.integratorChatStarted));
    }

    public void overlayPermissionsDialogDismissed() {
        Logger.d(TAG, "overlayPermissionsDialogDismissed");
        emitViewState(chatState.drawOverlayPermissionsDialogShown());
        emitDialogState(new DialogsState.NoDialog());
    }

    public void acceptUpgradeOfferClicked(MediaUpgradeOffer offer) {
        Logger.d(TAG, "upgradeToAudioClicked");
        mediaUpgradeOfferRepository.acceptOffer(offer, MediaUpgradeOfferRepository.Submitter.CHAT);
        emitDialogState(new DialogsState.NoDialog());
    }

    public void declineUpgradeOfferClicked(MediaUpgradeOffer offer) {
        Logger.d(TAG, "closeUpgradeDialogClicked");
        mediaUpgradeOfferRepository.declineOffer(offer, MediaUpgradeOfferRepository.Submitter.CHAT);
        emitDialogState(new DialogsState.NoDialog());
    }

    public void navigateToCallSuccess() {
        Logger.d(TAG, "navigateToCallSuccess");
        isNavigationPending = false;
    }

    private synchronized boolean setState(ChatState state) {
        if (this.chatState.equals(state)) return false;
        this.chatState = state;
        return true;
    }

    private synchronized boolean setDialogState(DialogsState dialogsState) {
        if (this.dialogsState.equals(dialogsState)) return false;
        this.dialogsState = dialogsState;
        return true;
    }

    private void initControllerCallback() {
        if (gliaCallback != null) return;
        gliaCallback = new ChatGliaCallback() {
            @Override
            public void queueForEngagementStart() {
                Logger.d(TAG, "queueForEngagementStart");
                viewInitQueueing();
            }

            @Override
            public void queueForEngangmentSuccess() {
                Logger.d(TAG, "queueForEngagementSuccess");
                viewInitQueueing();
            }

            @Override
            public void queueForTicketSuccess(String ticketId) {
                Logger.d(TAG, "queueForTicketSuccess");
                emitViewState(chatState.queueTicketSuccess(ticketId));
            }

            @Override
            public void engagementEndedByOperator() {
                Logger.d(TAG, "engagementEndedByOperator");
                stop();
                emitViewState(chatState.changeVisibility(false));
                if (!isDialogShowing()) {
                    showNoMoreOperatorsAvailableDialog();
                }
            }

            @Override
            public void engagementSuccess(OmnicoreEngagement engagement) {
                Logger.d(TAG, "engagementSuccess");
                String operatorProfileImgUrl = null;
                try {
                    operatorProfileImgUrl = engagement.getOperator().getPicture().getURL().get();
                } catch (Exception e) {
                }
                operatorOnlineStartChatUi(engagement.getOperator().getName(), operatorProfileImgUrl);
                if (!chatState.historyLoaded) {
                    repository.loadHistory();
                }
            }

            @Override
            public void messageDelivered(VisitorMessage visitorMessage) {
                Logger.d(TAG, "messageDelivered: " + visitorMessage.toString());
                List<ChatItem> currentChatItems = new ArrayList<>(chatState.chatItems);
                changeDeliveredIndex(currentChatItems, visitorMessage);
                emitChatItems(chatState.changeItems(currentChatItems));
            }

            @Override
            public void newOperatorMediaState(OperatorMediaState operatorMediaState) {
                Logger.d(TAG, "newOperatorMediaState: " + operatorMediaState.toString());
            }

            @Override
            public void onMessage(ChatMessage message) {
                Logger.d(TAG, "onMessage: " + message.getContent());
                List<ChatItem> items = new ArrayList<>(chatState.chatItems);
                appendMessageItem(items, message);
                emitChatItems(chatState.changeItems(items));
            }

            @Override
            public synchronized void chatHistoryLoaded(ChatMessage[] messages, Throwable error) {
                Logger.d(TAG, "chatHistoryLoaded");
                if (error != null && (messages == null || messages.length == 0)) {
                    Logger.e(TAG, "chatHistoryLoaded error");
                    this.error(error);
                }
                List<ChatItem> items = new ArrayList<>(chatState.chatItems);
                if (messages != null) {
                    for (ChatMessage message : messages) {
                        appendHistoryChatItem(items, message);
                    }
                    // If history added. Add name operator name after history.
                    if (items.size() > 1) {
                        items.add(items.get(0));
                        items.remove(0);
                    }
                }
                emitChatItems(chatState.historyLoaded(items));
                repository.initMessaging();
                mediaUpgradeOfferRepository.startListening();
            }

            @Override
            public void error(GliaException exception) {
                Logger.e(TAG, exception.toString());
                showUnexpectedErrorDialog();
                emitViewState(chatState.stop());
                emitViewState(chatState.changeVisibility(false));
            }

            @Override
            public void error(Throwable throwable) {
                Logger.e(TAG, throwable.toString());
                showUnexpectedErrorDialog();
                emitViewState(chatState.stop());
                emitViewState(chatState.changeVisibility(false));
            }
        };
    }

    private void initMediaUpgradeCallback() {
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
                Logger.d(TAG, "upgradeOfferChoiceSubmitSuccess, offer: " +
                        offer.toString() + ", submitter: " + submitter.toString());
                MediaUpgradeStartedTimerItem.Type type = null;
                if (offer.video == MediaDirection.NONE && offer.audio == MediaDirection.TWO_WAY) {
                    Logger.d(TAG, "audioUpgradeAcceptSuccess");
                    type = MediaUpgradeStartedTimerItem.Type.AUDIO;
                } else if (offer.video == MediaDirection.ONE_WAY || offer.video == MediaDirection.TWO_WAY) {
                    Logger.d(TAG, "videoUpgradeAcceptSuccess");
                    type = MediaUpgradeStartedTimerItem.Type.VIDEO;
                }
                if (type != null) {
                    if (chatState.isMediaUpgradeStarted()) {
                        upgradeMediaItem(type);
                    } else {
                        startTimer(type);
                    }
                    if (submitter == MediaUpgradeOfferRepository.Submitter.CHAT) {
                        if (viewCallback != null) {
                            Logger.d(TAG, "navigateToCall");
                            viewCallback.navigateToCall();
                        }
                        isNavigationPending = true;
                    }
                }
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
    }

    private void viewInitQueueing() {
        if (!chatState.isOperatorOnline()) {
            List<ChatItem> items = new ArrayList<>();
            items.add(OperatorStatusItem.QueueingStatusItem(chatState.companyName));
            emitViewState(chatState.initQueueing());
            emitChatItems(chatState.changeItems(items));
        }
    }

    private void destroyView() {
        if (viewCallback != null) {
            Logger.d(TAG, "destroyingView");
            viewCallback.destroyView();
        }
    }

    private void operatorOnlineStartChatUi(String operatorName, String profileImgUrl) {
        if (!chatState.isOperatorOnline()) {
            List<ChatItem> items = new ArrayList<>();
            emitViewState(chatState.engagementStarted(operatorName, profileImgUrl));
            items.add(OperatorStatusItem.OperatorFoundStatusItem(
                    chatState.companyName,
                    chatState.getFormattedOperatorName(),
                    profileImgUrl));
            emitChatItems(chatState.changeItems(items));
        }
    }

    private void stop() {
        Logger.d(TAG, "Stop, engagement ended");
        repository.stop(chatState.queueTicketId);
        mediaUpgradeOfferRepository.stopAll();
        emitViewState(chatState.stop());
    }

    private void appendHistoryChatItem(List<ChatItem> currentChatItems, ChatMessage message) {
        if (message.getSender() == Chat.Participant.VISITOR) {
            currentChatItems.add(new SendMessageItem(SendMessageItem.HISTORY_ID, false, message.getContent()));
        } else if (message.getSender() == Chat.Participant.OPERATOR) {
            changeLastOperatorMessages(currentChatItems, message);
        }
    }

    private void appendMessageItem(List<ChatItem> currentChatItems, ChatMessage message) {
        if (message.getSender() == Chat.Participant.VISITOR) {
            appendSentMessage(currentChatItems, message);
        } else if (message.getSender() == Chat.Participant.OPERATOR) {
            changeLastOperatorMessages(currentChatItems, message);
        }
    }

    private void appendSentMessage(List<ChatItem> items, ChatMessage message) {
        items.add(new SendMessageItem(message.getId(), false, message.getContent()));
    }

    private void changeDeliveredIndex(List<ChatItem> currentChatItems, VisitorMessage message) {
        for (int i = currentChatItems.size() - 1; i >= 0; i--) {
            if (currentChatItems.get(i) instanceof SendMessageItem) {
                SendMessageItem item = (SendMessageItem) currentChatItems.get(i);
                if (item.getId().equals(SendMessageItem.HISTORY_ID)) {
                    // we reached the history items no point in going searching further
                    break;
                } else if (item.getId().equals(message.getId())) {
                    // the visitormessage. show delivered for it.
                    currentChatItems.remove(i);
                    currentChatItems.add(i,
                            new SendMessageItem(message.getId(), true, message.getContent()));
                } else if (item.isShowDelivered()) {
                    // remove all other delivered references
                    currentChatItems.remove(i);
                    currentChatItems.add(i, new SendMessageItem(item.getId(),
                            false,
                            item.getMessage()));
                }
            }
        }
    }

    private void changeLastOperatorMessages(List<ChatItem> currentChatItems, ChatMessage message) {
        List<String> messages;
        if (currentChatItems.get(currentChatItems.size() - 1) instanceof ReceiveMessageItem) {
            ReceiveMessageItem lastItemInView = (ReceiveMessageItem) currentChatItems.get(currentChatItems.size() - 1);
            currentChatItems.remove(lastItemInView);
            messages = lastItemInView.getMessages();
        } else {
            messages = new ArrayList<>();
        }
        messages.add(message.getContent());
        currentChatItems.add(new ReceiveMessageItem(message.getId(), messages, chatState.operatorProfileImgUrl));
    }

    private boolean isMessageValid(String message) {
        return message.length() > 0;
    }

    private void dismissDialogs() {
        Logger.d(TAG, "Dismiss dialogs");
        emitDialogState(new DialogsState.NoDialog());
    }

    private void showExitQueueDialog() {
        if (!isDialogShowing()) {
            emitDialogState(new DialogsState.ExitQueueDialog());
        }
    }

    private void showExitChatDialog() {
        if (!isDialogShowing() && chatState.isOperatorOnline()) {
            emitDialogState(new DialogsState.EndEngagementDialog(chatState.getFormattedOperatorName()));
        }
    }

    private void showUpgradeAudioDialog(MediaUpgradeOffer mediaUpgradeOffer) {
        if (!isDialogShowing() && chatState.isOperatorOnline()) {
            emitDialogState(new DialogsState.UpgradeDialog(
                    new DialogOfferType.AudioUpgradeOffer(
                            mediaUpgradeOffer,
                            chatState.getFormattedOperatorName()
                    )));
        }
    }

    private void showUpgradeVideoDialog2Way(MediaUpgradeOffer mediaUpgradeOffer) {
        if (!isDialogShowing() && chatState.isOperatorOnline()) {
            emitDialogState(new DialogsState.UpgradeDialog(
                    new DialogOfferType.VideoUpgradeOffer2Way(
                            mediaUpgradeOffer,
                            chatState.getFormattedOperatorName()
                    )));
        }
    }

    private void showUpgradeVideoDialog1Way(MediaUpgradeOffer mediaUpgradeOffer) {
        if (!isDialogShowing() && chatState.isOperatorOnline()) {
            emitDialogState(new DialogsState.UpgradeDialog(
                    new DialogOfferType.VideoUpgradeOffer1Way(
                            mediaUpgradeOffer,
                            chatState.getFormattedOperatorName()
                    )));
        }
    }

    private void showNoMoreOperatorsAvailableDialog() {
        if (!isDialogShowing()) {
            emitDialogState(new DialogsState.NoMoreOperatorsDialog());
        }
    }

    private void showUnexpectedErrorDialog() {
        if (!isDialogShowing()) {
            emitDialogState(new DialogsState.UnexpectedErrorDialog());
        }
    }

    private void showOverlayPermissionsDialog() {
        if (!isDialogShowing()) {
            emitDialogState(new DialogsState.OverlayPermissionsDialog());
        }
    }

    private boolean isDialogShowing() {
        return !(dialogsState instanceof DialogsState.NoDialog);
    }

    private void startTimer(MediaUpgradeStartedTimerItem.Type type) {
        Logger.d(TAG, "startTimer");
        List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
        MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem =
                new MediaUpgradeStartedTimerItem(type, Utils.toMmSs(0));
        newItems.add(mediaUpgradeStartedTimerItem);
        emitChatItems(chatState.changeTimerItem(newItems, mediaUpgradeStartedTimerItem));
        createNewTimerCallback();
        callTimer.addFormattedValueListener(timerStatusListener);
        callTimer.startNew(CALL_TIMER_DELAY, CALL_TIMER_TICKER_VALUE);
    }

    private void upgradeMediaItem(MediaUpgradeStartedTimerItem.Type type) {
        Logger.d(TAG, "upgradeMediaItem");
        List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
        MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem =
                new MediaUpgradeStartedTimerItem(type, chatState.mediaUpgradeStartedTimerItem.time);
        newItems.remove(chatState.mediaUpgradeStartedTimerItem);
        newItems.add(mediaUpgradeStartedTimerItem);
        emitChatItems(chatState.changeTimerItem(newItems, mediaUpgradeStartedTimerItem));
    }

    private void createNewTimerCallback() {
        if (timerStatusListener != null) {
            callTimer.removeFormattedValueListener(timerStatusListener);
        }
        timerStatusListener = new TimeCounter.FormattedTimerStatusListener() {
            @Override
            public void onNewTimerValue(String formatedValue) {
                if (chatState.isMediaUpgradeStarted()) {
                    int index = chatState.chatItems.indexOf(chatState.mediaUpgradeStartedTimerItem);
                    if (index != -1) {
                        List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
                        MediaUpgradeStartedTimerItem.Type type = chatState.mediaUpgradeStartedTimerItem.type;
                        newItems.remove(index);
                        MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem =
                                new MediaUpgradeStartedTimerItem(type, formatedValue);
                        newItems.add(index, mediaUpgradeStartedTimerItem);
                        emitChatItems(chatState.changeTimerItem(newItems, mediaUpgradeStartedTimerItem));
                    }
                }
            }

            @Override
            public void onCancel() {
                if (chatState.isMediaUpgradeStarted() &&
                        chatState.chatItems.contains(chatState.mediaUpgradeStartedTimerItem)) {
                    List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
                    newItems.remove(chatState.mediaUpgradeStartedTimerItem);
                    emitChatItems(chatState.changeTimerItem(newItems, null));
                }
            }
        };
    }
}
