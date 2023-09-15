package com.glia.widgets.chat.adapter.holder.imageattachment;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.R;
import com.glia.widgets.StringProvider;
import com.glia.widgets.UiTheme;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;

public class VisitorImageAttachmentViewHolder extends ImageAttachmentViewHolder {
    private final TextView deliveredView;
    private final StringProvider stringProvider = Dependencies.getStringProvider();

    public VisitorImageAttachmentViewHolder(
        @NonNull View itemView,
        UiTheme uiTheme,
        GetImageFileFromCacheUseCase getImageFileFromCacheUseCase,
        GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase,
        GetImageFileFromNetworkUseCase getImageFileFromNetworkUseCase
    ) {
        super(itemView, getImageFileFromCacheUseCase, getImageFileFromDownloadsUseCase, getImageFileFromNetworkUseCase);
        deliveredView = itemView.findViewById(R.id.delivered_view);
        deliveredView.setText(stringProvider.getRemoteString(R.string.chat_status_delivered));
        setupDeliveredView(itemView.getContext(), uiTheme);
    }

    public void bind(AttachmentFile attachmentFile, boolean showDelivered) {
        super.bind(attachmentFile);
        setShowDelivered(showDelivered);

        setAccessibilityLabels(showDelivered);
    }

    private void setAccessibilityLabels(boolean showDelivered) {
        if (showDelivered) {
            itemView.setContentDescription(stringProvider.getRemoteString(
                R.string.android_chat_visitor_delivered_accessibility_image));
        } else {
            itemView.setContentDescription(stringProvider.getRemoteString(
                R.string.android_chat_visitor_accessibility_image));
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

    public void updateDelivered(boolean delivered) {
        deliveredView.setVisibility(delivered ? View.VISIBLE : View.GONE);
    }
}
