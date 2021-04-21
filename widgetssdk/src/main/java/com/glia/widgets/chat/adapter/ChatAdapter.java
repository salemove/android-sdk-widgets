package com.glia.widgets.chat.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.view.OperatorStatusView;
import com.glia.widgets.view.SingleChoiceCardView;
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
    public static final int VISITOR_MESSAGE_TYPE = 1;
    public static final int OPERATOR_MESSAGE_VIEW_TYPE = 2;
    public static final int MEDIA_UPGRADE_ITEM_TYPE = 3;
    private final UiTheme uiTheme;
    private final SingleChoiceCardView.OnOptionClickedListener onOptionClickedListener;
    private final SingleChoiceCardView.OnImageLoadedListener onImageLoadedListener;

    public ChatAdapter(
            UiTheme uiTheme,
            SingleChoiceCardView.OnOptionClickedListener onOptionClickedListener,
            SingleChoiceCardView.OnImageLoadedListener onImageLoadedListener
    ) {
        this.uiTheme = uiTheme;
        this.onOptionClickedListener = onOptionClickedListener;
        this.onImageLoadedListener = onImageLoadedListener;
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

            statusPictureView.setTheme(uiTheme);
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
                statusPictureView.showPlaceHolder();
                chatStartingHeadingView.setVisibility(View.VISIBLE);
                chatStartingCaptionView.setVisibility(View.VISIBLE);
                chatStartedNameView.setVisibility(View.GONE);
                chatStartedCaptionView.setVisibility(View.GONE);
            } else if (item.getStatus() == OperatorStatusItem.Status.OPERATOR_CONNECTED) {
                if (item.getProfileImgUrl() != null) {
                    statusPictureView.showProfileImage(item.getProfileImgUrl());
                } else {
                    statusPictureView.showPlaceHolder();
                }
                chatStartedNameView.setText(item.getOperatorName());
                chatStartedCaptionView.setText(context.getString(R.string.chat_operator_has_joined, item.getOperatorName()));

                chatStartingHeadingView.setVisibility(View.GONE);
                chatStartingCaptionView.setVisibility(View.GONE);
                chatStartedNameView.setVisibility(View.VISIBLE);
                chatStartedCaptionView.setVisibility(View.VISIBLE);
            }
            statusPictureView
                    .isRippleAnimationShowing(item.getStatus() == OperatorStatusItem.Status.IN_QUEUE);
        }
    }

    private static class VisitorMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView content;
        private final TextView deliveredView;

        public VisitorMessageViewHolder(@NonNull View itemView, UiTheme uiTheme) {
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

        public void bind(VisitorMessageItem item) {
            content.setText(item.getMessage());
            deliveredView.setVisibility(item.isShowDelivered() ? View.VISIBLE : View.GONE);
        }
    }

    private static class OperatorMessageViewHolder extends RecyclerView.ViewHolder {
        private final FrameLayout contentLayout;
        private final UiTheme uiTheme;
        private final Context context;
        private final OperatorStatusView operatorStatusView;

        public OperatorMessageViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);
            this.contentLayout = itemView.findViewById(R.id.content_layout);
            context = itemView.getContext();
            this.uiTheme = uiTheme;
            this.operatorStatusView = itemView.findViewById(R.id.chat_head_view);
            operatorStatusView.setTheme(uiTheme);
            operatorStatusView.isRippleAnimationShowing(false);
        }

        public void bind(
                OperatorMessageItem item,
                SingleChoiceCardView.OnOptionClickedListener onOptionClickedListener,
                SingleChoiceCardView.OnImageLoadedListener onImageLoadedListener
        ) {
            contentLayout.removeAllViews();
            if(item.singleChoiceOptions !=null){
                SingleChoiceCardView singleChoiceCardView = new SingleChoiceCardView(context);
                singleChoiceCardView.setOnOptionClickedListener(onOptionClickedListener);
                singleChoiceCardView.setData(
                        item.getId(),
                        item.choiceCardImageUrl,
                        item.content,
                        item.singleChoiceOptions,
                        item.selectedChoiceIndex,
                        uiTheme,
                        getAdapterPosition(),
                        item.selectedChoiceIndex == null ? onImageLoadedListener : null
                );
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(
                        0,
                        Float.valueOf(context.getResources().getDimension(R.dimen.medium))
                                .intValue(),
                        0,
                        0
                );
                contentLayout.addView(singleChoiceCardView, params);
            } else {
                TextView contentView = getMessageContentView();
                contentView.setText(item.content);
                contentLayout.addView(contentView);
            }
            operatorStatusView.setVisibility(item.showChatHead ? View.VISIBLE : View.GONE);
            if (item.operatorProfileImgUrl != null) {
                operatorStatusView.showProfileImage(item.operatorProfileImgUrl);
            } else {
                operatorStatusView.showPlaceHolder();
            }
        }

        private TextView getMessageContentView() {
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
        private final @DrawableRes
        Integer upgradeAudioIcon;
        private final @DrawableRes
        Integer upgradeVideoIcon;

        public MediaUpgradeStartedViewHolder(@NonNull View itemView, UiTheme uiTheme) {
            super(itemView);
            context = itemView.getContext();

            MaterialCardView layoutCardView = itemView.findViewById(R.id.card_view);
            iconView = itemView.findViewById(R.id.icon_view);
            titleView = itemView.findViewById(R.id.title_view);
            timerView = itemView.findViewById(R.id.timer_view);

            this.upgradeAudioIcon = uiTheme.getIconChatAudioUpgrade();
            this.upgradeVideoIcon = uiTheme.getIconChatVideoUpgrade();

            ColorStateList baseLightStateList = ContextCompat.getColorStateList(context, uiTheme.getBaseLightColor());
            int baseShadeColor = ContextCompat.getColor(context, uiTheme.getBaseShadeColor());
            int baseNormalColor = ContextCompat.getColor(context, uiTheme.getBaseNormalColor());
            ColorStateList brandPrimaryColorStateList =
                    ContextCompat.getColorStateList(context, uiTheme.getBrandPrimaryColor());
            int baseDarkColor = ContextCompat.getColor(context, uiTheme.getBaseDarkColor());

            layoutCardView.setBackgroundTintList(baseLightStateList);
            layoutCardView.setStrokeColor(baseShadeColor);
            iconView.setImageTintList(brandPrimaryColorStateList);
            titleView.setTextColor(baseDarkColor);
            timerView.setTextColor(baseNormalColor);

            if (uiTheme.getFontRes() != null) {
                Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
                titleView.setTypeface(fontFamily);
                timerView.setTypeface(fontFamily);
            }
        }

        public void bind(MediaUpgradeStartedTimerItem chatItem) {
            if (chatItem.type == MediaUpgradeStartedTimerItem.Type.AUDIO) {
                iconView.setImageResource(upgradeAudioIcon);
                titleView.setText(context.getString(R.string.chat_upgraded_to_audio_call));
            } else {
                iconView.setImageResource(upgradeVideoIcon);
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
                    inflater.inflate(R.layout.chat_operator_status_layout, parent, false),
                    uiTheme);
        } else if (viewType == VISITOR_MESSAGE_TYPE) {
            return new VisitorMessageViewHolder(
                    inflater.inflate(R.layout.chat_visitor_message_layout, parent, false),
                    uiTheme);
        } else if (viewType == OPERATOR_MESSAGE_VIEW_TYPE) {
            return new OperatorMessageViewHolder(
                    inflater.inflate(R.layout.chat_operator_message_layout, parent, false),
                    uiTheme);
        } else if (viewType == MEDIA_UPGRADE_ITEM_TYPE) {
            return new MediaUpgradeStartedViewHolder(inflater.inflate(
                    R.layout.chat_media_upgrade_layout, parent, false),
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
        } else if (chatItem instanceof VisitorMessageItem) {
            ((VisitorMessageViewHolder) holder).bind((VisitorMessageItem) chatItem);
        } else if (chatItem instanceof OperatorMessageItem) {
            ((OperatorMessageViewHolder) holder).bind(
                    (OperatorMessageItem) chatItem,
                    onOptionClickedListener,
                    onImageLoadedListener
            );
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
