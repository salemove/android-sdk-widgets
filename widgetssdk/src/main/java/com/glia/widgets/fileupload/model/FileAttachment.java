package com.glia.widgets.fileupload.model;

import android.net.Uri;

import com.glia.androidsdk.engagement.EngagementFile;

final public class FileAttachment {
    private final Uri uri;
    private final EngagementFile engagementFile;
    private final FileAttachment.Status attachmentStatus;

    public FileAttachment(Uri uri) {
        this.uri = uri;
        this.engagementFile = null;
        this.attachmentStatus = FileAttachment.Status.UPLOADING;
    }

    public FileAttachment(FileAttachment attachment, FileAttachment.Status status) {
        this.uri = attachment.uri;
        this.attachmentStatus = status;
        this.engagementFile = attachment.engagementFile;
    }

    public FileAttachment(FileAttachment attachment, EngagementFile engagementFile) {
        this.uri = attachment.uri;
        this.attachmentStatus = attachment.attachmentStatus;
        this.engagementFile = engagementFile;
    }

    public FileAttachment setEngagementFile(EngagementFile engagementFile) {
        return new FileAttachment(this, engagementFile);
    }

    public FileAttachment setAttachmentStatus(FileAttachment.Status status) {
        return new FileAttachment(this, status);
    }

    public EngagementFile getEngagementFile() {
        return this.engagementFile;
    }

    public Uri getUri() {
        return uri;
    }

    public FileAttachment.Status getAttachmentStatus() {
        return this.attachmentStatus;
    }

    public boolean isReadyToSend() {
        return this.attachmentStatus == FileAttachment.Status.READY_TO_SEND;
    }

    public enum Status {
        UPLOADING,
        SECURITY_SCAN,
        READY_TO_SEND,

        ERROR_NETWORK_TIMEOUT,
        ERROR_INTERNAL,
        ERROR_INVALID_INPUT,
        ERROR_PERMISSIONS_DENIED,
        ERROR_FORMAT_UNSUPPORTED,
        ERROR_FILE_TOO_LARGE,
        ERROR_ENGAGEMENT_MISSING,
        ERROR_UNKNOWN
    }
}
