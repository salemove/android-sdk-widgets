package com.glia.widgets.filepreview.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.glia.androidsdk.chat.AttachmentFile;
import com.glia.widgets.chat.domain.DecodeSampledBitmapFromInputStreamUseCase;
import com.glia.widgets.filepreview.data.GliaFileRepository;
import com.glia.widgets.filepreview.domain.exception.FileNameMissingException;
import com.glia.widgets.filepreview.domain.exception.RemoteFileIsDeletedException;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromNetworkUseCase;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Maybe;

public class GetImageFileFromNetworkUseCaseTest {

    private GliaFileRepository fileRepository;
    private AttachmentFile attachmentFile;
    private GetImageFileFromNetworkUseCase useCase;

    private static final InputStream INPUT_STREAM = mock(InputStream.class);
    private static final String NAME = "NAME";
    private static final String NAME_EMPTY = "";
    private static final Bitmap BITMAP = mock(Bitmap.class);
    private DecodeSampledBitmapFromInputStreamUseCase decodeSampledBitmapFromInputStreamUseCase;

    @Before
    public void setUp() {
        fileRepository = mock(GliaFileRepository.class);
        attachmentFile = mock(AttachmentFile.class);
        decodeSampledBitmapFromInputStreamUseCase = mock(DecodeSampledBitmapFromInputStreamUseCase.class);
        useCase = new GetImageFileFromNetworkUseCase(fileRepository, decodeSampledBitmapFromInputStreamUseCase);
    }

    @Test
    public void execute_emitsFileNameMissingException_whenFileIsNull() {
        useCase.invoke(null)
                .test()
                .assertError(FileNameMissingException.class);
    }

    @Test
    public void execute_emitsFileNameMissingException_whenFileNameIsEmpty() {
        when(attachmentFile.getName()).thenReturn(NAME_EMPTY);
        useCase.invoke(attachmentFile)
                .test()
                .assertError(FileNameMissingException.class);
    }

    @Test
    public void execute_emitsError_whenDecodingFails() {
        when(fileRepository.loadImageFileFromNetwork(any())).thenReturn(Maybe.just(INPUT_STREAM));
        when(attachmentFile.getName()).thenReturn(NAME);
        when(attachmentFile.isDeleted()).thenReturn(false);
        when(decodeSampledBitmapFromInputStreamUseCase.invoke(INPUT_STREAM))
                .thenReturn(Maybe.error(new IOException()));

        useCase.invoke(attachmentFile)
                .test()
                .assertError(IOException.class);
    }

    @Test
    public void execute_emitsRemoteFileIsDeletedException_whenFileIsDeleted() {
        when(attachmentFile.getName()).thenReturn(NAME);
        when(attachmentFile.isDeleted()).thenReturn(true);
        useCase.invoke(attachmentFile)
                .test()
                .assertError(RemoteFileIsDeletedException.class);
    }

    @Test
    public void execute_successfullyCompletes_whenValidArgument() {
        when(fileRepository.loadImageFileFromNetwork(any())).thenReturn(Maybe.just(INPUT_STREAM));
        when(decodeSampledBitmapFromInputStreamUseCase.invoke(INPUT_STREAM)).thenReturn(Maybe.just(BITMAP));
        when(attachmentFile.getName()).thenReturn(NAME);
        when(attachmentFile.isDeleted()).thenReturn(false);
        useCase.invoke(attachmentFile).test().assertResult(BITMAP);
    }
}
