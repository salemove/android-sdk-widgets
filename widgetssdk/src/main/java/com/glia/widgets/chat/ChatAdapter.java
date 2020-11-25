package com.glia.widgets.chat;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int OPERATOR_STATUS_VIEW_TYPE = 0;
    public static final int SEND_MESSAGE_VIEW_TYPE = 1;
    public static final int RECEIVE_MESSAGE_VIEW_TYPE = 2;
    private final ColorStateList senderMessageTint;
    private final ColorStateList receiverMessageTint;

    private List<ChatItem> chatItems;

    public ChatAdapter(ColorStateList senderMessageTint,
                       ColorStateList receiverMessageTint) {
        this.senderMessageTint = senderMessageTint;
        this.receiverMessageTint = receiverMessageTint;
    }

    public void initDefault() {
        List<ChatItem> chatItems = new ArrayList<>();
        chatItems.add(new OperatorStatusItem());
        chatItems.add(new SendMessageItem("Hi, I need help and guidance with moving money from one account to another"));
        chatItems.add(new ReceiveMessageItem("Hi, Roger! I’d be glad to help you out. Could you specify the accounts that you want to use."));
        chatItems.add(new ReceiveMessageItem("Let’s upgrade to a video call and go over the details together!"));
        chatItems.add(new SendMessageItem("Okay!"));
        replaceAllItems(chatItems);
    }

    private static class OperatorStatusViewHolder extends RecyclerView.ViewHolder {
        private final View itemView;

        public OperatorStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bind(OperatorStatusItem item) {
            TextView joinedCaptionView = itemView.findViewById(R.id.joined_operator_caption_view);
        }
    }

    private static class SendMessageViewHolder extends RecyclerView.ViewHolder {
        private final View itemView;

        public SendMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bind(SendMessageItem item, ColorStateList backgroundColor) {
            TextView content = itemView.findViewById(R.id.content);

            content.setBackgroundTintList(backgroundColor);
            content.setText(item.getMessage());
        }
    }

    private static class ReceiveMessageViewHolder extends RecyclerView.ViewHolder {
        private final View itemView;

        public ReceiveMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void bind(ReceiveMessageItem item, ColorStateList backgroundColor) {
            TextView content = itemView.findViewById(R.id.content);

            content.setBackgroundTintList(backgroundColor);
            content.setText(item.getMessage());
        }
    }

    public void replaceAllItems(List<ChatItem> chatItems) {
        this.chatItems = chatItems;
        notifyDataSetChanged();
    }

    public void addItem(ChatItem chatItem) {
        this.chatItems.add(chatItem);
        notifyItemRangeInserted(this.chatItems.size() - 1, this.chatItems.size() - 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == OPERATOR_STATUS_VIEW_TYPE) {
            return new OperatorStatusViewHolder(inflater.inflate(R.layout.chat_operator_status_item, parent, false));
        } else if (viewType == SEND_MESSAGE_VIEW_TYPE) {
            return new SendMessageViewHolder(inflater.inflate(R.layout.chat_send_message_item, parent, false));
        } else if (viewType == RECEIVE_MESSAGE_VIEW_TYPE) {
            return new ReceiveMessageViewHolder(inflater.inflate(R.layout.chat_receive_message_item, parent, false));
        } else {
            throw new IllegalArgumentException("Unknown viewtype: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem chatItem = chatItems.get(position);
        if (chatItem instanceof OperatorStatusItem) {
            ((OperatorStatusViewHolder) holder).bind((OperatorStatusItem) chatItem);
        } else if (chatItem instanceof SendMessageItem) {
            ((SendMessageViewHolder) holder).bind((SendMessageItem) chatItem, senderMessageTint);
        } else if (chatItem instanceof ReceiveMessageItem) {
            ((ReceiveMessageViewHolder) holder).bind((ReceiveMessageItem) chatItem, receiverMessageTint);
        }
    }

    @Override
    public int getItemCount() {
        if (this.chatItems != null) {
            return this.chatItems.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return chatItems.get(position).getViewType();
    }
}
