package com.glia.widgets.chat;

import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.MessageAttachment;
import com.glia.androidsdk.chat.SingleChoiceAttachment;
import com.glia.androidsdk.chat.SingleChoiceOption;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.androidsdk.comms.MediaDirection;
import com.glia.androidsdk.comms.MediaUpgradeOffer;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.Constants;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatItem;
import com.glia.widgets.chat.adapter.MediaUpgradeStartedTimerItem;
import com.glia.widgets.chat.adapter.OperatorStatusItem;
import com.glia.widgets.chat.adapter.ReceiveMessageItem;
import com.glia.widgets.chat.adapter.ReceiveMessageItemMessage;
import com.glia.widgets.chat.adapter.SendMessageItem;
import com.glia.widgets.dialog.DialogController;
import com.glia.widgets.head.ChatHeadsController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.ChatHeadInput;
import com.glia.widgets.model.GliaChatRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepositoryCallback;
import com.glia.widgets.model.MessagesNotSeenHandler;
import com.glia.widgets.model.MinimizeHandler;
import com.glia.widgets.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.notification.domain.ShowAudioCallNotificationUseCase;
import com.glia.widgets.notification.domain.ShowVideoCallNotificationUseCase;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final MessagesNotSeenHandler messagesNotSeenHandler;

    private final DialogController dialogController;

    private final ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase;
    private final ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase;
    private final RemoveCallNotificationUseCase removeCallNotificationUseCase;

    private final String TAG = "ChatController";
    private volatile ChatState chatState;

    private boolean isNavigationPending;

    public ChatController(GliaChatRepository gliaChatRepository,
                          MediaUpgradeOfferRepository mediaUpgradeOfferRepository,
                          TimeCounter callTimer,
                          ChatViewCallback viewCallback,
                          MinimizeHandler minimizeHandler,
                          ChatHeadsController chatHeadsController,
                          DialogController dialogController,
                          MessagesNotSeenHandler messagesNotSeenHandler,
                          ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase,
                          ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase,
                          RemoveCallNotificationUseCase removeCallNotificationUseCase
    ) {
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
                .setChatInputMode(ChatInputMode.DISABLED)
                .setIsChatInBottom(true)
                .setMessagesNotSeen(0)
                .createChatState();
        this.repository = gliaChatRepository;
        this.mediaUpgradeOfferRepository = mediaUpgradeOfferRepository;
        this.callTimer = callTimer;
        this.minimizeHandler = minimizeHandler;
        this.chatHeadsController = chatHeadsController;
        this.dialogController = dialogController;
        this.messagesNotSeenHandler = messagesNotSeenHandler;
        isNavigationPending = false;

        this.showAudioCallNotificationUseCase = showAudioCallNotificationUseCase;
        this.showVideoCallNotificationUseCase = showVideoCallNotificationUseCase;
        this.removeCallNotificationUseCase = removeCallNotificationUseCase;
    }

    public void initChat(String companyName,
                         String queueId,
                         String contextUrl,
                         boolean enableChatHeads,
                         boolean useOverlays,
                         boolean isConfigurationChange,
                         UiTheme uiTheme,
                         boolean hasOverlayPermissions
    ) {
        chatHeadsController.setHasOverlayPermissions(hasOverlayPermissions);
        chatHeadsController.setEnableChatHeads(enableChatHeads);
        if (!isConfigurationChange) {
            chatHeadsController.onNavigatedToChat(
                    new ChatHeadInput(
                            chatState.companyName,
                            chatState.queueId,
                            chatState.contextUrl,
                            uiTheme
                    ));
            messagesNotSeenHandler.onNavigatedToChat();
        }
        if (chatState.integratorChatStarted || dialogController.isShowingChatEnderDialog()) {
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

    public void onDestroy(boolean retain) {
        Logger.d(TAG, "onDestroy, retain:" + retain);
        destroyView();
        viewCallback = null;
        if (!retain) {
            removeCallNotificationUseCase.execute();
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
        messagesNotSeenHandler.chatOnBackClicked();
        chatHeadsController.onBackButtonPressed(Constants.CHAT_ACTIVITY, false);
    }

    public void noMoreOperatorsAvailableDismissed() {
        Logger.d(TAG, "noMoreOperatorsAvailableDismissed");
        stop();
        dialogController.dismissDialogs();
        chatHeadsController.chatEndedByUser();
    }

    public void unexpectedErrorDialogDismissed() {
        Logger.d(TAG, "unexpectedErrorDialogDismissed");
        stop();
        dialogController.dismissDialogs();
        chatHeadsController.chatEndedByUser();
    }

    public void endEngagementDialogYesClicked() {
        Logger.d(TAG, "endEngagementDialogYesClicked");
        stop();
        dialogController.dismissDialogs();
        chatHeadsController.chatEndedByUser();
    }

    public void endEngagementDialogDismissed() {
        Logger.d(TAG, "endEngagementDialogDismissed");
        dialogController.dismissDialogs();
    }

    public void leaveChatClicked() {
        Logger.d(TAG, "leaveChatClicked");
        if (chatState.isOperatorOnline())
            dialogController.showExitChatDialog(chatState.getFormattedOperatorName());
    }

    public void leaveChatQueueClicked() {
        Logger.d(TAG, "leaveChatQueueClicked");
        dialogController.showExitQueueDialog();
    }

    public boolean isChatVisible() {
        return chatState.isVisible;
    }

    public void setViewCallback(ChatViewCallback chatViewCallback) {
        Logger.d(TAG, "setViewCallback");
        this.viewCallback = chatViewCallback;
        viewCallback.emitState(chatState);
        viewCallback.emitItems(chatState.chatItems);
        viewCallback.setLastTypedText(chatState.lastTypedText);

        // always start in bottom
        emitViewState(chatState.isInBottomChanged(true));
        viewCallback.scrollToBottomImmediate();

        if (isNavigationPending) {
            viewCallback.navigateToCall();
        }
    }

    public void onResume(boolean hasOverlaysPermission) {
        Logger.d(TAG, "onResume: " + hasOverlaysPermission);
        chatHeadsController.setHasOverlayPermissions(hasOverlaysPermission);
        if (chatHeadsController.showOverlayPermissionsDialog()
                && !chatState.overlaysPermissionDialogShown) {
            dialogController.showOverlayPermissionsDialog();
        }
        emitViewState(chatState.changeVisibility(chatState.integratorChatStarted));
    }

    public void overlayPermissionsDialogDismissed() {
        Logger.d(TAG, "overlayPermissionsDialogDismissed");
        emitViewState(chatState.drawOverlayPermissionsDialogShown());
        dialogController.dismissDialogs();
    }

    public void acceptUpgradeOfferClicked(MediaUpgradeOffer offer) {
        Logger.d(TAG, "upgradeToAudioClicked");
        messagesNotSeenHandler.chatUpgradeOfferAccepted();
        mediaUpgradeOfferRepository.acceptOffer(offer, MediaUpgradeOfferRepository.Submitter.CHAT);
        dialogController.dismissDialogs();
    }

    public void declineUpgradeOfferClicked(MediaUpgradeOffer offer) {
        Logger.d(TAG, "closeUpgradeDialogClicked");
        mediaUpgradeOfferRepository.declineOffer(offer, MediaUpgradeOfferRepository.Submitter.CHAT);
        dialogController.dismissDialogs();
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
                dialogController.showNoMoreOperatorsAvailableDialog();
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
                Logger.d(TAG, "messageDelivered: " + visitorMessage.toString() +
                        ", id: " + visitorMessage.getId());
                List<ChatItem> currentChatItems = new ArrayList<>(chatState.chatItems);
                changeDeliveredIndex(currentChatItems, visitorMessage);
                emitChatItems(chatState.changeItems(currentChatItems));
            }

            @Override
            public void newOperatorMediaState(OperatorMediaState operatorMediaState) {
                Logger.d(TAG, "newOperatorMediaState: " + operatorMediaState.toString());
                if (operatorMediaState.getVideo() != null) {
                    onOperatorMediaStateVideo(operatorMediaState);
                } else if (operatorMediaState.getAudio() != null) {
                    onOperatorMediaStateAudio(operatorMediaState);
                } else {
                    onOperatorMediaStateUnknown();
                }
            }

            @Override
            public void onMessage(ChatMessage message) {
                Logger.d(TAG, "onMessage: " + message.getContent() +
                        ", id: " + message.getId());
                if (message.getAttachment() != null && message.getAttachment()
                        instanceof SingleChoiceAttachment) {
                    SingleChoiceAttachment attachment =
                            (SingleChoiceAttachment) message.getAttachment();

                    if (attachment.getOptions() != null) {
                        for (SingleChoiceOption option : attachment.getOptions()) {
                            Logger.d(TAG, "onMessage, option text: " + option.getText() +
                                    ", option value: " + option.getValue());
                        }
                    }
                    if (attachment.getSelectedOption() != null) {
                        Logger.d(TAG, "selectedOption: " + attachment.getSelectedOption());
                    }
                }
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
                dialogController.showUnexpectedErrorDialog();
                emitViewState(chatState.stop());
                emitViewState(chatState.changeVisibility(false));
            }

            @Override
            public void error(Throwable throwable) {
                Logger.e(TAG, throwable.toString());
                dialogController.showUnexpectedErrorDialog();
                emitViewState(chatState.stop());
                emitViewState(chatState.changeVisibility(false));
            }
        };
    }

    private void onOperatorMediaStateVideo(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: video");
        showVideoCallNotificationUseCase.execute();
    }

    private void onOperatorMediaStateAudio(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: audio");
        showAudioCallNotificationUseCase.execute();
    }

    private void onOperatorMediaStateUnknown() {
        Logger.d(TAG, "newOperatorMediaState: null");
        removeCallNotificationUseCase.execute();
    }

    private void initMediaUpgradeCallback() {
        mediaUpgradeOfferRepositoryCallback = new MediaUpgradeOfferRepositoryCallback() {
            @Override
            public void newOffer(MediaUpgradeOffer offer) {
                if (offer.video == MediaDirection.NONE && offer.audio == MediaDirection.TWO_WAY) {
                    // audio call
                    Logger.d(TAG, "audioUpgradeRequested");
                    if (chatState.isOperatorOnline())
                        dialogController.showUpgradeAudioDialog(offer, chatState.getFormattedOperatorName());
                } else if (offer.video == MediaDirection.TWO_WAY) {
                    // video call
                    Logger.d(TAG, "2 way videoUpgradeRequested");
                    if (chatState.isOperatorOnline())
                        dialogController.showUpgradeVideoDialog2Way(offer, chatState.getFormattedOperatorName());
                } else if (offer.video == MediaDirection.ONE_WAY) {
                    Logger.d(TAG, "1 way videoUpgradeRequested");
                    if (chatState.isOperatorOnline())
                        dialogController.showUpgradeVideoDialog1Way(offer, chatState.getFormattedOperatorName());
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
                dialogController.dismissDialogs();
            }

            @Override
            public void upgradeOfferChoiceDeclinedSuccess(
                    MediaUpgradeOfferRepository.Submitter submitter
            ) {
                Logger.d(TAG, "upgradeOfferChoiceDeclinedSuccess");
                dialogController.dismissDialogs();
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
        List<ChatItem> items = new ArrayList<>();
        emitViewState(chatState.engagementStarted(operatorName, profileImgUrl));
        items.add(OperatorStatusItem.OperatorFoundStatusItem(
                chatState.companyName,
                chatState.getFormattedOperatorName(),
                profileImgUrl));
        emitChatItems(chatState.changeItems(items));
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
            appendMessagesNotSeen();
            if (message.getAttachment() instanceof SingleChoiceAttachment) {
                SingleChoiceAttachment attachment =
                        ((SingleChoiceAttachment) message.getAttachment());
                emitViewState(
                        chatState.chatInputModeChanged(
                                attachment.getOptions() != null ?
                                        ChatInputMode.SINGLE_CHOICE_CARD :
                                        ChatInputMode.ENABLED
                        ));
            }
        }
    }

    private void appendSentMessage(List<ChatItem> items, ChatMessage message) {
        // do nothing if single choice response
        MessageAttachment attachment = message.getAttachment();
        if (attachment instanceof SingleChoiceAttachment &&
                ((SingleChoiceAttachment) attachment).getSelectedOption() != null) {
            Logger.d(TAG, "Not adding singleChoiceAnswer");
            return;
        }
        items.add(new SendMessageItem(message.getId(), false, message.getContent()));
    }

    private void appendMessagesNotSeen() {
        emitViewState(chatState.messagesNotSeenChanged(
                chatState.isChatInBottom ?
                        0 :
                        chatState.messagesNotSeen + 1));
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
        MessageAttachment attachment = message.getAttachment();
        List<ReceiveMessageItemMessage> messages;
        if (currentChatItems.get(currentChatItems.size() - 1) instanceof ReceiveMessageItem) {
            ReceiveMessageItem lastItemInView = (ReceiveMessageItem) currentChatItems.get(currentChatItems.size() - 1);
            currentChatItems.remove(lastItemInView);
            messages = lastItemInView.getMessages();
        } else {
            messages = new ArrayList<>();
        }
        String imageUrl = null;
        if (attachment instanceof SingleChoiceAttachment) {
            try {
                imageUrl = ((SingleChoiceAttachment) attachment).getImageUrl().get();
            } catch (Exception e) {
            }
        }
        messages.add(new ReceiveMessageItemMessage(
                message.getContent(),
                message.getAttachment() != null &&
                        attachment instanceof SingleChoiceAttachment ?
                        Arrays.asList(((SingleChoiceAttachment) attachment).getOptions()) :
                        null,
                null,
                imageUrl));
        currentChatItems.add(new ReceiveMessageItem(
                message.getId(),
                messages,
                chatState.operatorProfileImgUrl
        ));
    }

    private boolean isMessageValid(String message) {
        return message.length() > 0;
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

    public void singleChoiceOptionClicked(
            String id,
            int indexInList,
            int messageIndex,
            int optionIndex
    ) {
        Logger.d(TAG, "singleChoiceOptionClicked, id: " + id);
        if (indexInList == RecyclerView.NO_POSITION) {
            return;
        }
        ChatItem item = chatState.chatItems.get(indexInList);
        //
        if (item.getId().equals(id)) {
            ReceiveMessageItem choiceCardItem =
                    (ReceiveMessageItem) chatState.chatItems.get(indexInList);
            ReceiveMessageItemMessage receiveMessageItemMessage =
                    choiceCardItem.getMessages().get(messageIndex);
            SingleChoiceOption selectedOption =
                    receiveMessageItemMessage.attachments.get(optionIndex);

            repository.sendMessage(selectedOption.asSingleChoiceResponse());

            ReceiveMessageItemMessage receiveMessageItemMessageWithSelected =
                    new ReceiveMessageItemMessage(
                            receiveMessageItemMessage.content,
                            receiveMessageItemMessage.attachments,
                            optionIndex,
                            receiveMessageItemMessage.imageUrl
                    );
            List<ReceiveMessageItemMessage> messagesWithSelected =
                    new ArrayList<>(choiceCardItem.getMessages());
            messagesWithSelected.remove(messageIndex);
            messagesWithSelected.add(messageIndex, receiveMessageItemMessageWithSelected);
            ReceiveMessageItem choiceCardItemWithSelected =
                    new ReceiveMessageItem(
                            id,
                            messagesWithSelected,
                            choiceCardItem.getOperatorProfileImgUrl()
                    );
            List<ChatItem> modifiedItems = new ArrayList<>(chatState.chatItems);
            modifiedItems.remove(indexInList);
            modifiedItems.add(indexInList, choiceCardItemWithSelected);
            emitChatItems(chatState.changeItems(modifiedItems));
        }
    }

    public void onRecyclerviewPositionChanged(boolean isBottom) {
        emitViewState(chatState.isInBottomChanged(isBottom));
        if (isBottom) {
            Logger.d(TAG, "onRecyclerviewPositionChanged, isBottom!");
            emitViewState(chatState.messagesNotSeenChanged(0));
        }
    }

    public void newMessagesIndicatorClicked() {
        Logger.d(TAG, "newMessagesIndicatorClicked");
        if (viewCallback != null) {
            viewCallback.smoothScrollToBottom();
        }
    }
}
