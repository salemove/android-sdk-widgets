package com.glia.widgets.chat.adapter.holder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
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
import com.glia.widgets.view.OperatorStatusView;
import com.glia.widgets.view.SingleChoiceCardView;

public class OperatorMessageViewHolder extends RecyclerView.ViewHolder {
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
        if (item.singleChoiceOptions != null) {
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
                    Float.valueOf(context.getResources().getDimension(R.dimen.glia_medium))
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
