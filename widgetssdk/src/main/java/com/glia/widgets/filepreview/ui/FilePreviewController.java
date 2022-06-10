package com.glia.widgets.filepreview.ui;

import com.glia.widgets.core.engagement.domain.GliaOnEngagementEndUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromCacheUseCase;
import com.glia.widgets.filepreview.domain.usecase.GetImageFileFromDownloadsUseCase;
import com.glia.widgets.filepreview.domain.usecase.PutImageFileToDownloadsUseCase;
import com.glia.widgets.helper.Logger;

import io.reactivex.disposables.Disposable;

public class FilePreviewController
        implements FilePreviewContract.Controller, GliaOnEngagementEndUseCase.Listener {
    private static final String TAG = FilePreviewController.class.getSimpleName();

    private FilePreviewContract.View view;
    private State state = new State();

    private final GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase;
    private final GetImageFileFromCacheUseCase getImageFileFromCacheUseCase;
    private final PutImageFileToDownloadsUseCase putImageFileToDownloadsUseCase;
    private final GliaOnEngagementEndUseCase onEngagementEndUseCase;

    private Disposable disposable;

    public FilePreviewController(
            GetImageFileFromDownloadsUseCase getImageFileFromDownloadsUseCase,
            GetImageFileFromCacheUseCase getImageFileFromCacheUseCase,
            PutImageFileToDownloadsUseCase putImageFileToDownloadsUseCase,
            GliaOnEngagementEndUseCase onEngagementEndUseCase
    ) {
        this.getImageFileFromDownloadsUseCase = getImageFileFromDownloadsUseCase;
        this.getImageFileFromCacheUseCase = getImageFileFromCacheUseCase;
        this.putImageFileToDownloadsUseCase = putImageFileToDownloadsUseCase;
        this.onEngagementEndUseCase = onEngagementEndUseCase;
    }

    public void setView(FilePreviewContract.View view) {
        this.view = view;
        this.state.reset();
        onEngagementEndUseCase.execute(this);
    }

    @Override
    public void onImageDataReceived(String bitmapId, String bitmapName) {
        setState(state.setImageData(bitmapId, bitmapName));
    }

    @Override
    public void onImageRequested() {
        setState(state.setImageLoadingFromDownloads());
        disposable = getImageFileFromDownloadsUseCase.execute(state.getImageIdName())
                .subscribe(
                        image -> setState(
                                state.setImageLoadedFromDownloads()
                                        .setLoadedImage(image)
                        ),
                        error -> onRequestImageFromCache()
                );
    }

    @Override
    public void onSharePressed() {
        view.shareImageFile(state.getImageIdName());
    }

    @Override
    public void onDownloadPressed() {
        disposable = putImageFileToDownloadsUseCase
                .execute(state.getImageIdName(), state.getLoadedImage())
                .subscribe(
                        () -> {
                            setState(state.setImageLoadedFromDownloads());
                            view.showOnImageSaveSuccess();
                        },
                        error -> view.showOnImageSaveFailed()
                );
    }

    @Override
    public void onDestroy() {
        if (disposable != null) disposable.dispose();
        onEngagementEndUseCase.unregisterListener(this);
        state.reset();
    }

    @Override
    public void engagementEnded() {
        Logger.d(TAG, "engagementEndedByOperator");
        view.engagementEnded();
    }

    private synchronized void setState(State state) {
        this.state = state;
        if (view != null) view.onStateUpdated(state);
    }

    private void onRequestImageFromCache() {
        setState(state.setImageLoadingFromCache());
        disposable = getImageFileFromCacheUseCase.execute(state.getImageIdName())
                .subscribe(
                        image -> setState(state.setImageLoadedFromCache().setLoadedImage(image)),
                        error -> {
                            view.showOnImageLoadingFailed();
                            setState(state.setImageLoadingFailure());
                        }
                );
    }
}
