package com.glia.widgets.fileupload.domain;

import android.net.Uri;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.engagement.EngagementFile;
import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.engagement.exception.EngagementMissingException;
import com.glia.widgets.fileupload.FileAttachmentRepository;
import com.glia.widgets.fileupload.exception.RemoveBeforeReUploadingException;
import com.glia.widgets.fileupload.exception.SupportedFileCountExceededException;

public class AddFileToAttachmentAndUploadUseCase {
    private final static long SUPPORTED_FILE_COUNT = 25;

    private final GliaEngagementRepository gliaEngagementRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    public AddFileToAttachmentAndUploadUseCase(
            GliaEngagementRepository gliaEngagementRepository,
            FileAttachmentRepository fileAttachmentRepository
    ) {
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.gliaEngagementRepository = gliaEngagementRepository;
    }

    public void execute(Uri fileUri, Listener listener) {
        if (fileAttachmentRepository.isFileAttached(fileUri)) {
            listener.onError(new RemoveBeforeReUploadingException());
        } else {
            onFileNotAttached(fileUri, listener);
        }
    }

    private void onFileNotAttached(Uri fileUri, Listener listener) {
        fileAttachmentRepository.attachFile(fileUri);
        if (hasNoOngoingEngagement()) {
            fileAttachmentRepository.setFileAttachmentEngagementMissing(fileUri);
            listener.onError(new EngagementMissingException());
        } else {
            onHasOngoingEngagement(fileUri, listener);
        }
    }

    private void onHasOngoingEngagement(Uri fileUri, Listener listener) {
        if (isSupportedFileCountExceeded()) {
            fileAttachmentRepository.setSupportedFileAttachmentCountExceeded(fileUri);
            listener.onError(new SupportedFileCountExceededException());
        } else {
            listener.onStarted();
            fileAttachmentRepository.uploadFile(fileUri, listener);
        }
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
