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
import com.glia.widgets.UiTheme;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;

public class VisitorImageAttachmentViewHolder extends ImageAttachmentViewHolder {
    private final TextView deliveredView;

    public VisitorImageAttachmentViewHolder(
            @NonNull View itemView,
            UiTheme uiTheme,
            GetImageFileFromCacheUseCase getImageFileFromCacheUseCase,
            GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase,
            GetImageFileFromNetworkUseCase getImageFileFromNetworkUseCase
    ) {
        super(itemView, getImageFileFromCacheUseCase, getImageFileFromDownloadsUseCase, getImageFileFromNetworkUseCase);
        deliveredView = itemView.findViewById(R.id.delivered_view);
        setupDeliveredView(itemView.getContext(), uiTheme);
    }

    public void bind(AttachmentFile attachmentFile, boolean showDelivered) {
        super.bind(attachmentFile);
        setShowDelivered(showDelivered);

        setAccessibilityLabels(showDelivered);
    }

    private void setAccessibilityLabels(boolean showDelivered) {
        if (showDelivered) {
            itemView.setContentDescription(itemView.getResources().getString(
                    R.string.glia_chat_visitor_image_delivered_content_description));
        } else {
            itemView.setContentDescription(itemView.getResources().getString(
                    R.string.glia_chat_visitor_image_content_description));
        }
    }

    private void setShowDelivered(boolean showDelivered) {
        deliveredView.setVisibility(showDelivered ? View.VISIBLE : View.GONE);
    }

    private void setupDeliveredView(Context context, UiTheme uiTheme) {
        if (uiTheme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
            deliveredView.setTypeface(fontFamily);
        }
        deliveredView.setTextColor(ContextCompat.getColor(context, uiTheme.getBaseNormalColor()));
    }
}
