package com.glia.widgets.chat.model.history;

import androidx.annotation.NonNull;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.chat.adapter.ChatAdapter;
import com.glia.widgets.helper.Utils;

import java.util.Objects;

public class VisitorAttachmentItem extends ParticipantMessageChatItem {

    public final AttachmentFile attachmentFile;
    public final boolean isFileExists;
    public final boolean isDownloading;
    public final boolean showDelivered;

    private VisitorAttachmentItem(String chatItemId, int viewType, AttachmentFile attachmentFile,
                                 boolean isFileExists, boolean isDownloading, boolean showDelivered, long timestamp) {
        super(chatItemId, viewType, chatItemId, timestamp);
        this.attachmentFile = attachmentFile;
        this.isFileExists = isFileExists;
        this.isDownloading = isDownloading;
        this.showDelivered = showDelivered;
    }

    public static VisitorAttachmentItem editDeliveredStatus(VisitorAttachmentItem source, boolean isDelivered) {
        return new VisitorAttachmentItem(
                source.getId(),
                source.getViewType(),
                source.attachmentFile,
                source.isFileExists,
                source.isDownloading,
                isDelivered,
                source.getTimestamp()
        );
    }

    public static VisitorAttachmentItem editDownloadedStatus(VisitorAttachmentItem source, boolean isDownloaded) {
        return new VisitorAttachmentItem(
                source.getId(),
                source.getViewType(),
                source.attachmentFile,
                isDownloaded,
                source.isDownloading,
                source.showDelivered,
                source.getTimestamp()
        );
    }

    public static VisitorAttachmentItem editFileStatuses(VisitorAttachmentItem source, boolean doesFileExists, boolean isDownloading) {
        return new VisitorAttachmentItem(
                source.getId(),
                source.getViewType(),
                source.attachmentFile,
                doesFileExists,
                isDownloading,
                source.showDelivered,
                source.getTimestamp()
        );
    }

    public static VisitorAttachmentItem fromAttachmentFile(String messageId, long messageTimestamp, AttachmentFile file) {
        int type;
        if (file.getContentType().startsWith("image")) {
            type = ChatAdapter.VISITOR_IMAGE_VIEW_TYPE;
        } else {
            type = ChatAdapter.VISITOR_FILE_VIEW_TYPE;
        }
        return new VisitorAttachmentItem(messageId, type, file, false, false, false, messageTimestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VisitorAttachmentItem that = (VisitorAttachmentItem) o;
        return isFileExists == that.isFileExists && isDownloading == that.isDownloading &&
                showDelivered == that.showDelivered && Objects.equals(attachmentFile, that.attachmentFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), attachmentFile, isFileExists, isDownloading, showDelivered);
    }

    @NonNull
    @Override
    public String toString() {
        return "VisitorAttachmentItem{" +
                "attachmentFile=" + Utils.toString(attachmentFile) +
                ", chatItemId=" + getId() +
                ", isFileExists=" + isFileExists +
                ", isDownloading=" + isDownloading +
                ", showDelivered=" + showDelivered +
                '}';
    }
}
