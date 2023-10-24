package com.glia.widgets.chat.adapter.holder.imageattachment;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.R;
import com.glia.widgets.StringProvider;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.chat.model.VisitorAttachmentItem;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;

public class VisitorImageAttachmentViewHolder extends ImageAttachmentViewHolder {
    private final TextView deliveredView;
    private final TextView errorView;
    private final StringProvider stringProvider = Dependencies.getStringProvider();
    private VisitorAttachmentItem.Image item;

    public VisitorImageAttachmentViewHolder(
        @NonNull View itemView,
        UiTheme uiTheme,
        GetImageFileFromCacheUseCase getImageFileFromCacheUseCase,
        GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase,
        GetImageFileFromNetworkUseCase getImageFileFromNetworkUseCase
    ) {
        super(itemView, getImageFileFromCacheUseCase, getImageFileFromDownloadsUseCase, getImageFileFromNetworkUseCase);
        deliveredView = itemView.findViewById(R.id.delivered_view);
        deliveredView.setText(stringProvider.getRemoteString(R.string.chat_message_delivered));
        errorView = itemView.findViewById(R.id.error_view);
        errorView.setText(stringProvider.getRemoteString(R.string.chat_message_delivery_failed_retry));
        setupUiTheme(itemView.getContext(), uiTheme);
    }

    public void bind(
        VisitorAttachmentItem.Image item,
        ChatAdapter.OnImageItemClickListener onImageItemClickListener,
        ChatAdapter.OnMessageClickListener onMessageClickListener
    ) {
        this.item = item;
        super.bind(item.getAttachment());
        itemView.setOnClickListener(view -> {
            AttachmentFile attachmentFile = item.getAttachment().getRemoteAttachment();
            if (attachmentFile != null) {
                onImageItemClickListener.onImageItemClick(attachmentFile, view);
            } else {
                onMessageClickListener.onMessageClick(item.getId());
            }
        });

        setShowError(item.getShowError());
        setShowDelivered(item.getShowDelivered());

        setAccessibilityLabels(item.getShowError(), item.getShowDelivered());
    }

    private void setAccessibilityLabels(boolean showError, boolean showDelivered) {
        if (showError) {
            itemView.setContentDescription(stringProvider.getRemoteString(
                R.string.android_chat_visitor_image_attachment_not_delivered_accessibility));
        } else if (showDelivered) {
            itemView.setContentDescription(stringProvider.getRemoteString(
                R.string.android_chat_visitor_image_attachment_delivered_accessibility));
        } else {
            itemView.setContentDescription(stringProvider.getRemoteString(
                R.string.android_chat_visitor_image_attachment_accessibility));
        }

        ViewCompat.setAccessibilityDelegate(itemView, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(@NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);

                String actionLabel = stringProvider.getRemoteString(
                    showError
                        ? R.string.general_retry
                        : R.string.general_open
                );

                AccessibilityNodeInfoCompat.AccessibilityActionCompat actionClick
                    = new AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, actionLabel);
                info.addAction(actionClick);
            }
        });
    }

    private void setShowDelivered(boolean showDelivered) {
        deliveredView.setVisibility(!item.getShowError() && showDelivered ? View.VISIBLE : View.GONE);
    }

    private void setShowError(boolean showError) {
        errorView.setVisibility(showError ? View.VISIBLE : View.GONE);
    }

    private void setupUiTheme(Context context, UiTheme uiTheme) {
        if (uiTheme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
            deliveredView.setTypeface(fontFamily);
            errorView.setTypeface(fontFamily);
        }
        Integer baseNormalColor = uiTheme.getBaseNormalColor();
        if (baseNormalColor != null) {
            deliveredView.setTextColor(ContextCompat.getColor(context, baseNormalColor));
        }
        Integer systemNegativeColor = uiTheme.getSystemNegativeColor();
        if (systemNegativeColor != null) {
            errorView.setTextColor(ContextCompat.getColor(context, systemNegativeColor));
        }
    }

    public void updateDelivered(boolean delivered) {
        setShowDelivered(delivered);
        setAccessibilityLabels(item.getShowError(), delivered);
    }
}
