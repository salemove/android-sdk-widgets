package com.glia.widgets.core.fileupload.domain;

import static com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase.SUPPORTED_FILE_COUNT;

import androidx.annotation.VisibleForTesting;

import com.glia.androidsdk.engagement.EngagementFile;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.engagement.exception.EngagementMissingException;
import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.exception.RemoveBeforeReUploadingException;
import com.glia.widgets.core.fileupload.exception.SupportedFileCountExceededException;
import com.glia.widgets.core.fileupload.exception.SupportedFileSizeExceededException;
import com.glia.widgets.core.fileupload.model.FileAttachment;

public class AddFileToAttachmentAndUploadUseCase {

    private final GliaEngagementRepository gliaEngagementRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    public AddFileToAttachmentAndUploadUseCase(
            GliaEngagementRepository gliaEngagementRepository,
            FileAttachmentRepository fileAttachmentRepository
    ) {
        this.gliaEngagementRepository = gliaEngagementRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
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
        return file.getSize() >= SUPPORTED_FILE_SIZE;
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

    public final static long SUPPORTED_FILE_SIZE = 26214400L;
}
