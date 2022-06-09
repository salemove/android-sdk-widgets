package com.glia.widgets.filepreview.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException;
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Completable;

public class PutImageFileToDownloadsUseCaseTest {

    private GliaFileRepository fileRepository;
    private PutImageFileToDownloadsUseCase useCase;

    @Before
    public void setUp() {
        fileRepository = mock(GliaFileRepository.class);
        useCase = new PutImageFileToDownloadsUseCase(fileRepository);
    }

    @Test
    public void execute_returnOnError_whenFileNameIsNull() {
        useCase.execute(null, BITMAP)
                .test()
                .assertError(FileNameMissingException.class);
    }

    @Test
    public void execute_returnOnError_whenFileNameIsEmpty() {
        useCase.execute(NAME_EMPTY, BITMAP)
                .test()
                .assertError(FileNameMissingException.class);
    }

    @Test
    public void execute_returnOnComplete() {
        when(fileRepository.putImageToDownloads(any(), any()))
                .thenReturn(Completable.complete());
        useCase.execute(NAME, BITMAP)
                .test()
                .assertComplete();
    }

    @Test
    public void execute_returnOnComplete_whenBitmapIsNull() {
        when(fileRepository.putImageToDownloads(any(), any()))
                .thenReturn(Completable.complete());
        useCase.execute(NAME, null)
                .test()
                .assertComplete();
    }

    private static final String NAME = "NAME";
    private static final String NAME_EMPTY = "";
    private static final Bitmap BITMAP = mock(Bitmap.class);
}
