package com.glia.widgets.core.fileupload.domain;

import static com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase.SUPPORTED_FILE_SIZE;
import static com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase.SUPPORTED_FILE_COUNT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;

import com.glia.widgets.chat.ChatType;
import com.glia.widgets.core.engagement.GliaEngagementConfigRepository;
import com.glia.widgets.core.engagement.exception.EngagementMissingException;
import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.exception.RemoveBeforeReUploadingException;
import com.glia.widgets.core.fileupload.exception.SupportedFileCountExceededException;
import com.glia.widgets.core.fileupload.exception.SupportedFileSizeExceededException;
import com.glia.widgets.core.fileupload.model.FileAttachment;
import com.glia.widgets.engagement.IsQueueingOrEngagementUseCase;

import org.junit.Before;
import org.junit.Test;

public class AddFileToAttachmentAndUploadUseCaseTest {

    private FileAttachmentRepository fileAttachmentRepository;
    private AddFileToAttachmentAndUploadUseCase subjectUnderTest;
    private GliaEngagementConfigRepository gliaEngagementConfigRepository;

    private IsQueueingOrEngagementUseCase isQueueingOrEngagementUseCase;

    @Before
    public void setUp() {
        isQueueingOrEngagementUseCase = mock(IsQueueingOrEngagementUseCase.class);
        fileAttachmentRepository = mock(FileAttachmentRepository.class);
        gliaEngagementConfigRepository = mock(GliaEngagementConfigRepository.class);
        subjectUnderTest = new AddFileToAttachmentAndUploadUseCase(
                isQueueingOrEngagementUseCase,
                fileAttachmentRepository,
                gliaEngagementConfigRepository
        );
    }

    @Test
    public void execute_callsOnErrorWithRemoveBeforeReUploadingException_whenFileAttachmentIsAttached() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        Uri uri = mock(Uri.class);
        when(fileAttachment.getUri()).thenReturn(uri);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(true);

        subjectUnderTest.execute(fileAttachment, listener);

        verify(listener).onError(isA(RemoveBeforeReUploadingException.class));
    }

    @Test
    public void execute_callsOnErrorWithEngagementMissingException_whenEngagementIsMissing() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        Uri uri = mock(Uri.class);
        when(fileAttachment.getUri()).thenReturn(uri);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(isQueueingOrEngagementUseCase.getHasOngoingEngagement()).thenReturn(false);

        subjectUnderTest.execute(fileAttachment, listener);

        verify(listener).onError(isA(EngagementMissingException.class));
    }

    @Test
    public void execute_callsOnErrorWithSupportedFileCountExceededException_whenTooManyAttachedFiles() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        Uri uri = mock(Uri.class);
        when(fileAttachment.getUri()).thenReturn(uri);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(fileAttachmentRepository.getAttachedFilesCount()).thenReturn(SUPPORTED_FILE_COUNT + 1);
        when(isQueueingOrEngagementUseCase.getHasOngoingEngagement()).thenReturn(true);

        subjectUnderTest.execute(fileAttachment, listener);

        verify(listener).onError(isA(SupportedFileCountExceededException.class));
    }

    @Test
    public void execute_callsOnErrorWithSupportedFileSizeExceededException_whenFileAttachmentTooLarge() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        Uri uri = mock(Uri.class);
        when(fileAttachment.getUri()).thenReturn(uri);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(fileAttachmentRepository.getAttachedFilesCount()).thenReturn(1L);
        when(isQueueingOrEngagementUseCase.getHasOngoingEngagement()).thenReturn(true);
        when(fileAttachment.getSize()).thenReturn(SUPPORTED_FILE_SIZE);

        subjectUnderTest.execute(fileAttachment, listener);

        verify(listener).onError(isA(SupportedFileSizeExceededException.class));
    }

    @Test
    public void execute_callsOnStarted_whenValidArgument() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        Uri uri = mock(Uri.class);
        when(fileAttachment.getUri()).thenReturn(uri);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(fileAttachmentRepository.getAttachedFilesCount()).thenReturn(1L);
        when(isQueueingOrEngagementUseCase.getHasOngoingEngagement()).thenReturn(true);
        when(fileAttachment.getSize()).thenReturn(SUPPORTED_FILE_SIZE - 1);

        subjectUnderTest.execute(fileAttachment, listener);

        verify(listener).onStarted();
    }

    @Test(expected = NullPointerException.class)
    public void execute_throwsNullPointerException_whenFileAttachmentIsNull() {
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(fileAttachmentRepository.getAttachedFilesCount()).thenReturn(1L);
        when(isQueueingOrEngagementUseCase.getHasOngoingEngagement()).thenReturn(true);

        subjectUnderTest.execute(null, listener);
    }

    @Test(expected = NullPointerException.class)
    public void execute_throwsNullPointerException_whenListenerIsNull() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(fileAttachmentRepository.getAttachedFilesCount()).thenReturn(1L);
        when(isQueueingOrEngagementUseCase.getHasOngoingEngagement()).thenReturn(true);

        subjectUnderTest.execute(fileAttachment, null);
    }

    @Test
    public void execute_checkIsSecureEngagement_whenHasNoOngoingEngagement() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        Uri uri = mock(Uri.class);
        when(fileAttachment.getUri()).thenReturn(uri);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(isQueueingOrEngagementUseCase.getHasOngoingEngagement()).thenReturn(false);

        subjectUnderTest.execute(fileAttachment, listener);

        verify(gliaEngagementConfigRepository).getChatType();
    }

    @Test
    public void execute_uploadFile_whenIsSecureEngagement() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        Uri uri = mock(Uri.class);
        when(fileAttachment.getUri()).thenReturn(uri);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(isQueueingOrEngagementUseCase.getHasOngoingEngagement()).thenReturn(false);
        when(gliaEngagementConfigRepository.getChatType()).thenReturn(ChatType.SECURE_MESSAGING);
        when(fileAttachmentRepository.getAttachedFilesCount()).thenReturn(1L);
        when(fileAttachment.getSize()).thenReturn(SUPPORTED_FILE_SIZE - 1);

        subjectUnderTest.execute(fileAttachment, listener);

        verify(fileAttachmentRepository, times(1)).uploadFile(fileAttachment, listener);
    }
}
