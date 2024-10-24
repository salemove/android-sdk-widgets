package com.glia.widgets.core.fileupload.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.glia.widgets.core.fileupload.FileAttachmentRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.Observer;

public class RemoveLocalAttachmentObserverUseCaseTest {

    private FileAttachmentRepository repository;
    private RemoveFileAttachmentObserverUseCase subjectUnderTest;

    @Before
    public void setUp() {
        repository = mock(FileAttachmentRepository.class);
        subjectUnderTest = new RemoveFileAttachmentObserverUseCase(repository);
    }

    @Test
    public void execute_callsRepositoryRemoveObserver_whenValidArgument() {
        Observer observer = mock(Observer.class);
        subjectUnderTest.invoke(observer);
        verify(repository).removeObserver(observer);
    }

    @Test
    public void execute_callsRepositoryRemoveObserver_whenNullArgument() {
        subjectUnderTest.invoke(null);
        verify(repository).removeObserver(null);
    }
}
