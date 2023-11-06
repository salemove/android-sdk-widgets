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
import com.glia.widgets.chat.model.Attachment;
import com.glia.widgets.core.fileupload.model.FileAttachment;
import com.glia.widgets.helper.FileHelper;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class FileAttachmentViewHolder extends RecyclerView.ViewHolder {
    private final CardView extensionContainerView;
    private final TextView extensionTypeText;
    private final LinearProgressIndicator progressIndicator;
    private final TextView titleText;
    private final TextView statusIndicator;
    private final StringProvider stringProvider;

    public FileAttachmentViewHolder(@NonNull View itemView, @NonNull StringProvider stringProvider) {
        super(itemView);
        this.stringProvider = stringProvider;
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
        Attachment attachment,
        ChatAdapter.OnFileItemClickListener listener
    ) {
        setData(false, isFileExists, isDownloading, attachment, listener);
    }

    protected void setData(
            boolean isLocalFile,
            boolean isFileExists,
            boolean isDownloading,
            Attachment attachment,
            ChatAdapter.OnFileItemClickListener listener
    ) {
        updateClickListener(isFileExists, isDownloading, attachment, listener);
        updateTitle(attachment);
        updateStatusIndicator(isLocalFile, isFileExists, isDownloading);
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
            Attachment attachment,
            ChatAdapter.OnFileItemClickListener listener
    ) {
        AttachmentFile attachmentFile = attachment.getRemoteAttachment();
        if (isDownloading || attachmentFile == null) {
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

    private void updateTitle(Attachment attachment) {
        String name = getAttachmentName(attachment);
        long byteSize = getAttachmentSize(attachment);
        titleText.setText(String.format("%s • %s", name, Formatter.formatFileSize(itemView.getContext(), byteSize)));
        extensionTypeText.setText(FileHelper.toFileExtensionOrEmpty(name).toUpperCase());
    }

    public String getAttachmentName(Attachment attachment) {
        AttachmentFile attachmentFile = attachment.getRemoteAttachment();
        if (attachmentFile != null) {
            return attachmentFile.getName();
        }
        FileAttachment fileAttachment = attachment.getLocalAttachment();
        if (fileAttachment != null) {
            return fileAttachment.getDisplayName();
        }
        return "";
    }

    public long getAttachmentSize(Attachment attachment) {
        AttachmentFile attachmentFile = attachment.getRemoteAttachment();
        if (attachmentFile != null) {
            return attachmentFile.getSize();
        }
        FileAttachment fileAttachment = attachment.getLocalAttachment();
        if (fileAttachment != null) {
            return fileAttachment.getSize();
        }
        return 0;
    }

    private void updateStatusIndicator(boolean isLocalFile, boolean isFileExists, boolean isDownloading) {
        statusIndicator.setVisibility(isLocalFile ? View.GONE : View.VISIBLE);

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
