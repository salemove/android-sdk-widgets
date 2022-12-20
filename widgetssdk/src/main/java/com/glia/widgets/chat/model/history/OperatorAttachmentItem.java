package com.glia.widgets.chat.model.history;

import androidx.annotation.NonNull;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.helper.Utils;

import java.util.Objects;

public class OperatorAttachmentItem extends OperatorChatItem {

    public final AttachmentFile attachmentFile;
    public final boolean isFileExists;
    public final boolean isDownloading;

    public OperatorAttachmentItem(
            String chatItemId,
            int viewType,
            boolean showChatHead,
            AttachmentFile attachmentFile,
            String operatorProfileImgUrl,
            boolean isFileExists,
            boolean isDownloading,
            String operatorId,
            String messageId,
            long timestamp
    ) {
        super(chatItemId, viewType, showChatHead, operatorProfileImgUrl, operatorId, messageId, timestamp);
        this.attachmentFile = attachmentFile;
        this.isFileExists = isFileExists;
        this.isDownloading = isDownloading;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperatorAttachmentItem)) return false;
        if (!super.equals(o)) return false;
        OperatorAttachmentItem that = (OperatorAttachmentItem) o;
        return isFileExists == that.isFileExists && isDownloading == that.isDownloading && Objects.equals(attachmentFile, that.attachmentFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), attachmentFile, isFileExists, isDownloading);
    }

    @Override
    public String toString() {
        return "OperatorAttachmentItem{" +
                "attachmentFile=" + attachmentFile +
                ", isFileExists=" + isFileExists +
                ", isDownloading=" + isDownloading +
                ", showChatHead=" + showChatHead +
                ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                ", operatorId='" + operatorId + '\'' +
                "} " + super.toString();
    }
}
