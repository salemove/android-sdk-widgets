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
import com.glia.widgets.StringProvider;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.model.VisitorAttachmentItem;
import com.glia.widgets.di.Dependencies;

public class VisitorFileAttachmentViewHolder extends FileAttachmentViewHolder {
    private final TextView deliveredView;
    private final StringProvider stringProvider = Dependencies.getStringProvider();

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
        deliveredView.setText(stringProvider.getRemoteString(R.string.chat_status_delivered));
        deliveredView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseNormalColor()));
    }

    private void updateDeliveredView(VisitorAttachmentItem item) {
        deliveredView.setVisibility(item.getShowDelivered() ? View.VISIBLE : View.GONE);

        setAccessibilityLabels(item);
    }

    private void setAccessibilityLabels(VisitorAttachmentItem item) {
        String name = item.getAttachmentFile().getName();
        String byteSize = Formatter.formatFileSize(itemView.getContext(), item.getAttachmentFile().getSize());
        itemView.setContentDescription(stringProvider.getRemoteString(item.getShowDelivered()
                ? R.string.android_chat_file_visitor_delivered_accessibility
                : R.string.android_chat_file_visitor_accessibility,
            name, byteSize));

        ViewCompat.setAccessibilityDelegate(itemView, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);

                String actionLabel = stringProvider.getRemoteString(item.isFileExists()
                    ? R.string.general_open
                    : R.string.general_download);

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
