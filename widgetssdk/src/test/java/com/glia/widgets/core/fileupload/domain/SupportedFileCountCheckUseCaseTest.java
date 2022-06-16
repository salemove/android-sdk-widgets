package com.glia.widgets.core.fileupload.domain;

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

        assertTrue(subjectUnderTest.execute());
    }

    @Test
    public void execute_returnsTrue_whenNoFileAttachment() {
        when(repository.getFileAttachments())
                .thenReturn(Collections.emptyList());

        assertTrue(subjectUnderTest.execute());
    }

    @Test
    public void execute_returnsTrue_when25FileAttachments() {
        List<FileAttachment> fileAttachmentList = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            fileAttachmentList.add(FILE_ATTACHMENT);
        }
        when(repository.getFileAttachments()).thenReturn(fileAttachmentList);

        assertTrue(subjectUnderTest.execute());
    }

    @Test
    public void execute_returnsFalse_when26FileAttachments() {
        List<FileAttachment> fileAttachmentList = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            fileAttachmentList.add(FILE_ATTACHMENT);
        }
        when(repository.getFileAttachments()).thenReturn(fileAttachmentList);

        assertFalse(subjectUnderTest.execute());
    }

    private static final FileAttachment FILE_ATTACHMENT = mock(FileAttachment.class);
}
