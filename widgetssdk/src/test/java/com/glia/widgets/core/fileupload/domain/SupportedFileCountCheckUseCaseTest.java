package com.glia.widgets.core.fileupload.domain;

import static com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase.SUPPORTED_FILE_COUNT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;

import org.junit.Before;
import org.junit.Test;

public class SupportedFileCountCheckUseCaseTest {

    private FileAttachmentRepository repository;
    private SupportedFileCountCheckUseCase subjectUnderTest;

    @Before
    public void setUp() {
        repository = mock(FileAttachmentRepository.class);
        subjectUnderTest = new SupportedFileCountCheckUseCase(repository);
    }

    @Test
    public void execute_returnsTrue_whenSingleFileAttachment() {
        when(repository.getAttachedFilesCount()).thenReturn(1);

        assertTrue(subjectUnderTest.invoke());
    }

    @Test
    public void execute_returnsTrue_whenNoFileAttachment() {
        when(repository.getAttachedFilesCount()).thenReturn(0);

        assertTrue(subjectUnderTest.invoke());
    }

    @Test
    public void execute_returnsTrue_whenSupportedFileAttachmentsCount() {
        when(repository.getAttachedFilesCount()).thenReturn(SUPPORTED_FILE_COUNT);

        assertTrue(subjectUnderTest.invoke());
    }

    @Test
    public void execute_returnsFalse_whenMoreThanSupportedFileAttachments() {
        when(repository.getAttachedFilesCount()).thenReturn(SUPPORTED_FILE_COUNT + 1);

        assertFalse(subjectUnderTest.invoke());
    }
}
