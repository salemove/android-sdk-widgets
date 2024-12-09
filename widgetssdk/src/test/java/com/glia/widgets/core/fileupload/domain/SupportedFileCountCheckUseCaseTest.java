package com.glia.widgets.core.fileupload.domain;

import static com.glia.widgets.core.fileupload.domain.SupportedFileCountCheckUseCase.SUPPORTED_FILE_COUNT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.glia.widgets.core.fileupload.EngagementFileAttachmentRepository;
import com.glia.widgets.core.fileupload.model.LocalAttachment;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SupportedFileCountCheckUseCaseTest {

    private EngagementFileAttachmentRepository repository;
    private SupportedFileCountCheckUseCase subjectUnderTest;

    @Before
    public void setUp() {
        repository = mock(EngagementFileAttachmentRepository.class);
        subjectUnderTest = new SupportedFileCountCheckUseCase(repository);
    }

    private static final LocalAttachment FILE_ATTACHMENT = mock(LocalAttachment.class);

    @Test
    public void execute_returnsTrue_whenSingleFileAttachment() {
        when(repository.getGetFileAttachments())
                .thenReturn(Collections.singletonList(FILE_ATTACHMENT));

        assertTrue(subjectUnderTest.invoke());
    }

    @Test
    public void execute_returnsTrue_whenNoFileAttachment() {
        when(repository.getGetFileAttachments())
                .thenReturn(Collections.emptyList());

        assertTrue(subjectUnderTest.invoke());
    }

    @Test
    public void execute_returnsTrue_whenSupportedFileAttachmentsCount() {
        List<LocalAttachment> localAttachmentList = new ArrayList<>();
        for (int i = 0; i < SUPPORTED_FILE_COUNT; i++) {
            localAttachmentList.add(FILE_ATTACHMENT);
        }
        when(repository.getGetFileAttachments()).thenReturn(localAttachmentList);

        assertTrue(subjectUnderTest.invoke());
    }

    @Test
    public void execute_returnsFalse_whenMoreThanSupportedFileAttachments() {
        List<LocalAttachment> localAttachmentList = new ArrayList<>();
        for (int i = 0; i < SUPPORTED_FILE_COUNT + 1; i++) {
            localAttachmentList.add(FILE_ATTACHMENT);
        }
        when(repository.getGetFileAttachments()).thenReturn(localAttachmentList);

        assertFalse(subjectUnderTest.invoke());
    }
}
