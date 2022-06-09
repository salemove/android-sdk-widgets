package com.glia.widgets.filepreview.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException;
import com.glia.widgets.filepreview.domain.exception.RemoteFileIsDeletedException;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public class GetImageFileFromNetworkUseCaseTest {

    private GliaFileRepository fileRepository;
    private AttachmentFile attachmentFile;
    private GetImageFileFromNetworkUseCase useCase;

    @Before
    public void setUp() {
        fileRepository = mock(GliaFileRepository.class);
        attachmentFile = mock(AttachmentFile.class);
        useCase = new GetImageFileFromNetworkUseCase(fileRepository);
    }

    @Test
    public void execute_returnOnError_whenFileIsNull() {
        useCase.execute(null)
                .test()
                .assertError(FileNameMissingException.class);
    }

    @Test
    public void execute_returnOnError_whenFileNameIsEmpty() {
        when(attachmentFile.getName()).thenReturn(NAME_EMPTY);
        useCase.execute(attachmentFile)
                .test()
                .assertError(FileNameMissingException.class);
    }

    @Test
    public void execute_returnOnError_whenFileIsDeleted() {
        when(attachmentFile.getName()).thenReturn(NAME);
        when(attachmentFile.isDeleted()).thenReturn(true);
        useCase.execute(attachmentFile)
                .test()
                .assertError(RemoteFileIsDeletedException.class);
    }

    @Test
    public void execute_returnOnSuccess() {
        when(fileRepository.loadImageFileFromNetwork(any()))
                .thenReturn(Maybe.just(BITMAP));
        when(fileRepository.putImageToCache(any(), any()))
                .thenReturn(Completable.complete());
        when(attachmentFile.getName()).thenReturn(NAME);
        when(attachmentFile.isDeleted()).thenReturn(false);
        useCase.execute(attachmentFile)
                .test()
                .assertResult(BITMAP);
    }

    private static final String NAME = "NAME";
    private static final String NAME_EMPTY = "";
    private static final Bitmap BITMAP = mock(Bitmap.class);
}
