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
import com.glia.widgets.chat.adapter.OperatorMessageItem;
import com.glia.widgets.chat.adapter.OperatorStatusItem;
import com.glia.widgets.chat.adapter.VisitorMessageItem;
import com.glia.widgets.dialog.DialogController;
import com.glia.widgets.glia.GliaCancelQueueTicketUseCase;
import com.glia.widgets.glia.GliaEndEngagementUseCase;
import com.glia.widgets.glia.GliaLoadHistoryUseCase;
import com.glia.widgets.glia.GliaOnEngagementEndUseCase;
import com.glia.widgets.glia.GliaOnEngagementUseCase;
import com.glia.widgets.glia.GliaOnMessageUseCase;
import com.glia.widgets.glia.GliaOnOperatorMediaStateUseCase;
import com.glia.widgets.glia.GliaOnQueueTicketUseCase;
import com.glia.widgets.glia.GliaQueueForEngagementUseCase;
import com.glia.widgets.glia.GliaSendMessagePreviewUseCase;
import com.glia.widgets.glia.GliaSendMessageUseCase;
import com.glia.widgets.head.ChatHeadsController;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.TimeCounter;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.ChatHeadInput;
import com.glia.widgets.model.MediaUpgradeOfferRepository;
import com.glia.widgets.model.MediaUpgradeOfferRepositoryCallback;
import com.glia.widgets.model.MessagesNotSeenHandler;
import com.glia.widgets.model.MinimizeHandler;
import com.glia.widgets.model.PermissionType;
import com.glia.widgets.notification.domain.RemoveCallNotificationUseCase;
import com.glia.widgets.notification.domain.ShowAudioCallNotificationUseCase;
import com.glia.widgets.notification.domain.ShowVideoCallNotificationUseCase;
import com.glia.widgets.permissions.CheckIfShowPermissionsDialogUseCase;
import com.glia.widgets.permissions.ResetPermissionsUseCase;
import com.glia.widgets.permissions.UpdateDialogShownUseCase;
import com.glia.widgets.permissions.UpdatePermissionsUseCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatController implements
        GliaLoadHistoryUseCase.Listener,
        GliaQueueForEngagementUseCase.Listener,
        GliaOnEngagementUseCase.Listener,
        GliaOnEngagementEndUseCase.Listener,
        GliaOnMessageUseCase.Listener,
        GliaSendMessageUseCase.Listener,
        GliaOnOperatorMediaStateUseCase.Listener,
        GliaOnQueueTicketUseCase.Listener {

    private static final int CALL_TIMER_DELAY = 1000;
    private static final int CALL_TIMER_TICKER_VALUE = 1000;

    private ChatViewCallback viewCallback;
    private MediaUpgradeOfferRepositoryCallback mediaUpgradeOfferRepositoryCallback;
    private TimeCounter.FormattedTimerStatusListener timerStatusListener;
    private MinimizeHandler.OnMinimizeCalledListener minimizeCalledListener;
    private final MediaUpgradeOfferRepository mediaUpgradeOfferRepository;
    private final TimeCounter callTimer;
    private final MinimizeHandler minimizeHandler;
    private final ChatHeadsController chatHeadsController;
    private final MessagesNotSeenHandler messagesNotSeenHandler;

    private final DialogController dialogController;

    private final ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase;
    private final ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase;
    private final RemoveCallNotificationUseCase removeCallNotificationUseCase;
    private final GliaLoadHistoryUseCase loadHistoryUseCase;
    private final GliaQueueForEngagementUseCase queueForEngagementUseCase;
    private final GliaOnEngagementUseCase getEngagementUseCase;
    private final GliaOnEngagementEndUseCase engagementEndUseCase;
    private final GliaOnMessageUseCase onMessageUseCase;
    private final GliaSendMessagePreviewUseCase sendMessagePreviewUseCase;
    private final GliaSendMessageUseCase sendMessageUseCase;
    private final GliaOnOperatorMediaStateUseCase onOperatorMediaStateUseCase;
    private final GliaCancelQueueTicketUseCase cancelQueueTicketUseCase;
    private final GliaEndEngagementUseCase endEngagementUseCase;
    private final GliaOnQueueTicketUseCase onQueueTicketUseCase;
    private final CheckIfShowPermissionsDialogUseCase checkIfShowPermissionsDialogUseCase;
    private final UpdateDialogShownUseCase updateDialogShownUseCase;
    private final UpdatePermissionsUseCase updatePermissionsUseCase;
    private final ResetPermissionsUseCase resetPermissionsUseCase;

    private final String TAG = "ChatController";
    private volatile ChatState chatState;

    public ChatController(MediaUpgradeOfferRepository mediaUpgradeOfferRepository,
                          TimeCounter callTimer,
                          ChatViewCallback viewCallback,
                          MinimizeHandler minimizeHandler,
                          ChatHeadsController chatHeadsController,
                          DialogController dialogController,
                          MessagesNotSeenHandler messagesNotSeenHandler,
                          ShowAudioCallNotificationUseCase showAudioCallNotificationUseCase,
                          ShowVideoCallNotificationUseCase showVideoCallNotificationUseCase,
                          RemoveCallNotificationUseCase removeCallNotificationUseCase,
                          GliaLoadHistoryUseCase loadHistoryUseCase,
                          GliaQueueForEngagementUseCase queueForEngagementUseCase,
                          GliaOnEngagementUseCase gliaObserveEngagementUseCase,
                          GliaOnEngagementEndUseCase gliaOnEngagementEndUseCase,
                          GliaOnMessageUseCase onMessageUseCase,
                          GliaSendMessagePreviewUseCase gliaSendMessagePreviewUseCase,
                          GliaSendMessageUseCase sendMessageUseCase,
                          GliaOnOperatorMediaStateUseCase onOperatorMediaStateUseCase,
                          GliaCancelQueueTicketUseCase cancelQueueTicketUseCase,
                          GliaEndEngagementUseCase endEngagementUseCase,
                          GliaOnQueueTicketUseCase onQueueTicketUseCase,
                          CheckIfShowPermissionsDialogUseCase checkIfShowPermissionsDialogUseCase,
                          UpdateDialogShownUseCase updateDialogShownUseCase,
                          UpdatePermissionsUseCase updatePermissionsUseCase,
                          ResetPermissionsUseCase resetPermissionsUseCase
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
                .setChatInputMode(ChatInputMode.ENABLED_NO_ENGAGEMENT)
                .setIsChatInBottom(true)
                .setMessagesNotSeen(0)
                .setIsNavigationPending(false)
                .setUnsentMessages(new ArrayList<>())
                .createChatState();
        this.mediaUpgradeOfferRepository = mediaUpgradeOfferRepository;
        this.callTimer = callTimer;
        this.minimizeHandler = minimizeHandler;
        this.chatHeadsController = chatHeadsController;
        this.dialogController = dialogController;
        this.messagesNotSeenHandler = messagesNotSeenHandler;

        this.showAudioCallNotificationUseCase = showAudioCallNotificationUseCase;
        this.showVideoCallNotificationUseCase = showVideoCallNotificationUseCase;
        this.removeCallNotificationUseCase = removeCallNotificationUseCase;
        this.loadHistoryUseCase = loadHistoryUseCase;
        this.queueForEngagementUseCase = queueForEngagementUseCase;
        this.getEngagementUseCase = gliaObserveEngagementUseCase;
        this.engagementEndUseCase = gliaOnEngagementEndUseCase;
        this.onMessageUseCase = onMessageUseCase;
        this.sendMessagePreviewUseCase = gliaSendMessagePreviewUseCase;
        this.sendMessageUseCase = sendMessageUseCase;
        this.onOperatorMediaStateUseCase = onOperatorMediaStateUseCase;
        this.cancelQueueTicketUseCase = cancelQueueTicketUseCase;
        this.endEngagementUseCase = endEngagementUseCase;
        this.onQueueTicketUseCase = onQueueTicketUseCase;
        this.checkIfShowPermissionsDialogUseCase = checkIfShowPermissionsDialogUseCase;
        this.updateDialogShownUseCase = updateDialogShownUseCase;
        this.updatePermissionsUseCase = updatePermissionsUseCase;
        this.resetPermissionsUseCase = resetPermissionsUseCase;
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
        emitViewState(chatState.initChat(companyName, queueId, contextUrl));
        loadHistoryUseCase.execute(this);
        chatHeadsController.setUseOverlays(useOverlays);
        initMediaUpgradeCallback();
        initMinimizeCallback();
        mediaUpgradeOfferRepository.addCallback(mediaUpgradeOfferRepositoryCallback);
        minimizeHandler.addListener(minimizeCalledListener);
    }

    private void queueForEngagement() {
        Logger.d(TAG, "queueForEngagement");
        queueForEngagementUseCase.execute(chatState.queueId, chatState.contextUrl, this);
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
            mediaUpgradeOfferRepository.stopAll();
            mediaUpgradeOfferRepositoryCallback = null;
            timerStatusListener = null;
            callTimer.clear();
            minimizeCalledListener = null;
            minimizeHandler.clear();

            loadHistoryUseCase.unregisterListener(this);
            queueForEngagementUseCase.unregisterListener(this);
            getEngagementUseCase.unregisterListener(this);
            engagementEndUseCase.unregisterListener(this);
            onMessageUseCase.unregisterListener(this);
            sendMessageUseCase.unregisterListener(this);
            onOperatorMediaStateUseCase.unregisterListener(this);
            onQueueTicketUseCase.unregisterListener(this);
            resetPermissionsUseCase.execute();
        }
    }

    public void sendMessagePreview(String message) {
        emitViewState(chatState.chatInputChanged(message));
        if (chatState.isOperatorOnline()) {
            Logger.d(TAG, "Send preview: " + message);
            sendMessagePreviewUseCase.execute(message);
        } else {
            Logger.d(TAG, "Send preview not sending");
        }
    }

    public boolean sendMessage(String message) {
        Logger.d(TAG, "Send MESSAGE: " + message);
        boolean valid = isMessageValid(message);
        if (valid) {
            Logger.d(TAG, "Send MESSAGE valid! : " + message);
            if (chatState.isOperatorOnline()) {
                Logger.d(TAG, "SEND MESSAGE sending");
                sendMessageUseCase.execute(message, this);
            } else {
                appendUnsentMessage(message);
                if (!chatState.engagementRequested) {
                    queueForEngagement();
                }
            }
        }
        return valid;
    }

    private void appendUnsentMessage(String message) {
        Logger.d(TAG, "appendUnsentMessage: " + message);
        List<VisitorMessageItem> unsentMessages = new ArrayList<>(chatState.unsentMessages);
        VisitorMessageItem unsentItem = new VisitorMessageItem(VisitorMessageItem.UNSENT_MESSAGE_ID, false, message);
        unsentMessages.add(unsentItem);
        emitViewState(chatState.changeUnsentMessages(unsentMessages));

        List<ChatItem> currentChatItems = new ArrayList<>(chatState.chatItems);
        currentChatItems.add(unsentItem);
        emitChatItems(chatState.changeItems(currentChatItems));
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
        emitViewState(chatState.changeVisibility(true));
        viewCallback.scrollToBottomImmediate();

        if (chatState.isNavigationPending) {
            viewCallback.navigateToCall();
        }
    }

    public void onResume(boolean hasOverlaysPermission,
                         boolean isCallChannelEnabled,
                         boolean isScreenSharingChannelEnabled) {
        Logger.d(TAG, "onResume\n" +
                "hasOverlayPermissions: " + hasOverlaysPermission +
                ", isCallChannelEnabled:" + isCallChannelEnabled +
                ", isScreenSharingChannelEnabled: " + isScreenSharingChannelEnabled);
        updatePermissionsUseCase.execute(
                hasOverlaysPermission,
                isCallChannelEnabled,
                isScreenSharingChannelEnabled
        );
        if (checkIfShowPermissionsDialogUseCase.execute(PermissionType.OVERLAY, true) &&
                dialogController.isNoDialogShown()) {
            dialogController.showOverlayPermissionsDialog();
            updateDialogShownUseCase.execute(PermissionType.OVERLAY);
        }
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
        emitViewState(chatState.isNavigationPendingChanged(false));
    }

    private synchronized boolean setState(ChatState state) {
        if (this.chatState.equals(state)) return false;
        this.chatState = state;
        return true;
    }

    private void error(String error) {
        Logger.e(TAG, error);
        dialogController.showUnexpectedErrorDialog();
        emitViewState(chatState.stop());
    }

    private void onOperatorMediaStateVideo() {
        Logger.d(TAG, "newOperatorMediaState: video");
        showVideoCallNotificationUseCase.execute();
    }

    private void onOperatorMediaStateAudio() {
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
                        emitViewState(chatState.isNavigationPendingChanged(true));
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
        List<ChatItem> items = new ArrayList<>(chatState.chatItems);
        if (chatState.operatorStatusItem != null) {
            items.remove(chatState.operatorStatusItem);
        }
        OperatorStatusItem operatorStatusItem =
                OperatorStatusItem.QueueingStatusItem(chatState.companyName);
        items.add(operatorStatusItem);
        emitViewState(chatState.queueingStarted(operatorStatusItem));
        emitChatItems(chatState.changeItems(items));
    }

    private void destroyView() {
        if (viewCallback != null) {
            Logger.d(TAG, "destroyingView");
            viewCallback.destroyView();
        }
    }

    private void operatorOnlineStartChatUi(String operatorName, String profileImgUrl) {
        emitViewState(chatState.engagementStarted(operatorName, profileImgUrl));
        List<ChatItem> items = new ArrayList<>(chatState.chatItems);
        if (chatState.operatorStatusItem != null) {
            // remove previous operator status item
            int operatorStatusItemIndex = items.indexOf(chatState.operatorStatusItem);
            items.remove(chatState.operatorStatusItem);
            items.add(operatorStatusItemIndex,
                    OperatorStatusItem.OperatorFoundStatusItem(
                            chatState.companyName,
                            chatState.getFormattedOperatorName(),
                            profileImgUrl));
        } else {
            items.add(OperatorStatusItem.OperatorFoundStatusItem(
                    chatState.companyName,
                    chatState.getFormattedOperatorName(),
                    profileImgUrl));
        }
        emitChatItems(chatState.changeItems(items));
    }

    private void stop() {
        Logger.d(TAG, "Stop, engagement ended");
        if (chatState.queueTicketId != null) {
            cancelQueueTicketUseCase.execute(chatState.queueTicketId);
        }
        endEngagementUseCase.execute();
        mediaUpgradeOfferRepository.stopAll();
        emitViewState(chatState.stop());
    }

    private void appendHistoryChatItem(List<ChatItem> currentChatItems, ChatMessage message) {
        if (message.getSender() == Chat.Participant.VISITOR) {
            currentChatItems.add(new VisitorMessageItem(VisitorMessageItem.HISTORY_ID, false, message.getContent()));
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
        items.add(new VisitorMessageItem(message.getId(), false, message.getContent()));
    }

    private void appendMessagesNotSeen() {
        emitViewState(chatState.messagesNotSeenChanged(
                chatState.isChatInBottom ?
                        0 :
                        chatState.messagesNotSeen + 1));
    }

    private void initGliaEngagementObserving() {
        onQueueTicketUseCase.execute(this);
        getEngagementUseCase.execute(this);
        engagementEndUseCase.execute(this);
    }

    private void changeDeliveredIndex(List<ChatItem> currentChatItems, VisitorMessage message) {
        for (int i = currentChatItems.size() - 1; i >= 0; i--) {
            if (currentChatItems.get(i) instanceof VisitorMessageItem) {
                VisitorMessageItem item = (VisitorMessageItem) currentChatItems.get(i);
                if (item.getId().equals(VisitorMessageItem.HISTORY_ID)) {
                    // we reached the history items no point in going searching further
                    break;
                } else if (item.getId().equals(message.getId())) {
                    // the visitormessage. show delivered for it.
                    currentChatItems.remove(i);
                    currentChatItems.add(i,
                            new VisitorMessageItem(message.getId(), true, message.getContent()));
                } else if (item.isShowDelivered()) {
                    // remove all other delivered references
                    currentChatItems.remove(i);
                    currentChatItems.add(i, new VisitorMessageItem(item.getId(),
                            false,
                            item.getMessage()));
                }
            }
        }
    }

    private void changeLastOperatorMessages(List<ChatItem> currentChatItems, ChatMessage message) {
        MessageAttachment attachment = message.getAttachment();

        if (!currentChatItems.isEmpty() &&
                currentChatItems.get(currentChatItems.size() - 1) instanceof OperatorMessageItem) {
            OperatorMessageItem lastItemInView = (OperatorMessageItem) currentChatItems.get(currentChatItems.size() - 1);
            currentChatItems.remove(lastItemInView);
            currentChatItems.add(new OperatorMessageItem(
                    lastItemInView.getId(),
                    lastItemInView.operatorProfileImgUrl,
                    false,
                    lastItemInView.content,
                    lastItemInView.singleChoiceOptions,
                    lastItemInView.selectedChoiceIndex,
                    lastItemInView.choiceCardImageUrl
            ));
        }
        String imageUrl = null;
        if (attachment instanceof SingleChoiceAttachment) {
            try {
                imageUrl = ((SingleChoiceAttachment) attachment).getImageUrl().get();
            } catch (Exception ignored) {
            }
        }

        List<SingleChoiceOption> singleChoiceOptions = attachment instanceof SingleChoiceAttachment ?
                Arrays.asList(((SingleChoiceAttachment) attachment).getOptions()) :
                null;
        currentChatItems.add(new OperatorMessageItem(
                message.getId(),
                chatState.operatorProfileImgUrl,
                true,
                message.getContent(),
                singleChoiceOptions,
                null,
                imageUrl
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
            int optionIndex
    ) {
        Logger.d(TAG, "singleChoiceOptionClicked, id: " + id);
        if (indexInList == RecyclerView.NO_POSITION) {
            return;
        }
        ChatItem item = chatState.chatItems.get(indexInList);

        if (item.getId().equals(id)) {
            OperatorMessageItem choiceCardItem =
                    (OperatorMessageItem) chatState.chatItems.get(indexInList);
            SingleChoiceOption selectedOption =
                    choiceCardItem.singleChoiceOptions.get(optionIndex);
            sendMessageUseCase.execute(selectedOption.asSingleChoiceResponse(), this);

            OperatorMessageItem choiceCardItemWithSelected =
                    new OperatorMessageItem(
                            id,
                            choiceCardItem.operatorProfileImgUrl,
                            choiceCardItem.showChatHead,
                            choiceCardItem.content,
                            choiceCardItem.singleChoiceOptions,
                            optionIndex,
                            choiceCardItem.choiceCardImageUrl
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

    @Override
    public void historyLoaded(ChatMessage[] messages) {
        Logger.d(TAG, "historyLoaded");
        List<ChatItem> items = new ArrayList<>(chatState.chatItems);
        if (messages != null && messages.length > 0) {
            for (ChatMessage message : messages) {
                appendHistoryChatItem(items, message);
            }
            emitChatItems(chatState.historyLoaded(items));
            initGliaEngagementObserving();
        } else {
            initGliaEngagementObserving();
            queueForEngagement();
        }

    }

    @Override
    public void ticketLoaded(String ticket) {
        Logger.d(TAG, "ticketLoaded");
        emitViewState(chatState.queueTicketSuccess(ticket));
        if (!chatState.engagementRequested) {
            viewInitQueueing();
        }
    }

    @Override
    public void queueForEngagementSuccess() {
        Logger.d(TAG, "queueForEngagementSuccess");
        viewInitQueueing();
    }

    @Override
    public void error(GliaException exception) {
        if (exception != null) {
            error(exception.toString());
        }
    }

    @Override
    public void error(Throwable error) {
        if (error != null) {
            error(error.toString());
        }
    }

    @Override
    public void newEngagementLoaded(OmnicoreEngagement engagement) {
        Logger.d(TAG, "newEngagementLoaded");
        String operatorProfileImgUrl = null;
        try {
            operatorProfileImgUrl = engagement.getOperator().getPicture().getURL().get();
        } catch (Exception ignored) {
        }
        operatorOnlineStartChatUi(engagement.getOperator().getName(), operatorProfileImgUrl);
        onMessageUseCase.execute(this);
        onOperatorMediaStateUseCase.execute(this);
        mediaUpgradeOfferRepository.startListening();
        if (!chatState.unsentMessages.isEmpty()) {
            sendMessageUseCase.execute(chatState.unsentMessages.get(0).getMessage(), this);
            Logger.d(TAG, "unsentMessage sent!");
        }
    }

    @Override
    public void engagementEnded() {
        Logger.d(TAG, "engagementEnded");
        stop();
        emitViewState(chatState.changeVisibility(false));
        dialogController.showNoMoreOperatorsAvailableDialog();
    }

    @Override
    public void onMessage(ChatMessage message) {
        boolean isUnsentMessage = !chatState.unsentMessages.isEmpty() &&
                chatState.unsentMessages.get(0).getMessage().equals(message.getContent());
        Logger.d(TAG, "onMessage: " + message.getContent() +
                ", id: " + message.getId() +
                ", isUnsentMessage: " + isUnsentMessage);
        if (isUnsentMessage) {
            List<VisitorMessageItem> unsentMessages = new ArrayList<>(chatState.unsentMessages);
            VisitorMessageItem currentMessage = unsentMessages.get(0);
            unsentMessages.remove(currentMessage);
            emitViewState(chatState.changeUnsentMessages(unsentMessages));

            List<ChatItem> currentChatItems = new ArrayList<>(chatState.chatItems);
            int currentMessageIndex = currentChatItems.indexOf(currentMessage);
            currentChatItems.remove(currentMessage);
            currentChatItems.add(currentMessageIndex, new VisitorMessageItem(message.getId(), false, message.getContent()));

            // emitting state because no need to change recyclerview items here
            emitViewState(chatState.changeItems(currentChatItems));
            if (!chatState.unsentMessages.isEmpty()) {
                sendMessageUseCase.execute(chatState.unsentMessages.get(0).getMessage(), this);
            }
            return;
        }

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
    public void messageSent(VisitorMessage message, GliaException exception) {
        if (exception != null) {
            Logger.d(TAG, "messageSent exception");
            error(exception);
        }
        if (message != null) {
            Logger.d(TAG, "messageSent: " + message.toString() +
                    ", id: " + message.getId());
            List<ChatItem> currentChatItems = new ArrayList<>(chatState.chatItems);
            changeDeliveredIndex(currentChatItems, message);
            emitChatItems(chatState.changeItems(currentChatItems));
        }
    }

    @Override
    public void onNewOperatorMediaState(OperatorMediaState operatorMediaState) {
        Logger.d(TAG, "newOperatorMediaState: " + operatorMediaState.toString());
        if (operatorMediaState.getVideo() != null) {
            onOperatorMediaStateVideo();
        } else if (operatorMediaState.getAudio() != null) {
            onOperatorMediaStateAudio();
        } else {
            onOperatorMediaStateUnknown();
        }
    }

    public void notificationsDialogDismissed() {
        dialogController.dismissDialogs();
    }
}
