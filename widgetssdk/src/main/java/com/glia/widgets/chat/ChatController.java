package com.glia.widgets.chat;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.chat.adapter.ChatItem;
import com.glia.widgets.chat.adapter.MediaUpgradeStartedTimerItem;
import com.glia.widgets.chat.adapter.OperatorStatusItem;
import com.glia.widgets.chat.adapter.ReceiveMessageItem;
import com.glia.widgets.chat.adapter.SendMessageItem;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.GliaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatController {

    private ChatViewCallback viewCallback;
    private ChatGliaCallback gliaCallback;
    private GliaRepository repository;

    private final String TAG = "ChatController";
    private volatile ChatState chatState;
    private volatile DialogsState dialogsState;
    private MediaUpgradeStartedTimerItem lastTimerItem = null;
    private final TimerTask timerTask = new TimerTask() {
        int seconds = 0;

        @Override
        public void run() {
            seconds++;
            String time = Utils.toMmSs(seconds);
            if (lastTimerItem != null) {
                int index = chatState.chatItems.indexOf(lastTimerItem);
                if (index != -1) {
                    List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
                    MediaUpgradeStartedTimerItem.Type type = lastTimerItem.type;
                    newItems.remove(index);
                    lastTimerItem = new MediaUpgradeStartedTimerItem(type, time);
                    newItems.add(index, lastTimerItem);
                    emitChatItems(chatState.changeItems(newItems));
                } else {
                    cancel();
                }
            } else {
                cancel();
            }
            Logger.d(TAG, "timer: " + time);
        }

        @Override
        public boolean cancel() {
            seconds = 0;
            return super.cancel();
        }
    };
    private final Timer timer = new Timer();

    public ChatController(ChatViewCallback viewCallback) {
        this.viewCallback = viewCallback;
        this.chatState = new ChatState.Builder()
                .setUseFloatingChatHeads(false)
                .setQueueTicketId(null)
                .setHistoryLoaded(false)
                .setOperatorName(null)
                .setCompanyName(null)
                .setQueueId(null)
                .setContextUrl(null)
                .setIsVisible(false)
                .setIntegratorChatStarted(false)
                .setHasOverlayPermissions(false)
                .setOverlaysPermissionDialogShown(false)
                .setChatItems(new ArrayList<>())
                .createChatState();
        this.dialogsState = new DialogsState.NoDialog();
    }

    public void initChat(GliaRepository gliaRepository,
                         String companyName,
                         String queueId,
                         String contextUrl,
                         boolean useChatHeads) {
        Logger.d(TAG, "initChat");
        if (useChatHeads && chatState.hasOverlayPermissions) {
            handleFloatingChatheads(false);
        }
        if (chatState.integratorChatStarted) {
            return;
        }
        this.repository = gliaRepository;
        emitViewState(chatState.queueingStarted(useChatHeads, companyName, queueId, contextUrl));
        initControllerCallback();
        repository.init(gliaCallback, queueId, contextUrl);
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
        viewCallback = null;
        if (!retain) {
            if (chatState.useFloatingChatHeads && repository != null) {
                repository.onDestroyView();
            }
            if (chatState.useFloatingChatHeads) {
                gliaCallback = null;
            }
            timerTask.cancel();
            timer.cancel();
        }
    }

    public void sendMessagePreview(String message) {
        Logger.d(TAG, "Send preview: " + message);
        repository.sendMessagePreview(message);
    }

    public void sendMessage(String message) {
        Logger.d(TAG, "Send MESSAGE: " + message);
        if (isMessageValid(message)) {
            Logger.d(TAG, "Send MESSAGE valid! : " + message);
            repository.sendMessage(message);
        }
    }

    public void show() {
        Logger.d(TAG, "show");
        if (!chatState.isVisible) {
            emitViewState(chatState.show());
        }
    }

    public void onBackArrowClicked() {
        Logger.d(TAG, "onBackArrowClicked");
        if (!chatState.useFloatingChatHeads) {
            emitViewState(chatState.hide());
        } else if (chatState.hasOverlayPermissions) {
            handleFloatingChatheads(true);
        }
    }

    public void noMoreOperatorsAvailableDismissed() {
        Logger.d(TAG, "noMoreOperatorsAvailableDismissed");
        stop(false, false);
        dismissDialogs();
    }

    public void unexpectedErrorDialogDismissed() {
        Logger.d(TAG, "unexpectedErrorDialogDismissed");
        stop(false, false);
        dismissDialogs();
    }

    public void endEngagementDialogYesClicked() {
        Logger.d(TAG, "endEngagementDialogYesClicked");
        stop(false, false);
        dismissDialogs();
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
    }

    public void onResume(boolean hasOverlaysPermission) {
        Logger.d(TAG, "onResume");
        if (!hasOverlaysPermission && !chatState.overlaysPermissionDialogShown) {
            showOverlayPermissionsDialog();
        }
        emitViewState(chatState.drawOverlaysPermissionChanged(hasOverlaysPermission));
    }

    public void overlayPermissionsDialogDismissed() {
        Logger.d(TAG, "overlayPermissionsDialogDismissed");
        emitDialogState(new DialogsState.NoDialog());
    }

    public void upgradeToAudioClicked() {
        Logger.d(TAG, "upgradeToAudioClicked");
        repository.acceptUpgradeOffer();
        emitDialogState(new DialogsState.NoDialog());
    }

    public void closeUpgradeDialogClicked() {
        Logger.d(TAG, "closeUpgradeDialogClicked");
        repository.declineOffer();
        emitDialogState(new DialogsState.NoDialog());
    }

    public String getCompanyName() {
        return chatState.companyName;
    }

    public String getQueueId() {
        return chatState.queueId;
    }

    public String getContextUrl() {
        return chatState.contextUrl;
    }

    public boolean isStarted() {
        return chatState.integratorChatStarted;
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
                stop(true, chatState.useFloatingChatHeads);
            }

            @Override
            public void engagementEnded(boolean showDialog) {
                Logger.d(TAG, "engagementEnded");
                if (showDialog) {
                    showNoMoreOperatorsAvailableDialog();
                } else {
                    emitViewState(chatState.stop(false));
                }
            }

            @Override
            public void engagementSuccess(OmnicoreEngagement engagement) {
                Logger.d(TAG, "engagementSuccess");
                operatorOnlineStartChatUi(engagement.getOperator().getName());
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
            }

            @Override
            public void audioUpgradeRequested() {
                Logger.d(TAG, "audioUpgradeRequested");
                showUpgradeDialog();
            }

            @Override
            public void audioUpgradeOfferChoiceSubmitSuccess() {
                Logger.d(TAG, "audioUpgradeAcceptSuccess");
                startTimer(MediaUpgradeStartedTimerItem.Type.AUDIO);
            }

            @Override
            public void error(GliaException exception) {
                Logger.e(TAG, exception.toString());
                showUnexpectedErrorDialog();
                emitViewState(chatState.stop(chatState.useFloatingChatHeads));
            }

            @Override
            public void error(Throwable throwable) {
                Logger.e(TAG, throwable.toString());
                showUnexpectedErrorDialog();
                emitViewState(chatState.stop(chatState.useFloatingChatHeads));
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

    private void operatorOnlineStartChatUi(String operatorName) {
        if (!chatState.isOperatorOnline()) {
            List<ChatItem> items = new ArrayList<>();
            emitViewState(chatState.engagementStarted(operatorName));
            items.add(OperatorStatusItem.OperatorFoundStatusItem(chatState.companyName, chatState.getFormattedOperatorName()));
            emitChatItems(chatState.changeItems(items));
        }
    }

    private void stop(boolean showDialog, boolean isVisible) {
        repository.stop(chatState.queueTicketId, showDialog);
        emitViewState(chatState.stop(isVisible));
        if (showDialog && !isDialogShowing()) {
            showNoMoreOperatorsAvailableDialog();
        }
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
        currentChatItems.add(new ReceiveMessageItem(message.getId(), messages));
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

    private void showUpgradeDialog() {
        if (!isDialogShowing() && chatState.isOperatorOnline()) {
            emitDialogState(new DialogsState.UpgradeAudioDialog(chatState.getFormattedOperatorName()));
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

    private void handleFloatingChatheads(boolean show) {
        if (viewCallback != null && chatState.useFloatingChatHeads) {
            Logger.d(TAG, "handleFloatingChatHeads, show: " + show);
            viewCallback.handleFloatingChatHead(show);
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
        List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
        lastTimerItem = new MediaUpgradeStartedTimerItem(type, Utils.toMmSs(0));
        newItems.add(lastTimerItem);
        emitChatItems(chatState.changeItems(newItems));
        int timerDelay = 0;
        int timer1SecInterval = 1000;
        timer.schedule(timerTask, timerDelay, timer1SecInterval);
    }
}
