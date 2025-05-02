package com.glia.widgets.chat.adapter.holder.fileattachment;

import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.R;
import com.glia.widgets.internal.fileupload.model.LocalAttachment;
import com.glia.widgets.helper.FileHelper;
import com.glia.widgets.helper.ViewExtensionsKt;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * @hide
 */
public class FileAttachmentViewHolder extends RecyclerView.ViewHolder {
    private final CardView extensionContainerView;
    private final TextView extensionTypeText;
    private final LinearProgressIndicator progressIndicator;
    private final TextView titleText;
    private final TextView statusIndicator;

    public FileAttachmentViewHolder(@NonNull View itemView) {
        super(itemView);
        extensionContainerView = itemView.findViewById(R.id.type_indicator_view);
        extensionTypeText = itemView.findViewById(R.id.type_indicator_text);
        progressIndicator = itemView.findViewById(R.id.progress_indicator);
        titleText = itemView.findViewById(R.id.item_title);
        statusIndicator = itemView.findViewById(R.id.status_indicator);
        ViewExtensionsKt.setLocaleContentDescription(statusIndicator, R.string.general_download);

        setupExtensionContainer(itemView);
    }

    protected void setData(boolean isFileExists, boolean isDownloading, AttachmentFile attachment) {
        updateTitle(attachment.getName(), attachment.getSize());
        updateStatusIndicator(isFileExists, isDownloading);
        updateProgressIndicator(isDownloading);
    }

    protected void setData(LocalAttachment attachment) {
        updateTitle(attachment.getDisplayName(), attachment.getSize());
        updateStatusIndicator(true, false);
        updateProgressIndicator(false);
    }

    private void setupExtensionContainer(@NonNull View itemView) {
        extensionContainerView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.glia_primary_color));
    }

    private void updateTitle(String name, long byteSize) {
        titleText.setText(String.format("%s • %s", name, Formatter.formatFileSize(itemView.getContext(), byteSize)));
        extensionTypeText.setText(FileHelper.toFileExtensionOrEmpty(name).toUpperCase());
    }

    private void updateStatusIndicator(boolean isFileExists, boolean isDownloading) {
        if (isDownloading) {
            ViewExtensionsKt.setLocaleText(statusIndicator, R.string.chat_download_downloading);
        } else if (isFileExists) {
            ViewExtensionsKt.setLocaleText(statusIndicator, R.string.general_open);
        } else {
            ViewExtensionsKt.setLocaleText(statusIndicator, R.string.general_download);
        }
    }

    private void updateProgressIndicator(boolean isDownloading) {
        progressIndicator.setVisibility(isDownloading ? View.VISIBLE : View.GONE);
    }
}
