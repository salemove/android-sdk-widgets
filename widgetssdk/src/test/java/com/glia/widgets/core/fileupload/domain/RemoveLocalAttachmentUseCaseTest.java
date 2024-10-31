package com.glia.widgets.core.fileupload.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.model.LocalAttachment;

import org.junit.Before;
import org.junit.Test;

public class RemoveLocalAttachmentUseCaseTest {

    private FileAttachmentRepository repository;
    private RemoveFileAttachmentUseCase subjectUnderTest;

    @Before
    public void setUp() {
        repository = mock(FileAttachmentRepository.class);
        subjectUnderTest = new RemoveFileAttachmentUseCase(repository);
    }

    @Test
    public void execute_callsRepositoryDetachFile_whenValidArgument() {
        LocalAttachment localAttachment = mock(LocalAttachment.class);

        subjectUnderTest.invoke(localAttachment);

        verify(repository).detachFile(localAttachment);
    }

    @Test
    public void execute_callsRepositoryDetachFile_whenNullArgument() {
        subjectUnderTest.invoke(null);

        verify(repository).detachFile(null);
    }
}
