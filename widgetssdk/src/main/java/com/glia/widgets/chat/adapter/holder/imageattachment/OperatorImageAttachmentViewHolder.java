package com.glia.widgets.chat.adapter.holder.imageattachment;

import android.view.View;

import androidx.annotation.NonNull;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.model.history.OperatorAttachmentItem;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;
import com.glia.widgets.view.OperatorStatusView;

public class OperatorImageAttachmentViewHolder extends ImageAttachmentViewHolder {
    private final OperatorStatusView operatorStatusView;

    public OperatorImageAttachmentViewHolder(
            @NonNull View itemView,
            UiTheme uiTheme,
            GetImageFileFromCacheUseCase getImageFileFromCacheUseCase,
            GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase,
            GetImageFileFromNetworkUseCase getImageFileFromNetworkUseCase
    ) {
        super(itemView, getImageFileFromCacheUseCase, getImageFileFromDownloadsUseCase, getImageFileFromNetworkUseCase);
        operatorStatusView = itemView.findViewById(R.id.chat_head_view);
        setupOperatorStatus(uiTheme);
    }

    private void setupOperatorStatus(UiTheme uiTheme) {
        operatorStatusView.setTheme(uiTheme);
        operatorStatusView.isRippleAnimationShowing(false);
    }

    public void bind(OperatorAttachmentItem item, ChatAdapter.OnImageItemClickListener onImageItemClickListener) {
        super.bind(item.attachmentFile);
        itemView.setOnClickListener(v -> onImageItemClickListener.onImageItemClick(item.attachmentFile));
        updateOperatorStatus(item);
    }

    private void updateOperatorStatus(OperatorAttachmentItem item) {
        operatorStatusView.setVisibility(item.showChatHead ? View.VISIBLE : View.GONE);
        if (item.operatorProfileImgUrl != null) {
            operatorStatusView.showProfileImage(item.operatorProfileImgUrl);
        } else {
            operatorStatusView.showPlaceHolder();
        }
    }
}
