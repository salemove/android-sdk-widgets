package com.glia.widgets.chat.adapter.holder;

import android.content.Context;
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

public class OperatorStatusViewHolder extends RecyclerView.ViewHolder {
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
            chatStartedCaptionView.setText(context.getString(R.string.glia_chat_operator_has_joined, item.getOperatorName()));

            chatStartingHeadingView.setVisibility(View.GONE);
            chatStartingCaptionView.setVisibility(View.GONE);
            chatStartedNameView.setVisibility(View.VISIBLE);
            chatStartedCaptionView.setVisibility(View.VISIBLE);
        }
        statusPictureView.isRippleAnimationShowing(item.getStatus() == OperatorStatusItem.Status.IN_QUEUE);
    }
}
