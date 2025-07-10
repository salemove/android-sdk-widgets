package com.glia.widgets.internal.fileupload.domain;

import static com.glia.widgets.internal.fileupload.domain.FileUploadLimitNotExceededObservableUseCase.FILE_UPLOAD_LIMIT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.glia.widgets.internal.fileupload.FileAttachmentRepository;
import com.glia.widgets.internal.fileupload.model.LocalAttachment;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class FileUploadLimitNotExceededObservableUseCaseTest {

    private FileAttachmentRepository repository;
    private FileUploadLimitNotExceededObservableUseCase subjectUnderTest;

    @Before
    public void setUp() {
        repository = mock(FileAttachmentRepository.class);
        subjectUnderTest = new FileUploadLimitNotExceededObservableUseCase(repository);
    }

    @Test
    public void execute_returnsTrue_whenSingleFileAttachment() {
        when(repository.getObservable())
            .thenReturn(Observable.just(Collections.singletonList(mock(LocalAttachment.class))));
        subjectUnderTest.invoke().test().assertValue(true);
    }

    @Test
    public void execute_returnsTrue_whenNoFileAttachment() {
        when(repository.getObservable()).thenReturn(Observable.just(Collections.emptyList()));

        subjectUnderTest.invoke().test().assertValue(true);
    }

    @Test
    public void execute_returnsFalse_whenSupportedFileAttachmentsCount() {
        List<LocalAttachment> attachments = Collections.nCopies(FILE_UPLOAD_LIMIT, mock(LocalAttachment.class));
        when(repository.getObservable()).thenReturn(Observable.just(attachments));

        subjectUnderTest.invoke().test().assertValue(false);
    }

    @Test
    public void execute_returnsFalse_whenMoreThanSupportedFileAttachments() {
        List<LocalAttachment> attachments = Collections.nCopies(FILE_UPLOAD_LIMIT + 1, mock(LocalAttachment.class));
        when(repository.getObservable()).thenReturn(Observable.just(attachments));

        subjectUnderTest.invoke().test().assertValue(false);
    }
}
