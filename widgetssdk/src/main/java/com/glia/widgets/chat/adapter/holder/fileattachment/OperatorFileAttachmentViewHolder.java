package com.glia.widgets.chat.adapter.holder.fileattachment;

import android.view.View;

import androidx.annotation.NonNull;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.model.history.OperatorAttachmentItem;
import com.glia.widgets.view.OperatorStatusView;

public class OperatorFileAttachmentViewHolder extends FileAttachmentViewHolder {
    private final OperatorStatusView operatorStatusView;

    public OperatorFileAttachmentViewHolder(@NonNull View itemView, UiTheme uiTheme) {
        super(itemView);
        operatorStatusView = itemView.findViewById(R.id.chat_head_view);
        setupOperatorStatusView(uiTheme);
    }

    private void setupOperatorStatusView(UiTheme uiTheme) {
        operatorStatusView.setTheme(uiTheme);
        operatorStatusView.isRippleAnimationShowing(false);
    }

    public void bind(OperatorAttachmentItem item) {
        super.setData(item.isFileExists, item.isDownloading, item.attachmentFile);
        operatorStatusView.setVisibility(item.showChatHead ? View.VISIBLE : View.GONE);
        if (item.operatorProfileImgUrl != null) {
            operatorStatusView.showProfileImage(item.operatorProfileImgUrl);
        } else {
            operatorStatusView.showPlaceHolder();
        }
    }
}
