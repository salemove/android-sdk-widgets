package com.glia.widgets.core.fileupload.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.Observer;

public class AddSecureLocalAttachmentsObserverUseCaseTest {

    private FileAttachmentRepository repository;
    private AddFileAttachmentsObserverUseCase subjectUnderTest;

    @Before
    public void setUp() {
        repository = mock(FileAttachmentRepository.class);
        subjectUnderTest = new AddFileAttachmentsObserverUseCase(repository);
    }

    @Test
    public void execute_callsRepositoryAddObserver_whenValidArgument() {
        Observer observer = mock(Observer.class);
        subjectUnderTest.execute(observer);
        verify(repository).addObserver(observer);
    }

    @Test
    public void execute_callsRepositoryAddObserver_whenNullArgument() {
        subjectUnderTest.execute(null);
        verify(repository).addObserver(null);
    }
}
