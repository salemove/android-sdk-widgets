package com.glia.widgets.chat.model.history;

import androidx.annotation.NonNull;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.helper.Utils;

import java.util.Objects;

public class VisitorAttachmentItem extends ChatItem {

    public final AttachmentFile attachmentFile;
    public final boolean isFileExists;
    public final boolean isDownloading;
    public final boolean showDelivered;

    public VisitorAttachmentItem(String chatItemId, int viewType, AttachmentFile attachmentFile,
                                 boolean isFileExists, boolean isDownloading, boolean showDelivered) {
        super(chatItemId, viewType);
        this.attachmentFile = attachmentFile;
        this.isFileExists = isFileExists;
        this.isDownloading = isDownloading;
        this.showDelivered = showDelivered;
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
