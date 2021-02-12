package com.glia.widgets.chat.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.OperatorStatusView;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final AsyncListDiffer<ChatItem> differ =
            new AsyncListDiffer<>(this, DIFF_CALLBACK);

    public static final DiffUtil.ItemCallback<ChatItem> DIFF_CALLBACK
            = new DiffUtil.ItemCallback<ChatItem>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull ChatItem oldItem, @NonNull ChatItem newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(
                @NonNull ChatItem oldItem, @NonNull ChatItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    public static final int OPERATOR_STATUS_VIEW_TYPE = 0;
    public static final int SEND_MESSAGE_VIEW_TYPE = 1;
    public static final int RECEIVE_MESSAGE_VIEW_TYPE = 2;
    public static final int MEDIA_UPGRADE_ITEM_TYPE = 3;
    private final UiTheme uiTheme;

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
        private final TextView deliveredView;

        public SendMessageViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);
            this.content = itemView.findViewById(R.id.content);
            this.deliveredView = itemView.findViewById(R.id.delivered_view);
            Context context = itemView.getContext();
            ColorStateList primaryBrandColor = ContextCompat.getColorStateList(context, uiTheme.getBrandPrimaryColor());
            content.setBackgroundTintList(primaryBrandColor);
            content.setTextColor(context.getColor(uiTheme.getBaseLightColor()));
            if (uiTheme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
                content.setTypeface(fontFamily);
                deliveredView.setTypeface(fontFamily);
            }
            deliveredView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseNormalColor()));
        }

        public void bind(SendMessageItem item) {
            content.setText(item.getMessage());
            deliveredView.setVisibility(item.isShowDelivered() ? View.VISIBLE : View.GONE);
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

    private static class MediaUpgradeStartedViewHolder extends RecyclerView.ViewHolder {

        private final Context context;
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView timerView;

        public MediaUpgradeStartedViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);
            context = itemView.getContext();

            iconView = itemView.findViewById(R.id.icon_view);
            titleView = itemView.findViewById(R.id.title_view);
            timerView = itemView.findViewById(R.id.timer_view);

            int baseShadeColor = ContextCompat.getColor(context, uiTheme.getBaseShadeColor());
            int baseNormalColor = ContextCompat.getColor(context, uiTheme.getBaseNormalColor());
            ColorStateList brandPrimaryColorStateList =
                    ContextCompat.getColorStateList(context, uiTheme.getBrandPrimaryColor());
            int baseDarkColor = ContextCompat.getColor(context, uiTheme.getBaseDarkColor());

            ((MaterialCardView) itemView.findViewById(R.id.card_view))
                    .setStrokeColor(baseShadeColor);
            iconView.setImageTintList(brandPrimaryColorStateList);
            titleView.setTextColor(baseDarkColor);
            timerView.setTextColor(baseNormalColor);
        }

        public void bind(MediaUpgradeStartedTimerItem chatItem) {
            if (chatItem.type == MediaUpgradeStartedTimerItem.Type.AUDIO) {
                iconView.setImageResource(R.drawable.ic_baseline_mic);
                titleView.setText(context.getString(R.string.chat_upgraded_to_audio_call));
            } else {
                iconView.setImageResource(R.drawable.ic_baseline_videocam);
                titleView.setText(context.getString(R.string.chat_upgraded_to_video_call));
            }
            timerView.setText(chatItem.time);
        }
    }

    public void submitList(List<ChatItem> items) {
        differ.submitList(items);
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
        } else if (viewType == MEDIA_UPGRADE_ITEM_TYPE) {
            return new MediaUpgradeStartedViewHolder(inflater.inflate(
                    R.layout.chat_media_upgrade_item, parent, false),
                    uiTheme);
        } else {
            throw new IllegalArgumentException("Unknown viewtype: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem chatItem = differ.getCurrentList().get(position);
        if (chatItem instanceof OperatorStatusItem) {
            ((OperatorStatusViewHolder) holder).bind((OperatorStatusItem) chatItem);
        } else if (chatItem instanceof SendMessageItem) {
            ((SendMessageViewHolder) holder).bind((SendMessageItem) chatItem);
        } else if (chatItem instanceof ReceiveMessageItem) {
            ((ReceiveMessageViewHolder) holder).bind((ReceiveMessageItem) chatItem);
        } else if (chatItem instanceof MediaUpgradeStartedTimerItem) {
            ((MediaUpgradeStartedViewHolder) holder).bind((MediaUpgradeStartedTimerItem) chatItem);
        }
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @Override
    public int getItemViewType(int position) {
        return differ.getCurrentList().get(position).getViewType();
    }
}
