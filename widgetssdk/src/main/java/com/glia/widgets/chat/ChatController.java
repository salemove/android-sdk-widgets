package com.glia.widgets.chat;

import android.util.Pair;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.chat.VisitorMessage;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;
import com.glia.widgets.helper.Logger;
import com.glia.widgets.model.GliaRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatController {

    private ChatViewCallback viewCallback;
    private ChatGliaCallback gliaCallback;
    private GliaRepository repository;

    private final String TAG = "ChatController";
    private volatile ChatState chatState;
    public volatile DialogsState dialogsState;

    public ChatController(ChatViewCallback viewCallback) {
        this.viewCallback = viewCallback;
        this.chatState = new ChatState.Builder()
                .setUseFloatingChatHeads(false)
                .setQueueTicketId(null)
                .setHistoryLoaded(false)
                .setOperatorOnline(false)
                .setCompanyName(null)
                .setQueueId(null)
                .setContextUrl(null)
                .setIsVisible(false)
                .setIntegratorChatStarted(false)
                .setHasOverlayPermissions(false)
                .setOverlaysPermissionDialogShown(false)
                .setChatItems(new ArrayList<>())
                .createChatState();
        this.dialogsState = new DialogsState(false, false, false, false);
    }

    public void initChat(GliaRepository gliaRepository,
                         String companyName,
                         String queueId,
                         String contextUrl,
                         boolean useChatHeads) {
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

    public void onDestroy(boolean retain) {
        viewCallback = null;
        if (!retain && chatState.useFloatingChatHeads && repository != null) {
            repository.onDestroyView();
        }
        if (!retain && chatState.useFloatingChatHeads) {
            gliaCallback = null;
        }
    }

    public void sendMessagePreview(String message) {
        if (isMessageValid(message)) {
            Logger.d(TAG, "Send preview: " + message);
            repository.sendMessagePreview(message);
        }
    }

    public void sendMessage(String message) {
        if (isMessageValid(message)) {
            Logger.d(TAG, "Send MESSAGE: " + message);
            repository.sendMessage(message);
        }
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

    public void show() {
        if (!chatState.isVisible) {
            emitViewState(chatState.show());
        }
    }

    public void onBackArrowClicked() {
        if (!chatState.useFloatingChatHeads) {
            emitViewState(chatState.hide());
        } else if (chatState.hasOverlayPermissions) {
            handleFloatingChatheads(true);
        }
    }

    public void noMoreOperatorsAvailableDismissed() {
        stop(false, false);
        dismissDialogs();
    }

    public void unexpectedErrorDialogDismissed() {
        stop(false, false);
        dismissDialogs();
    }

    public void exitDialogYesClicked() {
        stop(false, false);
        dismissDialogs();
    }

    public void exitDialogDismissClicked() {
        dismissDialogs();
    }

    public void leaveChatClicked() {
        showExitDialog();
    }

    public boolean isChatVisible() {
        return chatState.isVisible;
    }

    public void setViewCallback(ChatViewCallback chatViewCallback) {
        this.viewCallback = chatViewCallback;
        viewCallback.emitState(chatState);
        viewCallback.emitItems(chatState.chatItems, new Pair<>(0, chatState.chatItems.size()), true);
        viewCallback.emitDialog(dialogsState);
    }

    public void onResume(boolean hasOverlaysPermission) {
        if (!hasOverlaysPermission && !chatState.overlaysPermissionDialogShown) {
            showOverlayPermissionsDialog();
        }
        emitViewState(chatState.drawOverlaysPermissionChanged(hasOverlaysPermission));
    }

    public void overlayPermissionsDialogDismissed() {
        emitDialogState(new DialogsState(false, false, false, false));
    }

    private synchronized void emitChatItems(ChatState state,
                                            Pair<Integer, Integer> range,
                                            boolean scrollToBottom) {
        if (setState(state) && viewCallback != null) {
            Logger.d(TAG, "Emit chat items:\n" + state.chatItems.toString() +
                    "\nRange: " + range.toString() +
                    "\nScrollToBottom: " + scrollToBottom +
                    "\n(State): " + state.toString());
            viewCallback.emitItems(state.chatItems, range, scrollToBottom);
        }
    }

    private synchronized void emitDialogState(DialogsState state) {
        if (setDialogState(state) && viewCallback != null) {
            Logger.d(TAG, "Emit dialog state:\n" + dialogsState.toString());
            viewCallback.emitDialog(dialogsState);
        }
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
                Integer invalidateFrom = changeDeliveredIndex(currentChatItems, visitorMessage);
                emitChatItems(chatState.changeItems(currentChatItems), new Pair<>
                                (invalidateFrom != null ? invalidateFrom : chatState.chatItems.size() - 1,
                                        invalidateFrom != null ? currentChatItems.size() - 1 - invalidateFrom : 1),
                        true);
            }

            @Override
            public void onMessage(ChatMessage message) {
                Logger.d(TAG, "onMessage: " + message.getContent());
                List<ChatItem> items = new ArrayList<>(chatState.chatItems);
                appendMessageItem(items, message);
                emitChatItems(chatState.changeItems(items), new Pair<>
                        (chatState.chatItems.size() - 1, 1), true);
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
                emitChatItems(
                        chatState.historyLoaded(items),
                        new Pair<>(0, items.size()),
                        true);
                repository.initMessaging();
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
        if (!chatState.operatorOnline) {
            List<ChatItem> items = new ArrayList<>();
            items.add(OperatorStatusItem.QueueingStatusItem(chatState.companyName));
            emitViewState(chatState.initQueueing());
            emitChatItems(chatState.changeItems(items), new Pair<>(0, 1), false);
        }
    }

    private void operatorOnlineStartChatUi(String operatorName) {
        if (!chatState.operatorOnline) {
            List<ChatItem> items = new ArrayList<>();
            items.add(OperatorStatusItem.OperatorFoundStatusItem(chatState.companyName, operatorName));
            emitViewState(chatState.engagementStarted());
            emitChatItems(chatState.changeItems(items), new Pair<>(0, 1), false);
        }
    }

    private void stop(boolean showDialog, boolean isVisible) {
        repository.stop(chatState.queueTicketId, showDialog);
        emitViewState(chatState.stop(isVisible));
        if (showDialog && !dialogsState.noOperatorsAvailableDialogShowing) {
            showNoMoreOperatorsAvailableDialog();
        }
    }

    private void appendHistoryChatItem(List<ChatItem> currentChatItems, ChatMessage message) {
        if (message.getSender() == Chat.Participant.VISITOR) {
            currentChatItems.add(new SendMessageItem(null, false, message.getContent()));
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

    private Integer changeDeliveredIndex(List<ChatItem> currentChatItems, VisitorMessage message) {
        Integer invalidateFrom = null;
        for (int i = currentChatItems.size() - 1; i >= 0; i--) {
            if (currentChatItems.get(i) instanceof SendMessageItem) {
                SendMessageItem item = (SendMessageItem) currentChatItems.get(i);
                if (item.getId() == null) {
                    // id-s are null in case of history items. break out of loop.
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
                    invalidateFrom = i;
                }
            }
        }
        return invalidateFrom;
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
        currentChatItems.add(new ReceiveMessageItem(messages));
    }

    private boolean isMessageValid(String message) {
        return message.length() > 0;
    }

    private void dismissDialogs() {
        Logger.d(TAG, "Dismiss dialogs");
        emitDialogState(new DialogsState(false, false, false, false));
    }

    private void showExitDialog() {
        if (!dialogsState.isDialogShowing()) {
            emitDialogState(new DialogsState(false, false, false, true));
        }
    }

    private void showNoMoreOperatorsAvailableDialog() {
        if (!dialogsState.isDialogShowing()) {
            emitDialogState(new DialogsState(false, true, false, false));
        }
    }

    private void showUnexpectedErrorDialog() {
        if (!dialogsState.isDialogShowing()) {
            emitDialogState(new DialogsState(false, false, true, false));
        }
    }

    private void handleFloatingChatheads(boolean show) {
        if (viewCallback != null && chatState.useFloatingChatHeads) {
            Logger.d(TAG, "handleFloatingChatHeads, show: " + show);
            viewCallback.handleFloatingChatHead(show);
        }
    }

    private void showOverlayPermissionsDialog() {
        if (!dialogsState.isDialogShowing()) {
            emitDialogState(new DialogsState(true, false, false, false));
        }
    }
}
