package com.glia.widgets.chat.adapter.holder;

import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.model.history.OperatorAttachmentItem;
import com.glia.widgets.chat.model.history.VisitorAttachmentItem;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.OperatorStatusView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class FileAttachmentViewHolder extends RecyclerView.ViewHolder {

    private final OperatorStatusView operatorStatusView;

    public FileAttachmentViewHolder(@NonNull View itemView, UiTheme uiTheme) {
        super(itemView);

        operatorStatusView = itemView.findViewById(R.id.chat_head_view);
        operatorStatusView.setTheme(uiTheme);
        operatorStatusView.isRippleAnimationShowing(false);
    }

    public void bind(OperatorAttachmentItem item) {
        setData(item.isFileExists, item.isDownloading, item.attachmentFile, item.showChatHead, item.operatorProfileImgUrl);
    }

    public void bind(VisitorAttachmentItem item) {
        setData(item.isFileExists, item.isDownloading, item.attachmentFile, false, null);
    }

    public void setData(boolean isFileExists, boolean isDownloading, AttachmentFile attachmentFile, boolean showChatHead, String operatorProfileImgUrl) {
        CardView extensionContainerView = itemView.findViewById(R.id.type_indicator_view);
        TextView extensionTypeText = itemView.findViewById(R.id.type_indicator_text);
        LinearProgressIndicator progressIndicator = itemView.findViewById(R.id.progress_indicator);
        TextView titleText = itemView.findViewById(R.id.item_title);
        TextView statusIndicator = itemView.findViewById(R.id.status_indicator);

        if (isFileExists) {
            statusIndicator.setText(R.string.glia_chat_attachment_open_button_label);
        } else {
            statusIndicator.setText(R.string.glia_chat_attachment_download_button_label);
        }

        if (isDownloading) {
            statusIndicator.setText(R.string.glia_chat_attachment_downloading_label);
            progressIndicator.setVisibility(View.VISIBLE);
        } else {
            progressIndicator.setVisibility(View.GONE);
            if (isFileExists) {
                statusIndicator.setText(R.string.glia_chat_attachment_open_button_label);
            } else {
                statusIndicator.setText(R.string.glia_chat_attachment_download_button_label);
            }
        }

        String name = attachmentFile.getName();
        long byteSize = attachmentFile.getSize();

        extensionContainerView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.glia_brand_primary_color));
        titleText.setText(String.format("%s • %s", name, Formatter.formatFileSize(itemView.getContext(), byteSize)));
        String extension = Utils.getExtensionByStringHandling(name).orElse("");
        extensionTypeText.setText(extension);

        operatorStatusView.setVisibility(showChatHead ? View.VISIBLE : View.GONE);
        if (operatorProfileImgUrl != null) {
            operatorStatusView.showProfileImage(operatorProfileImgUrl);
        } else {
            operatorStatusView.showPlaceHolder();
        }
    }
}
