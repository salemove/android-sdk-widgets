package com.glia.widgets.core.fileupload.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.glia.widgets.core.fileupload.EngagementFileAttachmentRepository;
import com.glia.widgets.core.fileupload.model.LocalAttachment;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class GetLocalAttachmentsUseCaseTest {

    private EngagementFileAttachmentRepository repository;
    private GetFileAttachmentsUseCase subjectUnderTest;

    @Before
    public void setUp() {
        repository = mock(EngagementFileAttachmentRepository.class);
        subjectUnderTest = new GetFileAttachmentsUseCase(repository);
    }

    @Test
    public void execute_returnsFileAttachments_whenSingleFileAttachment() {
        List<LocalAttachment> localAttachments =
            Collections.singletonList(mock(LocalAttachment.class));
        when(repository.getGetFileAttachments()).thenReturn(localAttachments);

        List<LocalAttachment> result = subjectUnderTest.invoke();

        assertEquals(localAttachments, result);
    }
}
