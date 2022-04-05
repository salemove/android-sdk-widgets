package com.glia.widgets.chat.adapter.holder.fileattachment;

import android.text.format.Formatter;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.model.history.OperatorAttachmentItem;
import com.glia.widgets.view.OperatorStatusView;

public class OperatorFileAttachmentViewHolder extends FileAttachmentViewHolder {
    private final OperatorStatusView operatorStatusView;

    public OperatorFileAttachmentViewHolder(@NonNull View itemView, UiTheme uiTheme) {
        super(itemView);
        operatorStatusView = itemView.findViewById(R.id.chat_head_view);
        setupOperatorStatusView(uiTheme);
    }

    public void bind(OperatorAttachmentItem item, ChatAdapter.OnFileItemClickListener listener) {
        super.setData(item.isFileExists, item.isDownloading, item.attachmentFile, listener);
        updateOperatorStatusView(item);
    }

    private void setupOperatorStatusView(UiTheme uiTheme) {
        operatorStatusView.setTheme(uiTheme);
        operatorStatusView.setShowRippleAnimation(false);
    }

    private void updateOperatorStatusView(OperatorAttachmentItem item) {
        operatorStatusView.setVisibility(item.showChatHead ? View.VISIBLE : View.GONE);
        if (item.operatorProfileImgUrl != null) {
            operatorStatusView.showProfileImage(item.operatorProfileImgUrl);
        } else {
            operatorStatusView.showPlaceHolderWithIconPadding();
        }

        String name = item.attachmentFile.getName();
        String byteSize = Formatter.formatFileSize(itemView.getContext(), item.attachmentFile.getSize());
        itemView.setContentDescription(itemView.getResources().getString(R.string.glia_chat_operator_file_content_description, name, byteSize));

        ViewCompat.setAccessibilityDelegate(itemView, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);

                String actionLabel = host.getResources().getString(item.isFileExists
                        ? R.string.glia_chat_attachment_open_button_label
                        : R.string.glia_chat_attachment_download_button_label);

                AccessibilityNodeInfoCompat.AccessibilityActionCompat actionClick
                        = new AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                                AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel);
                info.addAction(actionClick);
            }
        });
    }
}
