package com.glia.widgets.chat.model.history;

import androidx.annotation.NonNull;

import com.glia.androidsdk.chat.AttachmentFile;

import java.util.Objects;

public class VisitorAttachmentItem extends ChatItem {

    public final AttachmentFile attachmentFile;
    public final boolean isFileExists;
    public final boolean isDownloading;

    public VisitorAttachmentItem(String id, int viewType, AttachmentFile attachmentFile, boolean isFileExists, boolean isDownloading) {
        super(id, viewType);
        this.attachmentFile = attachmentFile;
        this.isFileExists = isFileExists;
        this.isDownloading = isDownloading;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OperatorAttachmentItem that = (OperatorAttachmentItem) o;
        return Objects.equals(attachmentFile, that.attachmentFile) &&
                Objects.equals(isFileExists, that.isFileExists) &&
                Objects.equals(isDownloading, that.isDownloading);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), attachmentFile, isFileExists, isDownloading);
    }

    @NonNull
    @Override
    public String toString() {
        return "OperatorAttachmentItem{" +
                ", attachmentFile='" + attachmentFile + '\'' +
                ", isFileExists='" + isFileExists + '\'' +
                ", isDownloading='" + isDownloading + '\'' +
                +'}';
    }
}
