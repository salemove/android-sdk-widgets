package com.glia.widgets.chat.adapter.holder.fileattachment;

import android.text.format.Formatter;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.glia.widgets.R;
import com.glia.widgets.StringKey;
import com.glia.widgets.StringKeyPair;
import com.glia.widgets.StringProvider;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.model.OperatorAttachmentItem;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.view.OperatorStatusView;

public class OperatorFileAttachmentViewHolder extends FileAttachmentViewHolder {
    private final OperatorStatusView operatorStatusView;
    private final StringProvider stringProvider = Dependencies.getStringProvider();

    public OperatorFileAttachmentViewHolder(@NonNull View itemView, UiTheme uiTheme) {
        super(itemView);
        operatorStatusView = itemView.findViewById(R.id.chat_head_view);
        setupOperatorStatusView(uiTheme);
    }

    public void bind(OperatorAttachmentItem.File item, ChatAdapter.OnFileItemClickListener listener) {
        super.setData(item.isFileExists(), item.isDownloading(), item.getAttachment(), listener);
        updateOperatorStatusView(item);
    }

    private void setupOperatorStatusView(UiTheme uiTheme) {
        operatorStatusView.setTheme(uiTheme);
        operatorStatusView.setShowRippleAnimation(false);
    }

    private void updateOperatorStatusView(OperatorAttachmentItem.File item) {
        operatorStatusView.setVisibility(item.getShowChatHead() ? View.VISIBLE : View.GONE);
        if (item.getOperatorProfileImgUrl() != null) {
            operatorStatusView.showProfileImage(item.getOperatorProfileImgUrl());
        } else {
            operatorStatusView.showPlaceholder();
        }

        String name = getAttachmentName(item.getAttachment());
        long size = getAttachmentSize(item.getAttachment());
        String byteSize = Formatter.formatFileSize(itemView.getContext(), size);
        itemView.setContentDescription(
            stringProvider.getRemoteString(
                R.string.android_chat_operator_file_accessibility,
                new StringKeyPair(StringKey.NAME, name),
                new StringKeyPair(StringKey.SIZE, byteSize)
            )
        );

        ViewCompat.setAccessibilityDelegate(itemView, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(@NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
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
}
