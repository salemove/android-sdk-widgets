package com.glia.widgets.internal.fileupload.domain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.glia.widgets.internal.fileupload.FileAttachmentRepository;
import com.glia.widgets.helper.rx.Schedulers;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;

public class AddSecureLocalAttachmentsObserverUseCaseTest {

    private FileAttachmentRepository repository;
    private AddFileAttachmentsObserverUseCase subjectUnderTest;

    @Before
    public void setUp() {
        repository = mock(FileAttachmentRepository.class);
        Schedulers schedulers = mock();
        Scheduler scheduler = mock();
        when(schedulers.getMainScheduler()).thenReturn(scheduler);
        when(schedulers.getComputationScheduler()).thenReturn(scheduler);
        subjectUnderTest = new AddFileAttachmentsObserverUseCase(repository, schedulers);
    }

    @Test
    public void execute_callsRepositoryObserver_whenTriggered() {
        when(repository.getObservable()).thenReturn(Observable.empty());
        subjectUnderTest.invoke();
        verify(repository).getObservable();
    }
}
