package com.glia.widgets.chat.adapter.holder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.model.history.OperatorMessageItem;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.ResourceProvider;
import com.glia.widgets.view.OperatorStatusView;
import com.glia.widgets.view.SingleChoiceCardView;
import com.glia.widgets.view.configuration.chat.ChatStyle;

public class OperatorMessageViewHolder extends RecyclerView.ViewHolder {
    private final FrameLayout contentLayout;
    private final ChatStyle chatStyle;
    private final UiTheme uiTheme;
    private final Context context;
    private final OperatorStatusView operatorStatusView;

    public OperatorMessageViewHolder(@NonNull View itemView, ChatStyle chatStyle, UiTheme uiTheme) {
        super(itemView);
        context = itemView.getContext();
        this.chatStyle = chatStyle;
        this.uiTheme = uiTheme;
        contentLayout = itemView.findViewById(R.id.content_layout);
        operatorStatusView = itemView.findViewById(R.id.chat_head_view);
        setupOperatorStatusView();
    }

    private void setupOperatorStatusView() {
        operatorStatusView.setTheme(uiTheme, chatStyle);
        operatorStatusView.setShowRippleAnimation(false);
    }

    public void bind(
            OperatorMessageItem item,
            SingleChoiceCardView.OnOptionClickedListener onOptionClickedListener
    ) {
        contentLayout.removeAllViews();
        if (item.singleChoiceOptions != null) {
            addSingleChoiceCardView(item, onOptionClickedListener);
        } else {
            addMessageTextView(item);
        }
        updateOperatorStatusView(item);
    }

    private void addSingleChoiceCardView(
            OperatorMessageItem item,
            SingleChoiceCardView.OnOptionClickedListener onOptionClickedListener
    ) {
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
                chatStyle
        );
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(
                0,
                Float.valueOf(context.getResources().getDimension(R.dimen.glia_medium))
                        .intValue(),
                0,
                0
        );
        contentLayout.addView(singleChoiceCardView, params);

        itemView.setContentDescription(item.content);
    }

    private void addMessageTextView(OperatorMessageItem item) {
        TextView contentView = getMessageContentView();
        contentView.setText(item.content);
        contentLayout.addView(contentView);

        if (!TextUtils.isEmpty(item.operatorName)) {
            itemView.setContentDescription(context.getString(
                    R.string.glia_chat_operator_name_message_content_description,
                    item.operatorName,
                    item.content));
        } else {
            itemView.setContentDescription(context.getString(
                    R.string.glia_chat_operator_message_content_description,
                    item.content));
        }
    }

    private TextView getMessageContentView() {
        TextView contentView = (TextView) LayoutInflater.from(context)
                .inflate(R.layout.chat_receive_message_content, contentLayout, false);
        contentView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(chatStyle.operatorMessage.layer.getBackgroundColor())));

        // Set corner radius
        GradientDrawable drawable = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.bg_message);
        if (drawable != null) {
            ResourceProvider resourceProvider = Dependencies.getResourceProvider();
            drawable.setCornerRadius(resourceProvider.convertDpToPixel(chatStyle.operatorMessage.layer.getCornerRadius()));
        }
        contentView.setBackground(drawable);

        contentView.setTextColor(Color.parseColor(chatStyle.operatorMessage.text.foregroundColor));
        contentView.setTextSize(chatStyle.operatorMessage.text.getTextSize());
        contentView.setLinkTextColor(ContextCompat.getColor(context, uiTheme.getOperatorMessageTextColor()));
        contentView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        if (uiTheme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
            contentView.setTypeface(fontFamily);
        }
        return contentView;
    }

    private void updateOperatorStatusView(OperatorMessageItem item) {
        operatorStatusView.setVisibility(item.showChatHead ? View.VISIBLE : View.GONE);
        if (item.operatorProfileImgUrl != null) {
            operatorStatusView.showProfileImage(item.operatorProfileImgUrl);
        } else {
            operatorStatusView.showPlaceholder();
        }
    }
}
