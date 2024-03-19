package com.glia.widgets.core.fileupload.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.model.FileAttachment;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class GetFileAttachmentsUseCaseTest {

    private FileAttachmentRepository repository;
    private GetFileAttachmentsUseCase subjectUnderTest;

    @Before
    public void setUp() {
        repository = mock(FileAttachmentRepository.class);
        subjectUnderTest = new GetFileAttachmentsUseCase(repository);
    }

    @Test
    public void execute_returnsFileAttachments_whenSingleFileAttachment() {
        List<FileAttachment> fileAttachments =
                Collections.singletonList(mock(FileAttachment.class));
        when(repository.getFileAttachments()).thenReturn(fileAttachments);

        List<FileAttachment> result = subjectUnderTest.invoke();

        assertEquals(fileAttachments, result);
    }
}
