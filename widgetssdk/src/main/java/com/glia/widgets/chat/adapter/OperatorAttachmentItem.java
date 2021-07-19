package com.glia.widgets.chat.adapter;

import androidx.annotation.NonNull;

import com.glia.androidsdk.chat.AttachmentFile;

import java.util.Objects;

public class OperatorAttachmentItem extends ChatItem {

    public final boolean showChatHead;
    public final AttachmentFile attachmentFile;
    public final String operatorProfileImgUrl;
    public final boolean isFileExists;
    public final boolean isDownloading;

    public OperatorAttachmentItem(String id, int viewType, boolean showChatHead, AttachmentFile attachmentFile, String operatorProfileImgUrl, boolean isFileExists, boolean isDownloading) {
        super(id, viewType);
        this.showChatHead = showChatHead;
        this.attachmentFile = attachmentFile;
        this.operatorProfileImgUrl = operatorProfileImgUrl;
        this.isFileExists = isFileExists;
        this.isDownloading = isDownloading;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OperatorAttachmentItem that = (OperatorAttachmentItem) o;
        return showChatHead == that.showChatHead &&
                Objects.equals(attachmentFile, that.attachmentFile) &&
                Objects.equals(operatorProfileImgUrl, that.operatorProfileImgUrl) &&
                Objects.equals(isFileExists, that.isFileExists) &&
                Objects.equals(isDownloading, that.isDownloading);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), showChatHead, attachmentFile, operatorProfileImgUrl, isFileExists, isDownloading);
    }

    @NonNull
    @Override
    public String toString() {
        return "OperatorAttachmentItem{" +
                ", showChatHead=" + showChatHead +
                ", attachmentFile='" + attachmentFile + '\'' +
                ", operatorProfileImgUrl='" + operatorProfileImgUrl + '\'' +
                ", isFileExists='" + isFileExists + '\'' +
                ", isDownloading='" + isDownloading + '\'' +
                +'}';
    }
}
