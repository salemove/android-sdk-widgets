package com.glia.widgets.fileupload;

import android.net.Uri;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.Glia;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.engagement.EngagementFile;
import com.glia.widgets.core.engagement.exception.EngagementMissingException;
import com.glia.widgets.fileupload.domain.AddFileToAttachmentAndUploadUseCase;
import com.glia.widgets.fileupload.model.FileAttachment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.exceptions.UndeliverableException;

public class FileAttachmentRepository {
    public long getAttachedFilesCount() {
        return observable
                .fileAttachments
                .size();
    }

    private static class ObservableFileAttachmentList extends Observable {
        public List<FileAttachment> fileAttachments = new ArrayList<>();

        public void notifyUpdate(List<FileAttachment> newObject) {
            this.fileAttachments = newObject;
            setChanged();
            notifyObservers();
        }
    }

    private final ObservableFileAttachmentList observable = new ObservableFileAttachmentList();

    public boolean isFileAttached(Uri uri) {
        return observable.fileAttachments
                .stream()
                .anyMatch(fileAttachment -> fileAttachment.getUri().equals(uri));
    }

    public void attachFile(FileAttachment file) {
        observable.notifyUpdate(
                Stream.concat(
                        observable.fileAttachments.stream(),
                        Stream.of(file)
                ).collect(Collectors.toList()));
    }

    public void uploadFile(FileAttachment file, AddFileToAttachmentAndUploadUseCase.Listener listener) {
        Engagement engagement = Glia.getCurrentEngagement().orElse(null);
        if (engagement != null) {
            engagement.uploadFile(file.getUri(), (engagementFile, e) -> {
                if (engagementFile != null) {
                    if (!engagementFile.isSecurityScanRequired()) {
                        onUploadFileSuccess(file.getUri(), engagementFile, listener);
                    } else {
                        onUploadFileSecurityScanRequired(file.getUri(), engagementFile, listener);
                    }
                } else if (e != null) {
                    onUploadFileError(file.getUri(), e);
                    listener.onError(e);
                }
            });
        } else {
            setFileAttachmentEngagementMissing(file.getUri());
            listener.onError(new EngagementMissingException());
        }
    }

    private void onUploadFileSecurityScanRequired(Uri uri, EngagementFile engagementFile, AddFileToAttachmentAndUploadUseCase.Listener listener) {
        setFileAttachmentSecurityCheckInProgress(uri);
        listener.onSecurityCheckStarted();

        engagementFile.on(EngagementFile.Events.SCAN_RESULT, scanResult -> {
            engagementFile.off(EngagementFile.Events.SCAN_RESULT);
            listener.onSecurityCheckFinished(scanResult);
            onUploadFileSecurityScanReceived(uri, engagementFile, scanResult, listener);
        });
    }

    private void onUploadFileSecurityScanReceived(Uri uri, EngagementFile engagementFile, EngagementFile.ScanResult scanResult, AddFileToAttachmentAndUploadUseCase.Listener listener) {
        if (scanResult == EngagementFile.ScanResult.CLEAN && engagementFile != null) {
            onUploadFileSuccess(uri, engagementFile, listener);
        } else {
            setFileAttachmentSecurityCheckFailed(uri);
            listener.onFinished();
        }
    }

    private void onUploadFileSuccess(Uri uri, EngagementFile engagementFile, AddFileToAttachmentAndUploadUseCase.Listener listener) {
        onEngagementFileReceived(uri, engagementFile);
        listener.onFinished();
    }

    public void setFileAttachmentTooLarge(Uri uri) {
        observable.notifyUpdate(observable.fileAttachments
                .stream()
                .map(fileAttachment ->
                        fileAttachment.getUri() == uri ? fileAttachment.setAttachmentStatus(FileAttachment.Status.ERROR_FILE_TOO_LARGE) : fileAttachment
                )
                .collect(Collectors.toList())
        );
    }

    private void setFileAttachmentSecurityCheckInProgress(Uri uri) {
        observable.notifyUpdate(observable.fileAttachments
                .stream()
                .map(fileAttachment ->
                        fileAttachment.getUri() == uri ? fileAttachment.setAttachmentStatus(FileAttachment.Status.SECURITY_SCAN) : fileAttachment
                ).collect(Collectors.toList())
        );
    }

    private void setFileAttachmentSecurityCheckFailed(Uri uri) {
        observable.notifyUpdate(observable.fileAttachments
                .stream()
                .map(fileAttachment ->
                        fileAttachment.getUri() == uri ? fileAttachment.setAttachmentStatus(FileAttachment.Status.ERROR_SECURITY_SCAN_FAILED) : fileAttachment
                ).collect(Collectors.toList())
        );
    }

    public void setSupportedFileAttachmentCountExceeded(Uri uri) {
        observable.notifyUpdate(observable.fileAttachments
                .stream()
                .map(fileAttachment ->
                        fileAttachment.getUri() == uri ? fileAttachment.setAttachmentStatus(FileAttachment.Status.ERROR_SUPPORTED_FILE_ATTACHMENT_COUNT_EXCEEDED) : fileAttachment
                ).collect(Collectors.toList()));
    }

    public void setFileAttachmentEngagementMissing(Uri uri) {
        observable.notifyUpdate(observable.fileAttachments
                .stream()
                .map(fileAttachment ->
                        fileAttachment.getUri() == uri ? fileAttachment.setAttachmentStatus(FileAttachment.Status.ERROR_ENGAGEMENT_MISSING) : fileAttachment
                ).collect(Collectors.toList()));
    }

    public void detachFile(FileAttachment attachment) {
        observable.notifyUpdate(
                observable.fileAttachments.stream()
                        .filter(
                                fileAttachment -> fileAttachment.getUri() != attachment.getUri()
                        ).collect(Collectors.toList()));
    }

    public void detachFiles(List<FileAttachment> attachments) {
        observable.notifyUpdate(
                observable.fileAttachments.stream()
                        .filter(attachment -> !attachments.contains(attachment))
                        .collect(Collectors.toList()));
    }

    private void onUploadFileError(Uri uri, GliaException exception) {
        observable.notifyUpdate(
                observable.fileAttachments.stream()
                        .map(
                                attachment -> attachment.getUri().equals(uri) ?
                                        attachment.setAttachmentStatus(getAttachmentStatus(exception)) :
                                        attachment
                        )
                        .collect(Collectors.toList()));
    }

    private FileAttachment.Status getAttachmentStatus(GliaException exception) {
        switch (exception.cause) {
            case INVALID_INPUT:
                return FileAttachment.Status.ERROR_INVALID_INPUT;
            case NETWORK_TIMEOUT:
                return FileAttachment.Status.ERROR_NETWORK_TIMEOUT;
            case INTERNAL_ERROR:
                return FileAttachment.Status.ERROR_INTERNAL;
            case PERMISSIONS_DENIED:
                return FileAttachment.Status.ERROR_PERMISSIONS_DENIED;
            case FILE_FORMAT_UNSUPPORTED:
                return FileAttachment.Status.ERROR_FORMAT_UNSUPPORTED;
            case FILE_TOO_LARGE:
                return FileAttachment.Status.ERROR_FILE_TOO_LARGE;
            default:
                return FileAttachment.Status.ERROR_UNKNOWN;
        }
    }

    private void onEngagementFileReceived(Uri uri, EngagementFile engagementFile) {
        observable.notifyUpdate(
                observable.fileAttachments.stream()
                        .map(attachment -> attachment.getUri().equals(uri) ?
                                attachment
                                        .setEngagementFile(engagementFile)
                                        .setAttachmentStatus(FileAttachment.Status.READY_TO_SEND) :
                                attachment
                        )
                        .collect(Collectors.toList()));
    }

    public List<FileAttachment> getFileAttachments() {
        return observable.fileAttachments;
    }

    public List<FileAttachment> getReadyToSendFileAttachments() {
        return observable.fileAttachments
                .stream()
                .filter(FileAttachment::isReadyToSend)
                .collect(Collectors.toList());
    }

    public void addObserver(Observer observer) {
        observable.addObserver(observer);
    }

    public void removeObserver(Observer observer) {
        observable.deleteObserver(observer);
    }

    public void clearObservers() {
        observable.deleteObservers();
    }
}

