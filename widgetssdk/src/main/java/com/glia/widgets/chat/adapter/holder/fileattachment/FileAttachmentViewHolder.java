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
import com.glia.widgets.StringProvider;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.helper.FileHelper;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class FileAttachmentViewHolder extends RecyclerView.ViewHolder {
    private final CardView extensionContainerView;
    private final TextView extensionTypeText;
    private final LinearProgressIndicator progressIndicator;
    private final TextView titleText;
    private final TextView statusIndicator;
    private final StringProvider stringProvider = Dependencies.getStringProvider();

    public FileAttachmentViewHolder(@NonNull View itemView) {
        super(itemView);
        extensionContainerView = itemView.findViewById(R.id.type_indicator_view);
        extensionTypeText = itemView.findViewById(R.id.type_indicator_text);
        progressIndicator = itemView.findViewById(R.id.progress_indicator);
        titleText = itemView.findViewById(R.id.item_title);
        statusIndicator = itemView.findViewById(R.id.status_indicator);
        statusIndicator.setContentDescription(stringProvider.getRemoteString(R.string.general_download));

        setupExtensionContainer(itemView);
    }

    protected void setData(
            boolean isFileExists,
            boolean isDownloading,
            AttachmentFile attachmentFile,
            ChatAdapter.OnFileItemClickListener listener
    ) {
        updateClickListener(isFileExists, isDownloading, attachmentFile, listener);
        updateTitle(attachmentFile);
        updateStatusIndicator(isFileExists, isDownloading);
        updateProgressIndicator(isDownloading);
    }

    private void setupExtensionContainer(@NonNull View itemView) {
        extensionContainerView.setCardBackgroundColor(
                ContextCompat.getColor(
                        itemView.getContext(),
                        R.color.glia_brand_primary_color
                )
        );
    }

    private void updateClickListener(
            boolean isFileExists,
            boolean isDownloading,
            AttachmentFile attachmentFile,
            ChatAdapter.OnFileItemClickListener listener
    ) {
        if (isDownloading) {
            itemView.setOnClickListener(null);
        } else {
            itemView.setOnClickListener(v -> {
                if (isFileExists) {
                    listener.onFileOpenClick(attachmentFile);
                } else {
                    listener.onFileDownloadClick(attachmentFile);
                }
            });
        }
    }

    private void updateTitle(AttachmentFile attachmentFile) {
        String name = attachmentFile.getName();
        long byteSize = attachmentFile.getSize();
        titleText.setText(String.format("%s • %s", name, Formatter.formatFileSize(itemView.getContext(), byteSize)));
        extensionTypeText.setText(FileHelper.toFileExtensionOrEmpty(name).toUpperCase());
    }

    private void updateStatusIndicator(boolean isFileExists, boolean isDownloading) {
        if (isDownloading) {
            statusIndicator.setText(stringProvider.getRemoteString(R.string.chat_download_downloading));
        } else if (isFileExists) {
            statusIndicator.setText(stringProvider.getRemoteString(R.string.general_open));
        } else {
            statusIndicator.setText(stringProvider.getRemoteString(R.string.general_download));
        }
    }

    private void updateProgressIndicator(boolean isDownloading) {
        progressIndicator.setVisibility(isDownloading ? View.VISIBLE : View.GONE);
    }
}
