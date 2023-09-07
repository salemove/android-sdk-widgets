package com.glia.widgets.chat.adapter.holder.fileattachment;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.model.VisitorAttachmentItem;

public class VisitorFileAttachmentViewHolder extends FileAttachmentViewHolder {
    private final TextView deliveredView;

    public VisitorFileAttachmentViewHolder(@NonNull View itemView, UiTheme uiTheme) {
        super(itemView);
        deliveredView = itemView.findViewById(R.id.delivered_view);
        setupDeliveredView(itemView.getContext(), uiTheme);
    }

    public void bind(VisitorAttachmentItem.File item, ChatAdapter.OnFileItemClickListener listener) {
        super.setData(item.isFileExists(), item.isDownloading(), item.getAttachmentFile(), listener);
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
        deliveredView.setVisibility(item.getShowDelivered() ? View.VISIBLE : View.GONE);

        setAccessibilityLabels(item);
    }

    private void setAccessibilityLabels(VisitorAttachmentItem item) {
        String name = item.getAttachmentFile().getName();
        String byteSize = Formatter.formatFileSize(itemView.getContext(), item.getAttachmentFile().getSize());
        itemView.setContentDescription(itemView.getResources().getString(item.getShowDelivered()
                ? R.string.glia_chat_visitor_file_delivered_content_description
                : R.string.glia_chat_visitor_file_content_description,
            name, byteSize));

        ViewCompat.setAccessibilityDelegate(itemView, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);

                String actionLabel = host.getResources().getString(item.isFileExists()
                    ? R.string.glia_chat_attachment_open_button_label
                    : R.string.glia_chat_attachment_download_button_label);

                AccessibilityNodeInfoCompat.AccessibilityActionCompat actionClick
                    = new AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel);
                info.addAction(actionClick);
            }
        });
    }

    public void updateDelivered(boolean delivered) {
        deliveredView.setVisibility(delivered ? View.VISIBLE : View.GONE);
    }
}
