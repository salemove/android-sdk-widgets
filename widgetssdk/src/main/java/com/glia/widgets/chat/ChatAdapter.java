package com.glia.widgets.chat;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int OPERATOR_STATUS_VIEW_TYPE = 0;
    public static final int SEND_MESSAGE_VIEW_TYPE = 1;
    public static final int RECEIVE_MESSAGE_VIEW_TYPE = 2;
    private final UiTheme uiTheme;

    private List<ChatItem> chatItems;

    public ChatAdapter(UiTheme uiTheme) {
        this.uiTheme = uiTheme;
    }

    private static class OperatorStatusViewHolder extends RecyclerView.ViewHolder {
        private final OperatorStatusView statusPictureView;
        private final TextView chatStartingHeadingView;
        private final TextView chatStartingCaptionView;
        private final TextView chatStartedNameView;
        private final TextView chatStartedCaptionView;
        private final Context context;

        public OperatorStatusViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);
            this.statusPictureView = itemView.findViewById(R.id.status_picture_view);
            this.chatStartingHeadingView = itemView.findViewById(R.id.chat_starting_heading_view);
            this.chatStartingCaptionView = itemView.findViewById(R.id.chat_starting_caption_view);
            this.chatStartedNameView = itemView.findViewById(R.id.chat_started_name_view);
            this.chatStartedCaptionView = itemView.findViewById(R.id.chat_started_caption_view);

            context = itemView.getContext();

            statusPictureView.setTint(uiTheme.getBrandPrimaryColor(), uiTheme.getBaseLightColor());
            chatStartingHeadingView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseDarkColor()));
            chatStartingCaptionView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseNormalColor()));
            chatStartedNameView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseDarkColor()));
            chatStartedCaptionView.setTextColor(ContextCompat.getColor(context, uiTheme.getBrandPrimaryColor()));

            if (uiTheme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
                chatStartingHeadingView.setTypeface(fontFamily, Typeface.BOLD);
                chatStartingCaptionView.setTypeface(fontFamily);
                chatStartedNameView.setTypeface(fontFamily, Typeface.BOLD);
                chatStartedCaptionView.setTypeface(fontFamily);
            }
        }

        public void bind(OperatorStatusItem item) {
            chatStartingHeadingView.setText(item.getCompanyName());
            if (item.getStatus() == OperatorStatusItem.Status.IN_QUEUE) {
                statusPictureView.removeOperatorImage();
                chatStartingHeadingView.setVisibility(View.VISIBLE);
                chatStartingCaptionView.setVisibility(View.VISIBLE);
                chatStartedNameView.setVisibility(View.GONE);
                chatStartedCaptionView.setVisibility(View.GONE);
            } else if (item.getStatus() == OperatorStatusItem.Status.OPERATOR_CONNECTED) {
                statusPictureView.setOperatorImage(android.R.drawable.star_on, false);
                chatStartedNameView.setText(item.getOperatorName());
                chatStartedCaptionView.setText(context.getString(R.string.chat_operator_has_joined, item.getOperatorName()));

                chatStartingHeadingView.setVisibility(View.GONE);
                chatStartingCaptionView.setVisibility(View.GONE);
                chatStartedNameView.setVisibility(View.VISIBLE);
                chatStartedCaptionView.setVisibility(View.VISIBLE);
            }
        }
    }

    private static class SendMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView content;

        public SendMessageViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);
            this.content = itemView.findViewById(R.id.content);
            Context context = itemView.getContext();
            ColorStateList primaryBrandColor = ContextCompat.getColorStateList(context, uiTheme.getBrandPrimaryColor());
            content.setBackgroundTintList(primaryBrandColor);
            content.setTextColor(context.getColor(uiTheme.getBaseLightColor()));
            if (uiTheme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
                content.setTypeface(fontFamily);
            }
        }

        public void bind(SendMessageItem item) {
            content.setText(item.getMessage());
        }
    }

    private static class ReceiveMessageViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout contentLayout;
        private final UiTheme uiTheme;
        private final Context context;

        public ReceiveMessageViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);
            this.contentLayout = itemView.findViewById(R.id.content_layout);
            context = itemView.getContext();
            this.uiTheme = uiTheme;
        }

        public void bind(ReceiveMessageItem item) {
            contentLayout.removeAllViews();
            for (String content : item.getMessages()) {
                TextView contentView = getContentView();
                contentView.setText(content);
                contentLayout.addView(contentView);
            }
        }

        private TextView getContentView() {
            TextView contentView = (TextView) LayoutInflater.from(context)
                    .inflate(R.layout.chat_receive_message_content, contentLayout, false);
            ColorStateList operatorBgColor =
                    ContextCompat.getColorStateList(context, uiTheme.getSystemAgentBubbleColor());
            contentView.setBackgroundTintList(operatorBgColor);
            contentView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseDarkColor()));
            if (uiTheme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
                contentView.setTypeface(fontFamily);
            }
            return contentView;
        }
    }

    public void replaceItems(List<ChatItem> items, Pair<Integer, Integer> range) {
        this.chatItems = items;
        notifyItemRangeChanged(range.first, range.second, chatItems);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == OPERATOR_STATUS_VIEW_TYPE) {
            return new OperatorStatusViewHolder(
                    inflater.inflate(R.layout.chat_operator_status_item, parent, false),
                    uiTheme);
        } else if (viewType == SEND_MESSAGE_VIEW_TYPE) {
            return new SendMessageViewHolder(
                    inflater.inflate(R.layout.chat_send_message_item, parent, false),
                    uiTheme);
        } else if (viewType == RECEIVE_MESSAGE_VIEW_TYPE) {
            return new ReceiveMessageViewHolder(
                    inflater.inflate(R.layout.chat_receive_message_item, parent, false),
                    uiTheme);
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
            ((SendMessageViewHolder) holder).bind((SendMessageItem) chatItem);
        } else if (chatItem instanceof ReceiveMessageItem) {
            ((ReceiveMessageViewHolder) holder).bind((ReceiveMessageItem) chatItem);
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
