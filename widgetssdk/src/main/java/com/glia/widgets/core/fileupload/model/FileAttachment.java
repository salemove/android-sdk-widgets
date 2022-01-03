package com.glia.widgets.core.fileupload.model;

import android.net.Uri;

import com.glia.androidsdk.engagement.EngagementFile;

final public class FileAttachment {
    private final Uri uri;
    private final String mimeType;
    private final String displayName;
    private final long size;
    private final EngagementFile engagementFile;
    private final FileAttachment.Status attachmentStatus;

    public FileAttachment(Uri uri, String displayName, long size, String mimeType) {
        this.attachmentStatus = FileAttachment.Status.UPLOADING;
        this.uri = uri;
        this.engagementFile = null;
        this.displayName = displayName;
        this.size = size;
        this.mimeType = mimeType;
    }

    public FileAttachment(FileAttachment attachment, FileAttachment.Status status) {
        this.uri = attachment.uri;
        this.engagementFile = attachment.engagementFile;
        this.displayName = attachment.displayName;
        this.size = attachment.size;
        this.attachmentStatus = status;
        this.mimeType = attachment.mimeType;
    }

    public FileAttachment(FileAttachment attachment, EngagementFile engagementFile) {
        this.uri = attachment.uri;
        this.attachmentStatus = attachment.attachmentStatus;
        this.displayName = attachment.displayName;
        this.size = attachment.size;
        this.engagementFile = engagementFile;
        this.mimeType = attachment.mimeType;
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

    public String getDisplayName() {
        return this.displayName;
    }

    public long getSize() {
        return this.size;
    }

    public String getMimeType() {
        return this.mimeType;
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
        ERROR_UNKNOWN,
        ERROR_SECURITY_SCAN_FAILED,

        ERROR_FILE_UPLOAD_FORBIDDEN,

        ERROR_SUPPORTED_FILE_ATTACHMENT_COUNT_EXCEEDED
    }
}
