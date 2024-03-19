package com.glia.widgets.core.fileupload.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;
import com.glia.widgets.core.fileupload.model.FileAttachment;

import org.junit.Before;
import org.junit.Test;

public class RemoveFileAttachmentUseCaseTest {

    private FileAttachmentRepository repository;
    private RemoveFileAttachmentUseCase subjectUnderTest;

    @Before
    public void setUp() {
        repository = mock(FileAttachmentRepository.class);
        subjectUnderTest = new RemoveFileAttachmentUseCase(repository);
    }

    @Test
    public void execute_callsRepositoryDetachFile_whenValidArgument() {
        FileAttachment fileAttachment = mock(FileAttachment.class);

        subjectUnderTest.invoke(fileAttachment);

        verify(repository).detachFile(fileAttachment);
    }

    @Test
    public void execute_callsRepositoryDetachFile_whenNullArgument() {
        subjectUnderTest.invoke(null);

        verify(repository).detachFile(null);
    }
}
