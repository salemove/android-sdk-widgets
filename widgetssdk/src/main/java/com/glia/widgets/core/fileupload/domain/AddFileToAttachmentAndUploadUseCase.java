package com.glia.widgets.core.fileupload.domain;

import com.glia.androidsdk.engagement.EngagementFile;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.engagement.exception.EngagementMissingException;
import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.exception.RemoveBeforeReUploadingException;
import com.glia.widgets.core.fileupload.exception.SupportedFileCountExceededException;
import com.glia.widgets.core.fileupload.exception.SupportedFileSizeExceededException;
import com.glia.widgets.core.fileupload.model.FileAttachment;

public class AddFileToAttachmentAndUploadUseCase {
    private final static long SUPPORTED_FILE_COUNT = 25;
    private final static long SUPPORTED_FILE_SIZE_25MB = 26214400L;

    private final GliaEngagementRepository gliaEngagementRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    public AddFileToAttachmentAndUploadUseCase(
            GliaEngagementRepository gliaEngagementRepository,
            FileAttachmentRepository fileAttachmentRepository
    ) {
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.gliaEngagementRepository = gliaEngagementRepository;
    }

    public void execute(FileAttachment file, Listener listener) {
        if (fileAttachmentRepository.isFileAttached(file.getUri())) {
            listener.onError(new RemoveBeforeReUploadingException());
        } else {
            onFileNotAttached(file, listener);
        }
    }

    private void onFileNotAttached(FileAttachment file, Listener listener) {
        fileAttachmentRepository.attachFile(file);
        if (hasNoOngoingEngagement()) {
            fileAttachmentRepository.setFileAttachmentEngagementMissing(file.getUri());
            listener.onError(new EngagementMissingException());
        } else {
            onHasOngoingEngagement(file, listener);
        }
    }

    private void onHasOngoingEngagement(FileAttachment file, Listener listener) {
        if (isSupportedFileCountExceeded()) {
            fileAttachmentRepository.setSupportedFileAttachmentCountExceeded(file.getUri());
            listener.onError(new SupportedFileCountExceededException());
        } else if (isSupportedFileSizeExceeded(file)) {
            fileAttachmentRepository.setFileAttachmentTooLarge(file.getUri());
            listener.onError(new SupportedFileSizeExceededException());
        } else {
            listener.onStarted();
            fileAttachmentRepository.uploadFile(file, listener);
        }
    }

    private boolean isSupportedFileSizeExceeded(FileAttachment file) {
        return isSupportedFileSizeLargerThen25MB(file);
    }

    private boolean isSupportedFileSizeLargerThen25MB(FileAttachment file) {
        return file.getSize() >= SUPPORTED_FILE_SIZE_25MB;
    }

    private boolean isSupportedFileCountExceeded() {
        return fileAttachmentRepository.getAttachedFilesCount() > SUPPORTED_FILE_COUNT;
    }

    private boolean hasNoOngoingEngagement() {
        return !gliaEngagementRepository.hasOngoingEngagement();
    }

    public interface Listener {
        void onFinished();

        void onStarted();

        void onError(Exception ex);

        void onSecurityCheckStarted();

        void onSecurityCheckFinished(EngagementFile.ScanResult scanResult);
    }
}
