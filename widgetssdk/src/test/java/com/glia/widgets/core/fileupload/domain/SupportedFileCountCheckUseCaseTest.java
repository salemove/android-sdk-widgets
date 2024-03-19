package com.glia.widgets.core.fileupload.domain;

import static com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase.SUPPORTED_FILE_COUNT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.model.FileAttachment;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        when(repository.getFileAttachments())
                .thenReturn(Collections.singletonList(FILE_ATTACHMENT));

        assertTrue(subjectUnderTest.invoke());
    }

    @Test
    public void execute_returnsTrue_whenNoFileAttachment() {
        when(repository.getFileAttachments())
                .thenReturn(Collections.emptyList());

        assertTrue(subjectUnderTest.invoke());
    }

    @Test
    public void execute_returnsTrue_whenSupportedFileAttachmentsCount() {
        List<FileAttachment> fileAttachmentList = new ArrayList<>();
        for (int i = 0; i < SUPPORTED_FILE_COUNT; i++) {
            fileAttachmentList.add(FILE_ATTACHMENT);
        }
        when(repository.getFileAttachments()).thenReturn(fileAttachmentList);

        assertTrue(subjectUnderTest.invoke());
    }

    @Test
    public void execute_returnsFalse_whenMoreThanSupportedFileAttachments() {
        List<FileAttachment> fileAttachmentList = new ArrayList<>();
        for (int i = 0; i < SUPPORTED_FILE_COUNT + 1; i++) {
            fileAttachmentList.add(FILE_ATTACHMENT);
        }
        when(repository.getFileAttachments()).thenReturn(fileAttachmentList);

        assertFalse(subjectUnderTest.invoke());
    }

    private static final FileAttachment FILE_ATTACHMENT = mock(FileAttachment.class);
}
