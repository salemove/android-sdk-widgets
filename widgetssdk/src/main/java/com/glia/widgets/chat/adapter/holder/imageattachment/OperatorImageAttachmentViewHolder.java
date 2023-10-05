package com.glia.widgets.chat.adapter.holder.imageattachment;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.glia.widgets.R;
import com.glia.widgets.StringProvider;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.model.OperatorAttachmentItem;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;
import com.glia.widgets.view.OperatorStatusView;

public class OperatorImageAttachmentViewHolder extends ImageAttachmentViewHolder {
    private final OperatorStatusView operatorStatusView;
    private final StringProvider stringProvider = Dependencies.getStringProvider();

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
        operatorStatusView.setShowRippleAnimation(false);
    }

    public void bind(OperatorAttachmentItem.Image item, ChatAdapter.OnImageItemClickListener onImageItemClickListener) {
        super.bind(item.getAttachmentFile());
        itemView.setOnClickListener(v -> onImageItemClickListener.onImageItemClick(item.getAttachmentFile(), v));
        updateOperatorStatus(item);

        setAccessibilityLabels();
    }

    private void setAccessibilityLabels() {
        itemView.setContentDescription(stringProvider.getRemoteString(
                R.string.android_chat_operator_image_attachment_accessibility));
        ViewCompat.setAccessibilityDelegate(itemView, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(@NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);

                String actionLabel = stringProvider.getRemoteString(R.string.general_open);

                AccessibilityNodeInfoCompat.AccessibilityActionCompat actionClick
                        = new AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                        AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel);
                info.addAction(actionClick);
            }
        });
    }

    private void updateOperatorStatus(OperatorAttachmentItem item) {
        operatorStatusView.setVisibility(item.getShowChatHead() ? View.VISIBLE : View.GONE);
        if (item.getOperatorProfileImgUrl() != null) {
            operatorStatusView.showProfileImage(item.getOperatorProfileImgUrl());
        } else {
            operatorStatusView.showPlaceholder();
        }
    }
}
