package com.glia.widgets.chat.adapter.holder;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.model.history.OperatorStatusItem;
import com.glia.widgets.view.OperatorStatusView;
import com.glia.widgets.view.configuration.chat.ChatStyle;

public class OperatorStatusViewHolder extends RecyclerView.ViewHolder {
    private final OperatorStatusView statusPictureView;
    private final TextView chatStartingHeadingView;
    private final TextView chatStartingCaptionView;
    private final TextView chatStartedNameView;
    private final TextView chatStartedCaptionView;
    private final Context context;
    private final ChatStyle chatStyle;

    public OperatorStatusViewHolder(@NonNull View itemView, UiTheme uiTheme, ChatStyle chatStyle) {
        super(itemView);
        this.chatStyle = chatStyle;
        this.statusPictureView = itemView.findViewById(R.id.status_picture_view);
        this.chatStartingHeadingView = itemView.findViewById(R.id.chat_starting_heading_view);
        this.chatStartingCaptionView = itemView.findViewById(R.id.chat_starting_caption_view);
        this.chatStartedNameView = itemView.findViewById(R.id.chat_started_name_view);
        this.chatStartedCaptionView = itemView.findViewById(R.id.chat_started_caption_view);

        context = itemView.getContext();

        statusPictureView.setTheme(uiTheme , chatStyle);

        setStartedHeadingTextColor(uiTheme);
        setStartedCaptionTextColor(uiTheme);

        if (uiTheme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
            chatStartingHeadingView.setTypeface(fontFamily, Typeface.BOLD);
            chatStartingCaptionView.setTypeface(fontFamily);
            chatStartedNameView.setTypeface(fontFamily, Typeface.BOLD);
            chatStartedCaptionView.setTypeface(fontFamily);
        }

        chatStartingCaptionView.setText(chatStyle.welcomeView.descriptionValue);
        chatStartingCaptionView.setTextColor(Color.parseColor(chatStyle.welcomeView.description.foregroundColor));
        chatStartingCaptionView.setTextSize(chatStyle.welcomeView.description.getTextSize());
    }

    private void setStartedHeadingTextColor(UiTheme uiTheme) {
        chatStartedNameView.setTextColor(
                ContextCompat.getColor(
                        context,
                        uiTheme.getGliaChatStartedHeadingTextColor()
                )
        );
    }

    private void setStartedCaptionTextColor(UiTheme uiTheme) {
        chatStartedCaptionView.setTextColor(
                ContextCompat.getColor(
                        context,
                        uiTheme.getGliaChatStartedCaptionTextColor()
                )
        );
    }

    public void bind(OperatorStatusItem item) {
        chatStartingHeadingView.setText(chatStyle.welcomeView.titleValue);
        chatStartingHeadingView.setTextColor(Color.parseColor(chatStyle.welcomeView.title.foregroundColor));
        chatStartingHeadingView.setTextSize(chatStyle.welcomeView.title.getTextSize());
        if (item.getStatus() == OperatorStatusItem.Status.IN_QUEUE) {
            statusPictureView.showPlaceholder();
            chatStartingHeadingView.setVisibility(View.VISIBLE);
            chatStartingCaptionView.setVisibility(View.VISIBLE);
            chatStartedNameView.setVisibility(View.GONE);
            chatStartedCaptionView.setVisibility(View.GONE);
            itemView.setContentDescription(context.getString(R.string.glia_chat_in_queue_message_content_description, item.getCompanyName()));
        } else if (item.getStatus() == OperatorStatusItem.Status.OPERATOR_CONNECTED) {
            if (item.getProfileImgUrl() != null) {
                statusPictureView.showProfileImage(item.getProfileImgUrl());
            } else {
                statusPictureView.showPlaceholder();
            }
            chatStartedNameView.setText(item.getOperatorName());
            chatStartedCaptionView.setText(context.getString(R.string.glia_chat_operator_has_joined, item.getOperatorName()));

            chatStartingHeadingView.setVisibility(View.GONE);
            chatStartingCaptionView.setVisibility(View.GONE);
            chatStartedNameView.setVisibility(View.VISIBLE);
            chatStartedCaptionView.setVisibility(View.VISIBLE);

            itemView.setContentDescription(context.getString(R.string.glia_chat_operator_has_joined_content_description, item.getOperatorName()));
        } else if (item.getStatus() == OperatorStatusItem.Status.JOINED) {
            chatStartedNameView.setText(item.getOperatorName());
            chatStartedCaptionView.setText(context.getString(R.string.glia_chat_operator_has_joined, item.getOperatorName()));
            chatStartingHeadingView.setVisibility(View.GONE);
            chatStartingCaptionView.setVisibility(View.GONE);
            chatStartedNameView.setVisibility(View.VISIBLE);
            chatStartedCaptionView.setVisibility(View.VISIBLE);
            itemView.setContentDescription(context.getString(R.string.glia_chat_operator_has_joined_content_description, item.getOperatorName()));
        } else if (item.getStatus() == OperatorStatusItem.Status.TRANSFERRING) {
            statusPictureView.showPlaceholder();
            chatStartingHeadingView.setVisibility(View.VISIBLE);
            chatStartingCaptionView.setVisibility(View.VISIBLE);
            chatStartedNameView.setVisibility(View.VISIBLE);
            chatStartedNameView.setText(context.getString(R.string.glia_chat_visitor_status_transferring));
            chatStartedCaptionView.setVisibility(View.GONE);
        }
        statusPictureView.setVisibility(
                isShowStatusPictureView(item.getStatus()) ? View.VISIBLE : View.GONE
        );
        statusPictureView.setShowRippleAnimation(isShowStatusViewRippleAnimation(item));
    }

    private boolean isShowStatusPictureView(OperatorStatusItem.Status status) {
        return status != OperatorStatusItem.Status.JOINED;
    }

    private boolean isShowStatusViewRippleAnimation(OperatorStatusItem item) {
        return item.getStatus() == OperatorStatusItem.Status.IN_QUEUE ||
                item.getStatus() == OperatorStatusItem.Status.TRANSFERRING;
    }
}
