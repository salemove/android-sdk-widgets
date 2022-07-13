package com.glia.widgets.filepreview.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Maybe;

public class GetImageFileFromCacheUseCaseTest {

    private GliaFileRepository fileRepository;
    private GetImageFileFromCacheUseCase useCase;

    @Before
    public void setUp() {
        fileRepository = mock(GliaFileRepository.class);
        useCase = new GetImageFileFromCacheUseCase(fileRepository);
    }

    @Test
    public void execute_emitsFileNameMissingException_whenFileNameIsNull() {
        useCase.execute(null)
                .test()
                .assertError(FileNameMissingException.class);
    }

    @Test
    public void execute_emitsFileNameMissingException_whenFileNameIsEmpty() {
        useCase.execute(NAME_EMPTY)
                .test()
                .assertError(FileNameMissingException.class);
    }

    @Test
    public void execute_successfullyCompletes_whenValidArgument() {
        when(fileRepository.loadImageFromCache(any()))
                .thenReturn(Maybe.just(BITMAP));
        useCase.execute(NAME)
                .test()
                .assertResult(BITMAP);
    }

    private static final String NAME = "NAME";
    private static final String NAME_EMPTY = "";
    private static final Bitmap BITMAP = mock(Bitmap.class);
}
