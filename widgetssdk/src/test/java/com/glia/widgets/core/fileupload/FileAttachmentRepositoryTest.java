package com.glia.widgets.core.fileupload;

import static com.glia.widgets.core.fileupload.model.FileAttachment.Status.ERROR_ENGAGEMENT_MISSING;
import static com.glia.widgets.core.fileupload.model.FileAttachment.Status.ERROR_FILE_TOO_LARGE;
import static com.glia.widgets.core.fileupload.model.FileAttachment.Status.ERROR_INTERNAL;
import static com.glia.widgets.core.fileupload.model.FileAttachment.Status.ERROR_SECURITY_SCAN_FAILED;
import static com.glia.widgets.core.fileupload.model.FileAttachment.Status.ERROR_SUPPORTED_FILE_ATTACHMENT_COUNT_EXCEEDED;
import static com.glia.widgets.core.fileupload.model.FileAttachment.Status.READY_TO_SEND;
import static com.glia.widgets.core.fileupload.model.FileAttachment.Status.SECURITY_SCAN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;

import com.glia.androidsdk.Engagement;
import com.glia.androidsdk.GliaException;
import com.glia.androidsdk.RequestCallback;
import com.glia.androidsdk.engagement.EngagementFile;
import com.glia.widgets.core.engagement.exception.EngagementMissingException;
import com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase;
import com.glia.widgets.core.fileupload.model.FileAttachment;
import com.glia.widgets.di.GliaCore;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Observer;
import java.util.Optional;
import java.util.function.Consumer;

public class FileAttachmentRepositoryTest {

    private FileAttachmentRepository subjectUnderTest;
    GliaCore gliaCore;

    @Before
    public void setUp() {
        gliaCore = mock(GliaCore.class);
        subjectUnderTest = new FileAttachmentRepository(gliaCore);

        when(FILE_ATTACHMENT_1.getUri()).thenReturn(URI_1);
        when(FILE_ATTACHMENT_2.getUri()).thenReturn(URI_2);
        when(FILE_ATTACHMENT_1.setEngagementFile(any())).thenReturn(FILE_ATTACHMENT_1);
        when(FILE_ATTACHMENT_1.setAttachmentStatus(any())).thenReturn(FILE_ATTACHMENT_1);
    }

    @Test
    public void getAttachedFilesCount_returnsZero_whenNoFileAttachmentsAttached() {
        subjectUnderTest.detachAllFiles();
        long result = subjectUnderTest.getAttachedFilesCount();
        assertEquals(0, result);
    }

    @Test
    public void getAttachedFilesCount_returnsOne_whenOneFileAttachmentAttached() {
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        long result = subjectUnderTest.getAttachedFilesCount();
        assertEquals(1, result);
    }

    @Test
    public void isFileAttached_returnsTrue_whenFileAttachmentAttached() {
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        boolean result = subjectUnderTest.isFileAttached(URI_1);
        assertTrue(result);
    }

    @Test
    public void isFileAttached_returnsFalse_whenFileAttachmentNotAttached() {
        subjectUnderTest.detachAllFiles();
        boolean result = subjectUnderTest.isFileAttached(URI_1);
        assertFalse(result);
    }

    @Test
    public void isFileAttached_returnsFalse_whenNullArgument() {
        subjectUnderTest.detachAllFiles();
        boolean result = subjectUnderTest.isFileAttached(null);
        assertFalse(result);
    }

    @Test
    public void attachFile_attachesFileAttachment_whenValidArgument() {
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        List<FileAttachment> result = subjectUnderTest.getFileAttachments();
        assertTrue(result.contains(FILE_ATTACHMENT_1));
    }

    @Test
    public void attachFile_attachesNull_whenNullArgument() {
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(null);
        List<FileAttachment> result = subjectUnderTest.getFileAttachments();
        assertTrue(result.contains(null));
    }

    @Test
    public void attachFile_attachesMultipleFiles_whenCalledMultipleTimes() {
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(null);
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        List<FileAttachment> result = subjectUnderTest.getFileAttachments();
        assertTrue(
                result.containsAll(
                        Arrays.asList(null, FILE_ATTACHMENT_1)
                )
        );
    }

    @Test
    public void attachFile_attachesSameFileMultipleTimes_whenCalledMultipleTimes() {
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        List<FileAttachment> result = subjectUnderTest.getFileAttachments();
        assertTrue(
                result.indexOf(FILE_ATTACHMENT_1) != result.lastIndexOf(FILE_ATTACHMENT_1)
        );
    }

    @Test
    public void uploadFile_successful_whenSecurityCheckNotNeeded() {
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(gliaCore.getCurrentEngagement()).thenReturn(Optional.of(ENGAGEMENT));
        clearInvocations(FILE_ATTACHMENT_1);
        doAnswer(invocation -> {
            RequestCallback<EngagementFile> callback = invocation.getArgument(1);
            callback.onResult(ENGAGEMENT_FILE, null);
            return null;
        }).when(ENGAGEMENT).uploadFile(any(Uri.class), any());
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);

        subjectUnderTest.uploadFile(FILE_ATTACHMENT_1, listener);

        verify(FILE_ATTACHMENT_1).setAttachmentStatus(READY_TO_SEND);
        verify(listener).onFinished();
    }

    @Test
    public void uploadFile_successful_whenSecurityNeeded() {
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(gliaCore.getCurrentEngagement()).thenReturn(Optional.of(ENGAGEMENT));
        clearInvocations(FILE_ATTACHMENT_1);
        when(ENGAGEMENT_FILE.isSecurityScanRequired()).thenReturn(true);
        doAnswer(invocation -> {
            RequestCallback<EngagementFile> callback = invocation.getArgument(1);
            callback.onResult(ENGAGEMENT_FILE, null);
            return null;
        }).when(ENGAGEMENT).uploadFile(any(Uri.class), any());
        doAnswer(invocation -> {
            Consumer<EngagementFile.ScanResult> callback = invocation.getArgument(1);
            callback.accept(SCAN_RESULT_CLEAN);
            return null;
        }).when(ENGAGEMENT_FILE).on(any(), any());
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);

        subjectUnderTest.uploadFile(FILE_ATTACHMENT_1, listener);

        verify(FILE_ATTACHMENT_1).setAttachmentStatus(SECURITY_SCAN);
        verify(listener).onSecurityCheckStarted();
        verify(listener).onSecurityCheckFinished(SCAN_RESULT_CLEAN);
        verify(listener).onFinished();
        verify(FILE_ATTACHMENT_1).setAttachmentStatus(READY_TO_SEND);
    }

    @Test
    public void uploadFile_fails_whenMissingEngagement() {
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(gliaCore.getCurrentEngagement()).thenReturn(Optional.empty());
        clearInvocations(FILE_ATTACHMENT_1);
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);

        subjectUnderTest.uploadFile(FILE_ATTACHMENT_1, listener);

        verify(listener).onError(isA(EngagementMissingException.class));
        verify(FILE_ATTACHMENT_1).setAttachmentStatus(ERROR_ENGAGEMENT_MISSING);
    }

    @Test
    public void uploadFile_fails_whenUploadFails() {
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(gliaCore.getCurrentEngagement()).thenReturn(Optional.of(ENGAGEMENT));
        clearInvocations(FILE_ATTACHMENT_1);
        doAnswer(invocation -> {
            RequestCallback<EngagementFile> callback = invocation.getArgument(1);
            callback.onResult(null, GLIA_EXCEPTION);
            return null;
        }).when(ENGAGEMENT).uploadFile(any(Uri.class), any());
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);

        subjectUnderTest.uploadFile(FILE_ATTACHMENT_1, listener);

        verify(listener).onError(GLIA_EXCEPTION);
        verify(FILE_ATTACHMENT_1).setAttachmentStatus(ERROR_INTERNAL);
    }

    @Test
    public void uploadFile_fails_whenSecurityCheckFails() {
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(gliaCore.getCurrentEngagement()).thenReturn(Optional.of(ENGAGEMENT));
        clearInvocations(FILE_ATTACHMENT_1);
        when(ENGAGEMENT_FILE.isSecurityScanRequired()).thenReturn(true);
        doAnswer(invocation -> {
            RequestCallback<EngagementFile> callback = invocation.getArgument(1);
            callback.onResult(ENGAGEMENT_FILE, null);
            return null;
        }).when(ENGAGEMENT).uploadFile(any(Uri.class), any());
        doAnswer(invocation -> {
            Consumer<EngagementFile.ScanResult> callback = invocation.getArgument(1);
            callback.accept(SCAN_RESULT_INFECTED);
            return null;
        }).when(ENGAGEMENT_FILE).on(any(), any());
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);

        subjectUnderTest.uploadFile(FILE_ATTACHMENT_1, listener);

        verify(FILE_ATTACHMENT_1).setAttachmentStatus(SECURITY_SCAN);
        verify(listener).onSecurityCheckStarted();
        verify(listener).onSecurityCheckFinished(SCAN_RESULT_INFECTED);
        verify(FILE_ATTACHMENT_1).setAttachmentStatus(ERROR_SECURITY_SCAN_FAILED);
        verify(listener).onFinished();
    }

    @Test
    public void setFileAttachmentTooLarge_updatesCorrectFileAttachmentStatus_whenMultipleFileAttachmentsAttached() {
        clearInvocations(FILE_ATTACHMENT_1);
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        subjectUnderTest.attachFile(FILE_ATTACHMENT_2);

        subjectUnderTest.setFileAttachmentTooLarge(URI_1);

        verify(FILE_ATTACHMENT_1).setAttachmentStatus(ERROR_FILE_TOO_LARGE);
        verify(FILE_ATTACHMENT_2, never()).setAttachmentStatus(any());
    }

    @Test
    public void setFileAttachmentTooLarge_updatesFileAttachmentStatus_whenNullArgument() {
        clearInvocations(FILE_ATTACHMENT_1);
        when(FILE_ATTACHMENT_1.getUri()).thenReturn(null);
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);

        subjectUnderTest.setFileAttachmentTooLarge(null);

        verify(FILE_ATTACHMENT_1).setAttachmentStatus(ERROR_FILE_TOO_LARGE);
    }

    @Test
    public void setFileAttachmentTooLarge_doesNothing_whenFileAttachmentNotAttached() {
        clearInvocations(FILE_ATTACHMENT_1);
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);

        subjectUnderTest.setFileAttachmentTooLarge(URI_2);

        verify(FILE_ATTACHMENT_1, never()).setAttachmentStatus(any());
    }

    @Test
    public void setSupportedFileAttachmentCountExceeded_updatesFileAttachmentStatus_whenFileAttachmentAttached() {
        clearInvocations(FILE_ATTACHMENT_1);
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);

        subjectUnderTest.setSupportedFileAttachmentCountExceeded(URI_1);

        verify(FILE_ATTACHMENT_1).setAttachmentStatus(ERROR_SUPPORTED_FILE_ATTACHMENT_COUNT_EXCEEDED);
    }

    @Test
    public void setFileAttachmentEngagementMissing_updatesFileAttachmentStatus_whenFileAttachmentAttached() {
        clearInvocations(FILE_ATTACHMENT_1);
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);

        subjectUnderTest.setFileAttachmentEngagementMissing(URI_1);

        verify(FILE_ATTACHMENT_1).setAttachmentStatus(ERROR_ENGAGEMENT_MISSING);
    }

    @Test
    public void detachFile_detachesFile_whenFileAttachmentAttached() {
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        subjectUnderTest.attachFile(FILE_ATTACHMENT_2);

        subjectUnderTest.detachFile(FILE_ATTACHMENT_1);

        List<FileAttachment> result = subjectUnderTest.getFileAttachments();

        assertFalse(result.contains(FILE_ATTACHMENT_1));
        assertTrue(result.contains(FILE_ATTACHMENT_2));
    }

    @Test
    public void detachFile_detachesAllFiles_whenSameFileAttachedMultipleTimes() {
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);

        subjectUnderTest.detachFile(FILE_ATTACHMENT_1);

        assertTrue(subjectUnderTest.getFileAttachments().isEmpty());
    }

    @Test
    public void detachFile_doesNothing_whenFileAttachmentNotAttached() {
        subjectUnderTest.detachAllFiles();

        subjectUnderTest.detachFile(FILE_ATTACHMENT_1);

        assertFalse(subjectUnderTest.getFileAttachments().contains(FILE_ATTACHMENT_1));
    }

    @Test(expected = NullPointerException.class)
    public void detachFile_throwsNullPointerException_whenNullArgument() {
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);

        subjectUnderTest.detachFile(null);
    }

    @Test
    public void detachAllFiles_detachesAllFiles_whenFilesAttached() {
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        subjectUnderTest.attachFile(FILE_ATTACHMENT_2);

        subjectUnderTest.detachAllFiles();

        assertTrue(subjectUnderTest.getFileAttachments().isEmpty());
    }

    @Test
    public void detachAllFiles_doesNothing_whenNoFilesAttached() {
        subjectUnderTest.detachAllFiles();
        subjectUnderTest.detachAllFiles();

        assertTrue(subjectUnderTest.getFileAttachments().isEmpty());
    }

    @Test
    public void getReadyToSendFileAttachments_returnsReadyToSendFileAttachments_whenNoFilesAttached() {
        subjectUnderTest.detachAllFiles();
        when(FILE_ATTACHMENT_1.isReadyToSend()).thenReturn(true);
        when(FILE_ATTACHMENT_2.isReadyToSend()).thenReturn(false);
        subjectUnderTest.attachFile(FILE_ATTACHMENT_1);
        subjectUnderTest.attachFile(FILE_ATTACHMENT_2);

        List<FileAttachment> result = subjectUnderTest.getReadyToSendFileAttachments();

        assertTrue(result.contains(FILE_ATTACHMENT_1));
        assertFalse(result.contains(FILE_ATTACHMENT_2));
    }

    @Test
    public void addObserver_successful_whenValidArgument() {
        clearInvocations(OBSERVER_1);
        subjectUnderTest.clearObservers();

        subjectUnderTest.addObserver(OBSERVER_1);

        subjectUnderTest.detachAllFiles();
        verify(OBSERVER_1).update(any(), any());
    }

    @Test
    public void addObserver_callsUpdateOnce_whenSameObserverAddedTwice() {
        clearInvocations(OBSERVER_1);
        subjectUnderTest.clearObservers();

        subjectUnderTest.addObserver(OBSERVER_1);
        subjectUnderTest.addObserver(OBSERVER_1);

        subjectUnderTest.detachAllFiles();
        verify(OBSERVER_1, times(1)).update(any(), any());
    }

    @Test(expected = NullPointerException.class)
    public void addObserver_throws_whenNullArgument() {
        subjectUnderTest.addObserver(null);
    }

    @Test
    public void removeObserver_removes_whenValidArgument() {
        clearInvocations(OBSERVER_1);
        subjectUnderTest.clearObservers();
        subjectUnderTest.addObserver(OBSERVER_1);

        subjectUnderTest.removeObserver(OBSERVER_1);

        subjectUnderTest.detachAllFiles();
        verify(OBSERVER_1, never()).update(any(), any());
    }

    @Test
    public void removeObserver_doesNothing_whenObserverNotAdded() {
        subjectUnderTest.clearObservers();

        subjectUnderTest.removeObserver(OBSERVER_1);
    }

    @Test
    public void removeObserver_notThrowing_whenNullArgument() {
        subjectUnderTest.removeObserver(null);
    }

    @Test
    public void clearObservers_clears_whenObserversAdded() {
        clearInvocations(OBSERVER_1);
        clearInvocations(OBSERVER_2);
        subjectUnderTest.clearObservers();
        subjectUnderTest.addObserver(OBSERVER_1);
        subjectUnderTest.addObserver(OBSERVER_2);

        subjectUnderTest.clearObservers();

        subjectUnderTest.detachAllFiles();
        verify(OBSERVER_1, never()).update(any(), any());
        verify(OBSERVER_2, never()).update(any(), any());
    }

    @Test
    public void clearObservers_doesNothing_whenNoObserversAdded() {
        subjectUnderTest.clearObservers();
        subjectUnderTest.clearObservers();
    }

    private static final FileAttachment FILE_ATTACHMENT_1 = mock(FileAttachment.class);
    private static final FileAttachment FILE_ATTACHMENT_2 = mock(FileAttachment.class);
    private static final Uri URI_1 = mock(Uri.class);
    private static final Uri URI_2 = mock(Uri.class);
    private static final Engagement ENGAGEMENT = mock(Engagement.class);
    private static final EngagementFile ENGAGEMENT_FILE = mock(EngagementFile.class);
    private static final GliaException GLIA_EXCEPTION =
            new GliaException("", GliaException.Cause.INTERNAL_ERROR);
    private static final EngagementFile.ScanResult SCAN_RESULT_CLEAN =
            EngagementFile.ScanResult.CLEAN;
    private static final EngagementFile.ScanResult SCAN_RESULT_INFECTED =
            EngagementFile.ScanResult.INFECTED;
    private static final Observer OBSERVER_1 = mock(Observer.class);
    private static final Observer OBSERVER_2 = mock(Observer.class);
}
