package com.glia.widgets.chat;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.androidsdk.comms.OperatorMediaState;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.chat.adapter.ChatItem;
import com.glia.widgets.chat.adapter.MediaUpgradeStartedTimerItem;
import com.glia.widgets.chat.adapter.OperatorStatusItem;
import com.glia.widgets.chat.adapter.ReceiveMessageItem;
import com.glia.widgets.chat.adapter.SendMessageItem;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.model.DialogsState;
import com.glia.widgets.model.GliaChatRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatController {

    private ChatViewCallback viewCallback;
    private ChatGliaCallback gliaCallback;
    private final GliaChatRepository repository;

    private final String TAG = "ChatController";
    private volatile ChatState chatState;
    private volatile DialogsState dialogsState;
    private boolean isNavigationPending;
    private TimerTask timerTask;
    private Timer timer;

    public ChatController(GliaChatRepository gliaChatRepository, ChatViewCallback viewCallback) {
        Logger.d(TAG, "constructor");
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
        this.repository = gliaChatRepository;
        isNavigationPending = false;
    }

    public void initChat(String companyName,
                         String queueId,
                         String contextUrl,
                         boolean useChatHeads) {
        Logger.d(TAG, "initChat, useChatHeads: " + useChatHeads);
        if (chatState.integratorChatStarted) {
            return;
        }
        emitViewState(chatState.queueingStarted(useChatHeads, companyName, queueId, contextUrl));
        if (useChatHeads && chatState.hasOverlayPermissions) {
            handleFloatingChatheads(false);
        }
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
        if (!retain) {
            destroyView();
        }
        viewCallback = null;
        if (!retain) {
            if (repository != null) {
                repository.onDestroy();
            }
            gliaCallback = null;
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
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
        if (isNavigationPending) {
            viewCallback.navigateToCall();
        }
    }

    public void onResume(boolean hasOverlaysPermission) {
        Logger.d(TAG, "onResume: " + hasOverlaysPermission);
        if (!hasOverlaysPermission && !chatState.overlaysPermissionDialogShown) {
            showOverlayPermissionsDialog();
        }
        chatState.drawOverlayPermissionsDialogShown(hasOverlaysPermission);
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

    public void navigateToCallSuccess() {
        Logger.d(TAG, "navigateToCallSuccess");
        isNavigationPending = false;
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
            public void audioUpgradeOfferChoiceDeniedSuccess() {
                // TODO When Sdk works
            }

            @Override
            public void newOperatorMediaState(OperatorMediaState operatorMediaState) {
                if (operatorMediaState.getAudio() == null && chatState.isMediaUpgradeStarted()
                        && timerTask != null) {
                    timerTask.cancel();
                    timer.cancel();
                    timerTask = null;
                    timer = null;
                }
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
                if (viewCallback != null) {
                    Logger.d(TAG, "navigateToCall");
                    viewCallback.navigateToCall();
                }
                isNavigationPending = true;
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

    private void destroyView() {
        if (viewCallback != null) {
            Logger.d(TAG, "destroyingView");
            viewCallback.destroyView();
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
        MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem =
                new MediaUpgradeStartedTimerItem(type, Utils.toMmSs(0));
        newItems.add(mediaUpgradeStartedTimerItem);
        emitChatItems(chatState.changeTimerItem(newItems, mediaUpgradeStartedTimerItem));
        int timerDelay = 1000;
        int timer1SecInterval = 1000;
        createNewTimerTask();
        createNewTimer();
        timer.schedule(timerTask, timerDelay, timer1SecInterval);
    }

    private void createNewTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
    }

    private void createNewTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        timerTask = new TimerTask() {
            int seconds = 1;

            @Override
            public void run() {
                String time = Utils.toMmSs(seconds);
                if (chatState.isMediaUpgradeStarted()) {
                    int index = chatState.chatItems.indexOf(chatState.mediaUpgradeStartedTimerItem);
                    if (index != -1) {
                        List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
                        MediaUpgradeStartedTimerItem.Type type = chatState.mediaUpgradeStartedTimerItem.type;
                        newItems.remove(index);
                        MediaUpgradeStartedTimerItem mediaUpgradeStartedTimerItem = new MediaUpgradeStartedTimerItem(type, time);
                        newItems.add(index, mediaUpgradeStartedTimerItem);
                        emitChatItems(chatState.changeTimerItem(newItems, mediaUpgradeStartedTimerItem));
                    } else {
                        cancel();
                    }
                } else {
                    cancel();
                }
                Logger.d(TAG, "timer: " + time);
                seconds++;
            }

            @Override
            public boolean cancel() {
                if (chatState.isMediaUpgradeStarted() &&
                        chatState.chatItems.contains(chatState.mediaUpgradeStartedTimerItem)) {
                    List<ChatItem> newItems = new ArrayList<>(chatState.chatItems);
                    newItems.remove(chatState.mediaUpgradeStartedTimerItem);
                    emitChatItems(chatState.changeTimerItem(newItems, null));
                }
                return super.cancel();
            }
        };
    }

    public void setOverlayPermissions(boolean canDrawOverlays) {
        Logger.d(TAG, "setOverlayPermissions: " + canDrawOverlays);
        emitViewState(chatState.drawOverlaysPermissionChanged(canDrawOverlays));
    }
}
