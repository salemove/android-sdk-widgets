package com.glia.widgets.core.fileupload.domain;

import static com.glia.widgets.core.fileupload.domain.AddFileToAttachmentAndUploadUseCase.SUPPORTED_FILE_SIZE;
import static com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase.SUPPORTED_FILE_COUNT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.glia.widgets.core.engagement.GliaEngagementRepository;
import com.glia.widgets.core.engagement.exception.EngagementMissingException;
import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.exception.RemoveBeforeReUploadingException;
import com.glia.widgets.core.fileupload.exception.SupportedFileCountExceededException;
import com.glia.widgets.core.fileupload.exception.SupportedFileSizeExceededException;
import com.glia.widgets.core.fileupload.model.FileAttachment;

import org.junit.Before;
import org.junit.Test;

public class AddFileToAttachmentAndUploadUseCaseTest {

    private GliaEngagementRepository gliaEngagementRepository;
    private FileAttachmentRepository fileAttachmentRepository;
    private AddFileToAttachmentAndUploadUseCase subjectUnderTest;

    @Before
    public void setUp() {
        gliaEngagementRepository = mock(GliaEngagementRepository.class);
        fileAttachmentRepository = mock(FileAttachmentRepository.class);
        subjectUnderTest = new AddFileToAttachmentAndUploadUseCase(
                gliaEngagementRepository,
                fileAttachmentRepository
        );
    }

    @Test
    public void execute_callsOnErrorWithRemoveBeforeReUploadingException_whenFileAttachmentIsAttached() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(true);

        subjectUnderTest.execute(fileAttachment, listener);

        verify(listener).onError(isA(RemoveBeforeReUploadingException.class));
    }

    @Test
    public void execute_callsOnErrorWithEngagementMissingException_whenEngagementIsMissing() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(gliaEngagementRepository.hasOngoingEngagement()).thenReturn(false);

        subjectUnderTest.execute(fileAttachment, listener);

        verify(listener).onError(isA(EngagementMissingException.class));
    }

    @Test
    public void execute_callsOnErrorWithSupportedFileCountExceededException_whenTooManyAttachedFiles() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(fileAttachmentRepository.getAttachedFilesCount()).thenReturn(SUPPORTED_FILE_COUNT + 1);
        when(gliaEngagementRepository.hasOngoingEngagement()).thenReturn(true);

        subjectUnderTest.execute(fileAttachment, listener);

        verify(listener).onError(isA(SupportedFileCountExceededException.class));
    }

    @Test
    public void execute_callsOnErrorWithSupportedFileSizeExceededException_whenFileAttachmentTooLarge() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(fileAttachmentRepository.getAttachedFilesCount()).thenReturn(1L);
        when(gliaEngagementRepository.hasOngoingEngagement()).thenReturn(true);
        when(fileAttachment.getSize()).thenReturn(SUPPORTED_FILE_SIZE);

        subjectUnderTest.execute(fileAttachment, listener);

        verify(listener).onError(isA(SupportedFileSizeExceededException.class));
    }

    @Test
    public void execute_callsOnStarted_whenValidArgument() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        AddFileToAttachmentAndUploadUseCase.Listener listener =
                mock(AddFileToAttachmentAndUploadUseCase.Listener.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(fileAttachmentRepository.getAttachedFilesCount()).thenReturn(1L);
        when(gliaEngagementRepository.hasOngoingEngagement()).thenReturn(true);
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
        when(gliaEngagementRepository.hasOngoingEngagement()).thenReturn(true);

        subjectUnderTest.execute(null, listener);
    }

    @Test(expected = NullPointerException.class)
    public void execute_throwsNullPointerException_whenListenerIsNull() {
        FileAttachment fileAttachment = mock(FileAttachment.class);
        when(fileAttachmentRepository.isFileAttached(any())).thenReturn(false);
        when(fileAttachmentRepository.getAttachedFilesCount()).thenReturn(1L);
        when(gliaEngagementRepository.hasOngoingEngagement()).thenReturn(true);

        subjectUnderTest.execute(fileAttachment, null);
    }
}
