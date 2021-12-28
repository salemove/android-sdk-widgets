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
import com.glia.widgets.helper.Utils;
import com.google.android.material.progressindicator.LinearProgressIndicator;

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
    }

    protected void setData(
            boolean isFileExists,
            boolean isDownloading,
            AttachmentFile attachmentFile
    ) {
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
    }
}
