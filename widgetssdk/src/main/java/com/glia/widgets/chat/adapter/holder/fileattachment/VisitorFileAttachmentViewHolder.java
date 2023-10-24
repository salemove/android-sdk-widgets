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
import com.glia.widgets.StringKey;
import com.glia.widgets.StringKeyPair;
import com.glia.widgets.StringProvider;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.model.VisitorAttachmentItem;
import com.glia.widgets.di.Dependencies;

public class VisitorFileAttachmentViewHolder extends FileAttachmentViewHolder {
    private final TextView deliveredView;
    private final TextView errorView;
    private final StringProvider stringProvider = Dependencies.getStringProvider();
    private VisitorAttachmentItem.File item;

    public VisitorFileAttachmentViewHolder(@NonNull View itemView, UiTheme uiTheme) {
        super(itemView);
        deliveredView = itemView.findViewById(R.id.delivered_view);
        errorView = itemView.findViewById(R.id.error_view);
        setupDeliveredView(itemView.getContext(), uiTheme);
        setupErrorView(itemView.getContext(), uiTheme);
    }

    public void bind(
        VisitorAttachmentItem.File item,
        ChatAdapter.OnFileItemClickListener onFileItemClickListener,
        ChatAdapter.OnMessageClickListener onMessageClickListener
    ) {
        this.item = item;
        boolean isLocalFile = item.getAttachment().getLocalAttachment() != null;
        super.setData(isLocalFile, item.isFileExists(), item.isDownloading(), item.getAttachment(), onFileItemClickListener);
        if (isLocalFile) {
            itemView.setOnClickListener(view -> onMessageClickListener.onMessageClick(item.getId()));
        }
        setShowDelivered(item.getShowDelivered());
        setShowError(item.getShowError());
        setAccessibilityLabels(item.getShowDelivered());
    }

    private void setupDeliveredView(Context context, UiTheme uiTheme) {
        if (uiTheme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
            deliveredView.setTypeface(fontFamily);
        }
        deliveredView.setText(stringProvider.getRemoteString(R.string.chat_message_delivered));
        Integer baseNormalColor = uiTheme.getBaseNormalColor();
        if (baseNormalColor != null) {
            deliveredView.setTextColor(ContextCompat.getColor(context, baseNormalColor));
        }
    }

    private void setupErrorView(Context context, UiTheme uiTheme) {
        if (uiTheme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
            errorView.setTypeface(fontFamily);
        }
        errorView.setText(stringProvider.getRemoteString(R.string.chat_message_delivery_failed_retry));
        Integer systemNegativeColor = uiTheme.getSystemNegativeColor();
        if (systemNegativeColor != null) {
            errorView.setTextColor(ContextCompat.getColor(context, systemNegativeColor));
        }
    }

    private void setShowDelivered(boolean showDelivered) {
        deliveredView.setVisibility(!item.getShowError() && showDelivered ? View.VISIBLE : View.GONE);
    }

    private void setShowError(boolean showError) {
        errorView.setVisibility(showError ? View.VISIBLE : View.GONE);
    }

    private void setAccessibilityLabels(boolean showDelivered) {
        String name = getAttachmentName(item.getAttachment());
        long size = getAttachmentSize(item.getAttachment());
        String byteSize = Formatter.formatFileSize(itemView.getContext(), size);
        int stringKey;
        if (item.getShowError()) {
            stringKey = R.string.android_chat_visitor_file_not_delivered_accessibility;
        } else if (showDelivered) {
            stringKey = R.string.android_chat_visitor_file_delivered_accessibility;
        } else {
            stringKey = R.string.android_chat_visitor_file_accessibility;
        }
        itemView.setContentDescription(stringProvider.getRemoteString(
            stringKey,
            new StringKeyPair(StringKey.NAME, name),
            new StringKeyPair(StringKey.SIZE, byteSize)
            )
        );

        ViewCompat.setAccessibilityDelegate(itemView, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);

                String actionLabel;
                if (item.getShowError()) {
                    actionLabel = stringProvider.getRemoteString(R.string.general_retry);
                } else if (item.isFileExists()) {
                    actionLabel = stringProvider.getRemoteString(R.string.general_open);
                } else {
                    actionLabel = stringProvider.getRemoteString(R.string.general_download);
                }

                AccessibilityNodeInfoCompat.AccessibilityActionCompat actionClick
                    = new AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel);
                info.addAction(actionClick);
            }
        });
    }

    public void updateDelivered(boolean delivered) {
        setShowDelivered(delivered);
        setAccessibilityLabels(delivered);
    }
}
