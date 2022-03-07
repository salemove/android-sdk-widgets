package com.glia.widgets.chat.adapter.holder.fileattachment;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.model.history.VisitorAttachmentItem;

public class VisitorFileAttachmentViewHolder extends FileAttachmentViewHolder {
    private final TextView deliveredView;

    public VisitorFileAttachmentViewHolder(@NonNull View itemView, UiTheme uiTheme) {
        super(itemView);
        deliveredView = itemView.findViewById(R.id.delivered_view);
        setupDeliveredView(itemView.getContext(), uiTheme);
    }

    public void bind(VisitorAttachmentItem item, ChatAdapter.OnFileItemClickListener listener) {
        super.setData(item.isFileExists, item.isDownloading, item.attachmentFile, listener);
        updateDeliveredView(item);
    }

    private void setupDeliveredView(Context context, UiTheme uiTheme) {
        if (uiTheme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
            deliveredView.setTypeface(fontFamily);
        }
        deliveredView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseNormalColor()));
    }

    private void updateDeliveredView(VisitorAttachmentItem item) {
        deliveredView.setVisibility(item.showDelivered ? View.VISIBLE : View.GONE);
    }
}
