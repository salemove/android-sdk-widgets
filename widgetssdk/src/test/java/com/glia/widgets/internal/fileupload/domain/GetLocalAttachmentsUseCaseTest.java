package com.glia.widgets.internal.fileupload.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.glia.widgets.internal.fileupload.FileAttachmentRepository;
import com.glia.widgets.internal.fileupload.model.LocalAttachment;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class GetLocalAttachmentsUseCaseTest {

    private FileAttachmentRepository repository;
    private GetFileAttachmentsUseCase subjectUnderTest;

    @Before
    public void setUp() {
        repository = mock(FileAttachmentRepository.class);
        subjectUnderTest = new GetFileAttachmentsUseCase(repository);
    }

    @Test
    public void execute_returnsFileAttachments_whenSingleFileAttachment() {
        List<LocalAttachment> localAttachments =
            Collections.singletonList(mock(LocalAttachment.class));
        when(repository.getFileAttachments()).thenReturn(localAttachments);

        List<LocalAttachment> result = subjectUnderTest.invoke();

        assertEquals(localAttachments, result);
    }
}
