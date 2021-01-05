package com.glia.widgets.chat;

import android.util.Log;

import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.chat.Chat;
import com.glia.androidsdk.chat.ChatMessage;
import com.glia.androidsdk.omnicore.OmnicoreEngagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatController {

    private ChatViewCallback viewCallback;
    private ChatGliaCallback gliaCallback;
    private final GliaRepository repository;
    private final String companyName;
    private String queueTicketId;
    private final String queueId;
    private boolean historyLoaded = false;
    private final String TAG = "ChatController";
    // Remembers which button to display on top app bar.
    private boolean operatorOnline = false;
    private List<String> previousAgentMessages = new ArrayList<>();

    public ChatController(ChatViewCallback viewCallback,
                          GliaRepository gliaRepository,
                          String companyName,
                          String queueId) {
        this.viewCallback = viewCallback;
        this.companyName = companyName;
        this.queueId = queueId;
        this.repository = gliaRepository;
        initControllerCallback();
    }

    private void initControllerCallback() {
        gliaCallback = new ChatGliaCallback() {
            @Override
            public void queueForEngagementStart() {
                if (viewCallback != null) {
                    Log.d(TAG, "queueForEngagementStart");
                    List<ChatItem> items = new ArrayList<>();
                    items.add(OperatorStatusItem.QueueingStatusItem(companyName));
                    viewCallback.replaceItems(items);
                }
            }

            @Override
            public void queueForEngangmentSuccess() {
                Log.d(TAG, "queueForEngagementSuccess");
                if (!operatorOnline) {
                    viewCallback.queueing(OperatorStatusItem.QueueingStatusItem(companyName));
                }
            }

            @Override
            public void queueForTicketSuccess(String ticketId) {
                Log.d(TAG, "queueTicketSuccess");
                queueTicketId = ticketId;
                if (!operatorOnline) {
                    viewCallback.queueing(OperatorStatusItem.QueueingStatusItem(companyName));
                }
            }

            @Override
            public void engagementEndedByOperator() {
                stop(true);
            }

            @Override
            public void engagementEnded(boolean showDialog) {
                Log.d(TAG, "engagementEnded");
                if (showDialog) {
                    viewCallback.engagementEndShowNoMoreOperatorsDialog();
                } else {
                    viewCallback.engagementEndNoDialog();
                }
                operatorOnline = false;
            }

            @Override
            public void engagementSuccess(OmnicoreEngagement engagement) {
                Log.d(TAG, "engagementSuccess");
                if (!operatorOnline) {
                    viewCallback.chatStarted(
                            OperatorStatusItem.OperatorFoundStatusItem(
                                    companyName, engagement.getOperator().getName()));
                    operatorOnline = true;
                }
                if (!historyLoaded) {
                    loadHistory();
                    historyLoaded = true;
                }
            }

            @Override
            public void onMessage(ChatMessage message) {
                Log.d(TAG, "onMessage:\n" + message.toString());
                handleNewMessage(message);
            }

            @Override
            public void chatHistoryLoaded(ChatMessage[] messages, Throwable error) {
                Log.d(TAG, "chatHistoryLoaded");
                if (error != null && (messages == null || messages.length == 0)) {
                    Log.e(TAG, "chatHistoryLoaded error");
                    this.error(error);
                }
                if (messages == null || messages.length == 0) {
                    return;
                }
                Arrays.stream(messages).forEachOrdered(message -> handleNewMessage(message));
                initMessaging();
            }

            @Override
            public void error(GliaException exception) {
                Log.e(TAG, exception.toString());
                viewCallback.unexpectedError();
            }

            @Override
            public void error(Throwable throwable) {
                Log.e(TAG, throwable.toString());
                viewCallback.unexpectedError();
            }
        };
    }

    public void init(String queueId) {
        repository.init(gliaCallback, queueId);
    }

    public void stop(boolean showDialog) {
        repository.stop(queueTicketId, showDialog);
    }

    public void onDestroy() {
        repository.onDestroyView();
        viewCallback = null;
        gliaCallback = null;
    }

    public void sendMessagePreview(String message) {
        if (isMessageValid(message)) {
            repository.sendMessagePreview(message);
        }
    }

    public void sendMessage(String message) {
        if (isMessageValid(message)) {
            repository.sendMessage(message);
        }
    }

    private void initMessaging() {
        repository.initMessaging();
    }

    private void loadHistory() {
        repository.loadHistory();
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getQueueId() {
        return queueId;
    }

    private void handleNewMessage(ChatMessage message) {
        if (message.getSender() == Chat.Participant.VISITOR) {
            viewCallback.appendItem(new SendMessageItem(message.getContent()));
            previousAgentMessages = new ArrayList<>();
        } else {
            if (previousAgentMessages.isEmpty()) {
                previousAgentMessages.add(message.getContent());
                viewCallback.appendItem(new ReceiveMessageItem(new ArrayList<>(previousAgentMessages)));
            } else {
                viewCallback.replaceReceiverItem(getReceiverItemWithAppendedMessage(message));
            }
        }
    }

    private ReceiveMessageItem getReceiverItemWithAppendedMessage(ChatMessage message) {
        previousAgentMessages.add(message.getContent());
        return new ReceiveMessageItem(new ArrayList<>(previousAgentMessages));
    }

    private boolean isMessageValid(String message) {
        return message.length() > 0;
    }
}
