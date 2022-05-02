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
import com.glia.widgets.chat.model.history.VisitorMessageItem;

public class VisitorMessageViewHolder extends RecyclerView.ViewHolder {
    private final TextView content;
    private final TextView deliveredView;

    public VisitorMessageViewHolder(@NonNull View itemView, UiTheme uiTheme) {
        super(itemView);
        this.content = itemView.findViewById(R.id.content);
        this.deliveredView = itemView.findViewById(R.id.delivered_view);
        Context context = itemView.getContext();

        content.setBackgroundTintList(ContextCompat.getColorStateList(context, uiTheme.getVisitorMessageBackgroundColor()));
        content.setTextColor(context.getColor(uiTheme.getVisitorMessageTextColor()));
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

        String contentDescription = itemView.getResources().getString(
                item.isShowDelivered()
                        ? R.string.glia_chat_visitor_message_delivered_content_description
                        : R.string.glia_chat_visitor_message_content_description,
                item.getMessage());
        itemView.setContentDescription(contentDescription);
    }
}
